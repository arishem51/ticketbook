package com.swd.ticketbook.services;

import com.swd.ticketbook.dto.order.*;
import com.swd.ticketbook.entities.*;
import com.swd.ticketbook.enums.BookingStatus;
import com.swd.ticketbook.enums.EventStatus;
import com.swd.ticketbook.exceptions.BusinessRuleViolationException;
import com.swd.ticketbook.exceptions.ResourceNotFoundException;
import com.swd.ticketbook.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for Order operations (UC-02.1, UC-02.2)
 * Business Rules: FR4, FR5, FR14, FR16, FR25
 */
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private EmailService emailService;

    /**
     * UC-02.1: Check if customer has pending order (FR5)
     * Customer can only have ONE active pending order across ALL events
     */
    public Optional<OrderResponse> getActivePendingOrder(Long userId) {
        Optional<Order> pendingOrder = orderRepository.findActivePendingOrderByUserId(
            userId, 
            LocalDateTime.now()
        );
        
        return pendingOrder.map(this::mapToOrderResponse);
    }

    /**
     * UC-02.1: Create Order (Draft)
     * FR5: Enforce single pending order rule
     * FR14: Enforce max tickets per order
     * FR16: Start 15-minute reservation timer
     * FR25: Collect recipient information
     */
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        // FR5: Check for existing pending order
        Optional<Order> existingPending = orderRepository.findActivePendingOrderByUserId(
            userId, 
            LocalDateTime.now()
        );
        
        if (existingPending.isPresent()) {
            throw new BusinessRuleViolationException(
                "You have a pending order. Please complete or cancel it first. Order ID: " 
                + existingPending.get().getOrderId()
            );
        }

        // Get user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get event
        Event event = eventRepository.findById(request.getEventId())
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // Validate event is active and not in the past
        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new BusinessRuleViolationException("Event is not available for booking");
        }
        if (event.hasOccurred()) {
            throw new BusinessRuleViolationException("Event has already occurred");
        }

        // Create order
        Order order = new Order(user, event);
        
        // Calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (OrderItemRequest item : request.getItems()) {
            TicketType ticketType = ticketTypeRepository.findById(item.getTicketTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));

            // Validate belongs to same event
            if (!ticketType.getEvent().getEventId().equals(event.getEventId())) {
                throw new BusinessRuleViolationException("Ticket type does not belong to this event");
            }

            // FR14: Check max quantity (if set by organizer)
            if (event.getMaxTicketQuantity() != null && 
                item.getQuantity() > event.getMaxTicketQuantity()) {
                throw new BusinessRuleViolationException(
                    "Maximum " + event.getMaxTicketQuantity() + " tickets per order"
                );
            }

            // Check availability
            if (item.getQuantity() > ticketType.getAvailableQuantity()) {
                throw new BusinessRuleViolationException(
                    "Only " + ticketType.getAvailableQuantity() + " tickets available for " 
                    + ticketType.getTypeName()
                );
            }

            // Reserve tickets (FR16)
            ticketType.reserve(item.getQuantity());
            ticketTypeRepository.save(ticketType);

            // Calculate amount
            BigDecimal itemAmount = ticketType.getPrice().multiply(new BigDecimal(item.getQuantity()));
            totalAmount = totalAmount.add(itemAmount);
            totalQuantity += item.getQuantity();
        }

        order.setTotalAmount(totalAmount);
        order.setTotalQuantity(totalQuantity);

        // FR25: Set recipient information
        RecipientInfoRequest recipientInfo = request.getRecipientInfo();
        order.setRecipientName(recipientInfo.getRecipientName());
        order.setRecipientPhone(recipientInfo.getRecipientPhone());
        order.setRecipientEmail(recipientInfo.getRecipientEmail());
        order.setRecipientAddress(recipientInfo.getRecipientAddress());
        order.setRecipientNotes(recipientInfo.getRecipientNotes());

        // FR16: Set 15-minute reservation expiry (already set in constructor)
        order = orderRepository.save(order);

        return mapToOrderResponse(order);
    }

    /**
     * UC-02.1: Process Payment
     * Initiates VNPAY payment
     */
    @Transactional
    public String initiatePayment(Long userId, PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Verify order belongs to user
        if (!order.getUser().getUserId().equals(userId)) {
            throw new BusinessRuleViolationException("Order does not belong to this user");
        }

        // Check order status
        if (order.getBookingStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessRuleViolationException("Order is not in pending payment status");
        }

        // FR16: Check if reservation expired
        if (order.isExpired()) {
            cancelExpiredOrder(order);
            throw new BusinessRuleViolationException("Order reservation has expired. Please create a new order.");
        }

        // Generate VNPAY payment URL
        String paymentUrl = vnPayService.createPaymentUrl(
            order.getOrderId().toString(),
            order.getTotalAmount(),
            "Order #" + order.getOrderId(),
            request.getReturnUrl()
        );

        return paymentUrl;
    }

    /**
     * UC-02.1: Confirm Payment
     * Called after successful VNPAY payment
     * FR6: Auto-generate unique QR codes for tickets
     */
    @Transactional
    public OrderResponse confirmPayment(Long orderId, String transactionId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getBookingStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessRuleViolationException("Order is not in pending payment status");
        }

        // FR16: Check if expired
        if (order.isExpired()) {
            throw new BusinessRuleViolationException("Order reservation has expired");
        }

        // Confirm order
        order.confirm();
        orderRepository.save(order);

        // FR6: Generate tickets with unique QR codes
        List<Ticket> tickets = generateTicketsForOrder(order);

        // Send confirmation email with tickets
        emailService.sendOrderConfirmation(
            order.getRecipientEmail(),
            order.getOrderId().toString(),
            order.getEvent().getName(),
            order.getTotalAmount(),
            tickets
        );

        return mapToOrderResponse(order);
    }

    /**
     * FR6: Generate unique QR codes for each ticket
     */
    private List<Ticket> generateTicketsForOrder(Order order) {
        List<Ticket> tickets = new ArrayList<>();

        // Get ticket types and quantities from the order
        Event event = order.getEvent();
        List<TicketType> ticketTypes = ticketTypeRepository.findByEvent_EventId(event.getEventId());

        // Generate tickets based on order quantity
        // Note: This is simplified - in real implementation, you'd track which ticket types were ordered
        int totalGenerated = 0;
        for (TicketType ticketType : ticketTypes) {
            if (totalGenerated >= order.getTotalQuantity()) {
                break;
            }

            int quantityForType = Math.min(
                ticketType.getTicketQuantity() - ticketType.getAvailableQuantity(),
                order.getTotalQuantity() - totalGenerated
            );

            for (int i = 0; i < quantityForType; i++) {
                // FR6: Generate unique QR code
                String qrCode = generateUniqueQRCode(order, ticketType);
                
                Ticket ticket = new Ticket(order, ticketType, qrCode);
                tickets.add(ticketRepository.save(ticket));
                totalGenerated++;
            }
        }

        return tickets;
    }

    /**
     * FR6: Generate unique QR code
     */
    private String generateUniqueQRCode(Order order, TicketType ticketType) {
        String qrCode;
        do {
            qrCode = "TKT-" + order.getOrderId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (ticketRepository.existsByQrCode(qrCode));
        
        return qrCode;
    }

    /**
     * UC-02.1: Cancel Order
     * Releases reserved tickets
     */
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Verify order belongs to user
        if (!order.getUser().getUserId().equals(userId)) {
            throw new BusinessRuleViolationException("Order does not belong to this user");
        }

        if (order.getBookingStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessRuleViolationException("Only pending orders can be cancelled");
        }

        cancelExpiredOrder(order);
    }

    /**
     * Cancel expired order and release tickets
     */
    private void cancelExpiredOrder(Order order) {
        order.cancel();
        orderRepository.save(order);

        // Release reserved tickets
        releaseTicketsForOrder(order);
    }

    /**
     * Release reserved tickets back to available inventory
     */
    private void releaseTicketsForOrder(Order order) {
        List<TicketType> ticketTypes = ticketTypeRepository.findByEvent_EventId(
            order.getEvent().getEventId()
        );

        // Release tickets (simplified logic)
        int totalToRelease = order.getTotalQuantity();
        for (TicketType ticketType : ticketTypes) {
            if (totalToRelease <= 0) break;
            
            int releaseQuantity = Math.min(
                totalToRelease,
                ticketType.getTicketQuantity() - ticketType.getAvailableQuantity()
            );
            
            ticketType.release(releaseQuantity);
            ticketTypeRepository.save(ticketType);
            totalToRelease -= releaseQuantity;
        }
    }

    /**
     * UC-02.2: View Purchased Tickets
     * Get all orders for a customer
     */
    public List<OrderResponse> getCustomerOrders(Long userId) {
        List<Order> orders = orderRepository.findByUser_UserIdOrderByOrderDateDesc(userId);
        return orders.stream()
            .map(this::mapToOrderResponse)
            .toList();
    }

    /**
     * UC-02.2: Get Order Details with Tickets
     */
    public OrderResponse getOrderDetails(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Verify order belongs to user
        if (!order.getUser().getUserId().equals(userId)) {
            throw new BusinessRuleViolationException("Order does not belong to this user");
        }

        return mapToOrderResponse(order);
    }

    /**
     * FR16: Background job to auto-cancel expired orders
     * Runs every minute
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    @Transactional
    public void cleanupExpiredOrders() {
        List<Order> expiredOrders = orderRepository.findExpiredPendingOrders(LocalDateTime.now());
        
        for (Order order : expiredOrders) {
            order.expire();
            orderRepository.save(order);
            releaseTicketsForOrder(order);
        }
    }

    /**
     * Map Order entity to OrderResponse DTO
     */
    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setEventId(order.getEvent().getEventId());
        response.setEventName(order.getEvent().getName());
        response.setBookingStatus(order.getBookingStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setTotalQuantity(order.getTotalQuantity());
        response.setOrderDate(order.getOrderDate());
        response.setReservationExpiresAt(order.getReservationExpiresAt());
        response.setCompletedAt(order.getCompletedAt());
        
        // Recipient info (FR25)
        response.setRecipientName(order.getRecipientName());
        response.setRecipientPhone(order.getRecipientPhone());
        response.setRecipientEmail(order.getRecipientEmail());
        response.setRecipientAddress(order.getRecipientAddress());
        response.setRecipientNotes(order.getRecipientNotes());

        // Calculate remaining seconds for countdown
        if (order.getReservationExpiresAt() != null && 
            order.getBookingStatus() == BookingStatus.PENDING_PAYMENT) {
            Duration duration = Duration.between(LocalDateTime.now(), order.getReservationExpiresAt());
            response.setRemainingSeconds(Math.max(0, duration.getSeconds()));
        }

        // Include tickets if confirmed
        if (order.getBookingStatus() == BookingStatus.CONFIRMED) {
            List<Ticket> tickets = ticketRepository.findByOrder_OrderId(order.getOrderId());
            response.setTickets(tickets.stream().map(this::mapToTicketResponse).toList());
        }

        return response;
    }

    /**
     * Map Ticket entity to TicketResponse DTO
     */
    private TicketResponse mapToTicketResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setTicketId(ticket.getTicketId());
        response.setQrCode(ticket.getQrCode());
        response.setTicketTypeName(ticket.getTicketType().getTypeName());
        response.setPrice(ticket.getTicketType().getPrice());
        response.setSeatNumber(ticket.getSeatNumber());
        response.setStatus(ticket.getStatus());
        response.setCheckInDateTime(ticket.getCheckInDateTime());
        response.setCreatedAt(ticket.getCreatedAt());
        return response;
    }
}

