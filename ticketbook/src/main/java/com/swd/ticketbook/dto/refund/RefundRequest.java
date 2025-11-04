package com.swd.ticketbook.dto.refund;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for refund request submission (UC-02.5)
 */
@Data
public class RefundRequest {
    
    @NotNull(message = "Ticket ID is required")
    private Long ticketId;
    
    @NotBlank(message = "Refund reason is required")
    private String reason;
}

