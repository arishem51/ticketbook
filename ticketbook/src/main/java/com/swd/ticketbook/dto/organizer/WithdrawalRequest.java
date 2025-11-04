package com.swd.ticketbook.dto.organizer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO for revenue withdrawal request (UC-03.4)
 * Business Rules: FR17, FR26
 */
public class WithdrawalRequest {

    @NotNull(message = "Withdrawal amount is required")
    @DecimalMin(value = "50.0", message = "Minimum withdrawal amount is $50")
    private BigDecimal amount;

    private Long eventId; // Optional - specific event or all events

    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}

