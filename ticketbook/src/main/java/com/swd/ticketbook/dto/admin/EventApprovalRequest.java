package com.swd.ticketbook.dto.admin;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for Admin approving or rejecting event (UC-04.6)
 * Business Rule: FR22, FR23
 */
public class EventApprovalRequest {

    private boolean approved;

    @NotBlank(message = "Admin notes are required")
    private String adminNotes;

    // Constructors
    public EventApprovalRequest() {
    }

    public EventApprovalRequest(boolean approved, String adminNotes) {
        this.approved = approved;
        this.adminNotes = adminNotes;
    }

    // Getters and Setters
    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
}

