package com.swd.ticketbook.dto.order;

import com.swd.ticketbook.enums.BookingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for order response
 */
@Data
public class OrderResponse {
    private Long orderId;
    private Long eventId;
    private String eventName;
    private BookingStatus bookingStatus;
    private BigDecimal totalAmount;
    private Integer totalQuantity;
    private LocalDateTime orderDate;
    private LocalDateTime reservationExpiresAt;
    private LocalDateTime completedAt;
    
    // Recipient info (FR25)
    private String recipientName;
    private String recipientPhone;
    private String recipientEmail;
    private String recipientAddress;
    private String recipientNotes;
    
    private List<TicketResponse> tickets;
    private Long remainingSeconds; // For reservation countdown
}

