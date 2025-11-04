package com.swd.ticketbook.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a Revenue Withdrawal Request by Organizer
 * Business Rules: FR17, FR26
 * UC-03.4: Organizers request withdrawal, Admin approves
 */
@Entity
@Table(name = "withdrawal_requests")
@Data
@NoArgsConstructor
public class WithdrawalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event; // Optional - can be for specific event or all events

    @NotNull
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "available_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal availableBalance; // Balance at time of request

    @Column(name = "platform_fee", precision = 10, scale = 2)
    private BigDecimal platformFee;

    @NotNull
    @Column(name = "bank_name", nullable = false, length = 255)
    private String bankName;

    @NotNull
    @Column(name = "bank_account_number", nullable = false, length = 100)
    private String bankAccountNumber;

    @NotNull
    @Column(name = "bank_account_holder", nullable = false, length = 255)
    private String bankAccountHolder;

    @Column(name = "bank_branch", length = 255)
    private String bankBranch;

    @NotNull
    @Column(name = "status", length = 50, nullable = false)
    private String status = "PENDING_REVIEW"; // PENDING_REVIEW, APPROVED, PROCESSING, COMPLETED, REJECTED

    @NotNull
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by")
    private Long reviewedBy; // Admin user ID

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "transaction_reference", length = 255)
    private String transactionReference;

    // Constructor
    public WithdrawalRequest(User organizer, BigDecimal amount, BigDecimal availableBalance) {
        this.organizer = organizer;
        this.amount = amount;
        this.availableBalance = availableBalance;
        this.requestedAt = LocalDateTime.now();
        this.status = "PENDING_REVIEW";
    }

    // Business methods
    public void approve(Long adminId, String notes) {
        this.status = "APPROVED";
        this.reviewedAt = LocalDateTime.now();
        this.reviewedBy = adminId;
        this.adminNotes = notes;
    }

    public void reject(Long adminId, String notes) {
        this.status = "REJECTED";
        this.reviewedAt = LocalDateTime.now();
        this.reviewedBy = adminId;
        this.adminNotes = notes;
    }

    public void markAsProcessing() {
        this.status = "PROCESSING";
    }

    public void complete(String transactionRef) {
        this.status = "COMPLETED";
        this.processedAt = LocalDateTime.now();
        this.transactionReference = transactionRef;
    }

    public boolean isPending() {
        return "PENDING_REVIEW".equals(status);
    }
}

