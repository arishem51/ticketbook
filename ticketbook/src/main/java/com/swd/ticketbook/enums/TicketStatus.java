package com.swd.ticketbook.enums;

/**
 * Enum for ticket status
 * Business Rule FR6, FR8
 */
public enum TicketStatus {
    AVAILABLE("Available"),
    CONFIRMED("Confirmed"),
    USED("Used"),
    REFUNDED("Refunded"),
    CANCELLED("Cancelled");

    private final String displayName;

    TicketStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

