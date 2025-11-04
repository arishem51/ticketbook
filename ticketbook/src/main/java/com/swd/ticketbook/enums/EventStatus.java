package com.swd.ticketbook.enums;

/**
 * Enum for event status
 */
public enum EventStatus {
    DRAFT("Draft"),
    PENDING_APPROVAL("Pending Approval"),
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed");

    private final String displayName;

    EventStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

