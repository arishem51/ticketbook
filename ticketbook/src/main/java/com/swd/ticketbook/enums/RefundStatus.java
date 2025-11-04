package com.swd.ticketbook.enums;

/**
 * Enum for refund request status
 * Business Rule FR7
 */
public enum RefundStatus {
    PENDING_ADMIN_REVIEW("Pending Admin Review"),
    APPROVED_PROCESSING("Approved - Processing"),
    COMPLETED("Completed"),
    REJECTED("Rejected");

    private final String displayName;

    RefundStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

