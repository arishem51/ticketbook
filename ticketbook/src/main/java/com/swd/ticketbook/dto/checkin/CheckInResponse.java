package com.swd.ticketbook.dto.checkin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for check-in response (UC-02.3)
 */
@Data
public class CheckInResponse {
    private boolean success;
    private String message;
    private Long ticketId;
    private String customerName;
    private String eventName;
    private String ticketTypeName;
    private String seatNumber;
    private LocalDateTime checkInDateTime;
}

