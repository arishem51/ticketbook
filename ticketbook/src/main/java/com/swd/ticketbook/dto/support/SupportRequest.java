package com.swd.ticketbook.dto.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for support request submission (UC-02.4)
 */
@Data
public class SupportRequest {
    
    @NotNull(message = "Event ID is required")
    private Long eventId;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotBlank(message = "Category is required")
    private String category; // "Ticket Issue", "Event Question", "Check-in Problem", "Other"
    
    private Long relatedOrderId; // Optional: if related to specific order
}

