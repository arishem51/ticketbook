package com.swd.ticketbook.enums;

/**
 * Enum for booking/order status
 * Business Rule FR5, FR16
 */
public enum BookingStatus {
    PENDING_PAYMENT("Pending Payment"),
    CONFIRMED("Confirmed"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed");

    private final String displayName;

    BookingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

