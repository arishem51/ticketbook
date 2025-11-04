package com.swd.ticketbook.dto.support;

import com.swd.ticketbook.enums.SupportTicketStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for support ticket response
 */
@Data
public class SupportResponse {
    private Long ticketId;
    private Long eventId;
    private String eventName;
    private String subject;
    private String description;
    private String category;
    private SupportTicketStatus status;
    private String organizerName;
    private String organizerResponse;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}

