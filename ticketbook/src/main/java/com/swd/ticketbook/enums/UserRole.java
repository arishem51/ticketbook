package com.swd.ticketbook.enums;

/**
 * Enum representing user roles in the system.
 * Business Rule FR11: Account Role Assignment
 * - CUSTOMER: Default role assigned during registration
 * - VERIFIED_ORGANIZER: Assigned after Admin approves KYC verification (FR26)
 * - ADMIN: Assigned only by system administrator
 */
public enum UserRole {
    CUSTOMER("Customer"),
    VERIFIED_ORGANIZER("Verified Organizer"),
    ADMIN("Admin");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

