package com.swd.ticketbook.services;

import com.swd.ticketbook.dto.support.SupportRequest;
import com.swd.ticketbook.dto.support.SupportResponse;
import com.swd.ticketbook.entities.Event;
import com.swd.ticketbook.entities.Order;
import com.swd.ticketbook.entities.SupportTicket;
import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.enums.SupportTicketStatus;
import com.swd.ticketbook.exceptions.BusinessRuleViolationException;
import com.swd.ticketbook.exceptions.ResourceNotFoundException;
import com.swd.ticketbook.repositories.EventRepository;
import com.swd.ticketbook.repositories.OrderRepository;
import com.swd.ticketbook.repositories.SupportTicketRepository;
import com.swd.ticketbook.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Support Ticket operations (UC-02.4)
 * Business Rules: FR22
 */
@Service
public class SupportService {

    private static final Logger log = LoggerFactory.getLogger(SupportService.class);

    @Autowired
    private SupportTicketRepository supportRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmailService emailService;

    /**
     * UC-02.4: Submit Support Request to Event Organizer
     * FR22: Validate required fields
     */
    @Transactional
    public SupportResponse submitSupportRequest(Long userId, SupportRequest request) {
        // Get user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get event
        Event event = eventRepository.findById(request.getEventId())
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // FR22: Validate required fields
        if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
            throw new BusinessRuleViolationException("Subject is required");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new BusinessRuleViolationException("Description is required");
        }
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new BusinessRuleViolationException("Category is required");
        }

        // Validate category
        List<String> validCategories = List.of(
            "Ticket Issue", 
            "Event Question", 
            "Check-in Problem", 
            "Other"
        );
        if (!validCategories.contains(request.getCategory())) {
            throw new BusinessRuleViolationException("Invalid category");
        }

        // Create support ticket
        SupportTicket ticket = new SupportTicket();
        ticket.setUser(user);
        ticket.setEvent(event);
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setStatus(SupportTicketStatus.PENDING);
        ticket.setCreatedAt(LocalDateTime.now());
        
        // Set order if related to specific order
        if (request.getRelatedOrderId() != null) {
            Order order = orderRepository.findById(request.getRelatedOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            // Verify order belongs to user
            if (!order.getUser().getUserId().equals(userId)) {
                throw new BusinessRuleViolationException("Order does not belong to this user");
            }
            ticket.setOrder(order);
        }

        ticket = supportRepository.save(ticket);

        // Notify organizer
        User organizer = event.getOrganizer();
        emailService.sendNewSupportTicketNotification(
            organizer.getContact(),
            ticket.getRequestId(),
            event.getName(),
            request.getSubject(),
            user.getFullName()
        );

        // Send confirmation to customer
        emailService.sendSupportRequestConfirmation(
            user.getContact(),
            ticket.getRequestId(),
            event.getName()
        );

        log.info("Support ticket created - ID: {}, User: {}, Event: {}, Category: {}",
            ticket.getRequestId(), userId, event.getEventId(), request.getCategory());

        return mapToSupportResponse(ticket);
    }

    /**
     * UC-02.4: View Customer's Support Requests
     */
    public List<SupportResponse> getCustomerSupportRequests(Long userId) {
        List<SupportTicket> tickets = supportRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
        return tickets.stream()
            .map(this::mapToSupportResponse)
            .toList();
    }

    /**
     * UC-02.4: View Support Request Details
     */
    public SupportResponse getSupportRequestDetails(Long userId, Long ticketId) {
        SupportTicket ticket = supportRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found"));

        // Verify ticket belongs to user
        if (!ticket.getUser().getUserId().equals(userId)) {
            throw new BusinessRuleViolationException("Support ticket does not belong to this user");
        }

        return mapToSupportResponse(ticket);
    }

    /**
     * Organizer: View Support Requests for Their Events
     */
    public List<SupportResponse> getOrganizerSupportRequests(Long organizerId) {
        List<SupportTicket> tickets = supportRepository.findByEvent_Organizer_UserIdOrderByCreatedAtAsc(organizerId);
        return tickets.stream()
            .map(this::mapToSupportResponse)
            .toList();
    }

    /**
     * Organizer: Respond to Support Request
     */
    @Transactional
    public SupportResponse respondToSupportRequest(Long organizerId, Long ticketId, String response) {
        SupportTicket ticket = supportRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found"));

        // Verify ticket is for organizer's event
        if (!ticket.getEvent().getOrganizer().getUserId().equals(organizerId)) {
            throw new BusinessRuleViolationException("Support ticket does not belong to your event");
        }

        // Update ticket with organizer response
        ticket.setOrganizerResponse(response);
        ticket.setStatus(SupportTicketStatus.IN_PROGRESS);
        ticket.setRespondedAt(LocalDateTime.now());
        supportRepository.save(ticket);

        // Notify customer
        emailService.sendSupportResponseNotification(
            ticket.getUser().getContact(),
            ticket.getRequestId(),
            ticket.getEvent().getName(),
            response
        );

        log.info("Support ticket responded - ID: {}, Organizer: {}", ticketId, organizerId);

        return mapToSupportResponse(ticket);
    }

    /**
     * Organizer: Mark Support Request as Resolved
     */
    @Transactional
    public SupportResponse markAsResolved(Long organizerId, Long ticketId) {
        SupportTicket ticket = supportRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found"));

        // Verify ticket is for organizer's event
        if (!ticket.getEvent().getOrganizer().getUserId().equals(organizerId)) {
            throw new BusinessRuleViolationException("Support ticket does not belong to your event");
        }

        ticket.setStatus(SupportTicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());
        supportRepository.save(ticket);

        // Notify customer
        emailService.sendSupportResolvedNotification(
            ticket.getUser().getContact(),
            ticket.getRequestId(),
            ticket.getEvent().getName()
        );

        log.info("Support ticket resolved - ID: {}, Organizer: {}", ticketId, organizerId);

        return mapToSupportResponse(ticket);
    }

    /**
     * Customer: Confirm Resolution and Close Ticket
     */
    @Transactional
    public SupportResponse confirmResolution(Long userId, Long ticketId) {
        SupportTicket ticket = supportRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found"));

        // Verify ticket belongs to user
        if (!ticket.getUser().getUserId().equals(userId)) {
            throw new BusinessRuleViolationException("Support ticket does not belong to this user");
        }

        if (ticket.getStatus() != SupportTicketStatus.RESOLVED) {
            throw new BusinessRuleViolationException("Support ticket is not in resolved status");
        }

        ticket.setStatus(SupportTicketStatus.CLOSED);
        supportRepository.save(ticket);

        log.info("Support ticket closed - ID: {}, User: {}", ticketId, userId);

        return mapToSupportResponse(ticket);
    }

    /**
     * Map SupportTicket entity to SupportResponse DTO
     */
    private SupportResponse mapToSupportResponse(SupportTicket ticket) {
        SupportResponse response = new SupportResponse();
        response.setTicketId(ticket.getRequestId()); // requestId is the ID field
        response.setEventId(ticket.getEvent().getEventId());
        response.setEventName(ticket.getEvent().getName());
        response.setSubject(ticket.getSubject());
        response.setDescription(ticket.getDescription());
        response.setCategory(ticket.getCategory());
        response.setStatus(ticket.getStatus());
        response.setOrganizerName(ticket.getEvent().getOrganizer().getFullName());
        response.setOrganizerResponse(ticket.getOrganizerResponse());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setResolvedAt(ticket.getResolvedAt());
        return response;
    }
}

