package com.swd.ticketbook.dto.checkin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for check-in request (UC-02.3)
 */
@Data
public class CheckInRequest {
    
    @NotBlank(message = "QR code is required")
    private String qrCode;
}

