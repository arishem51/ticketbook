package com.swd.ticketbook.dto.order;

import com.swd.ticketbook.enums.TicketStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for ticket response
 */
@Data
public class TicketResponse {
    private Long ticketId;
    private String qrCode;
    private String ticketTypeName;
    private BigDecimal price;
    private String seatNumber;
    private TicketStatus status;
    private LocalDateTime checkInDateTime;
    private LocalDateTime createdAt;
}

