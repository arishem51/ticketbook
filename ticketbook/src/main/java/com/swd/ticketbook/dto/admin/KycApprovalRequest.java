package com.swd.ticketbook.dto.admin;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for Admin approving or rejecting KYC verification
 * Business Rule: FR26, FR22, FR23
 */
public class KycApprovalRequest {

    private boolean approved;

    @NotBlank(message = "Reason/notes are required")
    private String notes;

    // Constructors
    public KycApprovalRequest() {
    }

    public KycApprovalRequest(boolean approved, String notes) {
        this.approved = approved;
        this.notes = notes;
    }

    // Getters and Setters
    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

