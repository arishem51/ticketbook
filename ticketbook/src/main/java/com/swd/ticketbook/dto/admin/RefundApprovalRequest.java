package com.swd.ticketbook.dto.admin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO for Admin approving or rejecting refund request
 * Business Rule: FR7, FR22, FR23
 */
public class RefundApprovalRequest {

    @NotNull(message = "Approval decision is required")
    private Boolean approved;

    @DecimalMin(value = "0.0", message = "Refund amount must be positive")
    private BigDecimal refundAmount; // For partial refunds

    @NotBlank(message = "Admin notes are required")
    private String adminNotes;

    // Constructors
    public RefundApprovalRequest() {
    }

    public RefundApprovalRequest(Boolean approved, BigDecimal refundAmount, String adminNotes) {
        this.approved = approved;
        this.refundAmount = refundAmount;
        this.adminNotes = adminNotes;
    }

    // Getters and Setters
    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
}

