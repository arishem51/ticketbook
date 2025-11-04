package com.swd.ticketbook.services;

import com.swd.ticketbook.dto.organizer.EventStatisticsResponse;
import com.swd.ticketbook.entities.*;
import com.swd.ticketbook.enums.BookingStatus;
import com.swd.ticketbook.enums.TicketStatus;
import com.swd.ticketbook.exceptions.BusinessRuleViolationException;
import com.swd.ticketbook.exceptions.ResourceNotFoundException;
import com.swd.ticketbook.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Event Statistics (UC-03.3)
 * Business Rules: FR17, FR26
 */
@Service
public class StatisticsService {

    private static final Logger log = LoggerFactory.getLogger(StatisticsService.class);
    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.05"); // 5% platform fee

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private RefundInfoRepository refundRepository;

    /**
     * UC-03.3: Get event statistics
     * FR17, FR26: Only Verified Organizer can view their event stats
     */
    public EventStatisticsResponse getEventStatistics(Long organizerId, Long eventId) {
        // Get event
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // Verify organizer owns the event
        if (!event.getOrganizer().getUserId().equals(organizerId)) {
            throw new BusinessRuleViolationException("You do not have permission to view this event's statistics");
        }

        EventStatisticsResponse stats = new EventStatisticsResponse();
        stats.setEventId(eventId);
        stats.setEventName(event.getName());

        // Get all ticket types for this event
        List<TicketType> ticketTypes = ticketTypeRepository.findByEvent_EventId(eventId);

        // Calculate total tickets
        int totalTickets = ticketTypes.stream()
            .mapToInt(TicketType::getTicketQuantity)
            .sum();
        int availableTickets = ticketTypes.stream()
            .mapToInt(TicketType::getAvailableQuantity)
            .sum();
        int soldTickets = totalTickets - availableTickets;

        stats.setTotalTicketsAvailable(totalTickets);
        stats.setTotalTicketsSold(soldTickets);

        // Get all confirmed orders for this event
        List<Order> orders = orderRepository.findByEvent_EventId(eventId).stream()
            .filter(o -> o.getBookingStatus() == BookingStatus.CONFIRMED)
            .collect(Collectors.toList());

        // Calculate revenue
        BigDecimal totalRevenue = orders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal platformFee = totalRevenue.multiply(PLATFORM_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netRevenue = totalRevenue.subtract(platformFee);

        stats.setTotalRevenue(totalRevenue);
        stats.setPlatformFee(platformFee);
        stats.setNetRevenue(netRevenue);

        // Check-in statistics
        List<Ticket> tickets = ticketRepository.findByOrder_Event_EventId(eventId);
        long checkedInCount = tickets.stream()
            .filter(t -> t.getStatus() == TicketStatus.USED)
            .count();

        stats.setTotalCheckedIn((int) checkedInCount);
        if (soldTickets > 0) {
            double checkInRate = (checkedInCount * 100.0) / soldTickets;
            stats.setCheckInRate(Math.round(checkInRate * 100.0) / 100.0);
        } else {
            stats.setCheckInRate(0.0);
        }

        // Ticket type breakdown
        List<EventStatisticsResponse.TicketTypeSales> ticketTypeSales = new ArrayList<>();
        for (TicketType tt : ticketTypes) {
            EventStatisticsResponse.TicketTypeSales ttSales = new EventStatisticsResponse.TicketTypeSales();
            ttSales.setTypeName(tt.getTypeName());
            ttSales.setPrice(tt.getPrice());
            ttSales.setTotalQuantity(tt.getTicketQuantity());
            ttSales.setAvailable(tt.getAvailableQuantity());
            ttSales.setSold(tt.getTicketQuantity() - tt.getAvailableQuantity());
            
            BigDecimal typeRevenue = tt.getPrice()
                .multiply(new BigDecimal(ttSales.getSold()))
                .setScale(2, RoundingMode.HALF_UP);
            ttSales.setRevenue(typeRevenue);
            
            ticketTypeSales.add(ttSales);
        }
        stats.setTicketTypeSales(ticketTypeSales);

        // Sales by day
        Map<String, Integer> salesByDay = new LinkedHashMap<>();
        Map<String, BigDecimal> revenueByDay = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Order order : orders) {
            String day = order.getOrderDate().format(formatter);
            salesByDay.put(day, salesByDay.getOrDefault(day, 0) + 1);
            revenueByDay.put(day, 
                revenueByDay.getOrDefault(day, BigDecimal.ZERO).add(order.getTotalAmount()));
        }

        stats.setSalesByDay(salesByDay);
        stats.setRevenueByDay(revenueByDay);

        // Refund statistics
        List<RefundInfo> refunds = refundRepository.findByTicket_Order_Event_EventId(eventId);
        int totalRefunds = refunds.size();
        BigDecimal totalRefundAmount = refunds.stream()
            .map(RefundInfo::getRefundAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.setTotalRefunds(totalRefunds);
        stats.setTotalRefundAmount(totalRefundAmount);

        // Note: Demographics would require additional user data fields
        // Placeholder for future implementation
        stats.setSalesByRegion(new HashMap<>());

        log.info("Statistics generated for event {} - Sold: {}, Revenue: {}", 
                 eventId, soldTickets, totalRevenue);

        return stats;
    }

    /**
     * Get summary statistics for all organizer's events
     */
    public Map<String, Object> getOrganizerSummary(Long organizerId) {
        List<Event> events = eventRepository.findByOrganizer_UserIdOrderByCreatedAtDesc(organizerId);

        int totalEvents = events.size();
        int activeEvents = (int) events.stream()
            .filter(e -> e.getStatus() == com.swd.ticketbook.enums.EventStatus.ACTIVE)
            .count();

        // Aggregate statistics across all events
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalTicketsSold = 0;

        for (Event event : events) {
            List<Order> orders = orderRepository.findByEvent_EventId(event.getEventId()).stream()
                .filter(o -> o.getBookingStatus() == BookingStatus.CONFIRMED)
                .collect(Collectors.toList());

            totalRevenue = totalRevenue.add(
                orders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
            );

            List<TicketType> ticketTypes = ticketTypeRepository.findByEvent_EventId(event.getEventId());
            totalTicketsSold += ticketTypes.stream()
                .mapToInt(tt -> tt.getTicketQuantity() - tt.getAvailableQuantity())
                .sum();
        }

        BigDecimal platformFee = totalRevenue.multiply(PLATFORM_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netRevenue = totalRevenue.subtract(platformFee);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalEvents", totalEvents);
        summary.put("activeEvents", activeEvents);
        summary.put("totalRevenue", totalRevenue);
        summary.put("platformFee", platformFee);
        summary.put("netRevenue", netRevenue);
        summary.put("totalTicketsSold", totalTicketsSold);

        return summary;
    }
}

