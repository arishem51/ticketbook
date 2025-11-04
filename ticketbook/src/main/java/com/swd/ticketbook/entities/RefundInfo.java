package com.swd.ticketbook.entities;

import com.swd.ticketbook.enums.RefundStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a Refund Request
 * Business Rule FR7, FR8
 */
@Entity
@Table(name = "refund_info")
@Data
@NoArgsConstructor
public class RefundInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RefundStatus status = RefundStatus.PENDING_ADMIN_REVIEW;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "admin_notes", length = 1000)
    private String adminNotes;

    @Column(name = "approved_by")
    private Long approvedBy; // Admin user ID

    @NotNull
    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    public RefundInfo(Ticket ticket, User user, String reason, BigDecimal refundAmount) {
        this.ticket = ticket;
        this.user = user;
        this.reason = reason;
        this.refundAmount = refundAmount;
        this.requestDate = LocalDateTime.now();
        this.status = RefundStatus.PENDING_ADMIN_REVIEW;
    }

    /**
     * Approve refund request
     */
    public void approve(Long adminId, String notes) {
        this.status = RefundStatus.APPROVED_PROCESSING;
        this.approvedBy = adminId;
        this.adminNotes = notes;
    }

    /**
     * Reject refund request
     */
    public void reject(String notes) {
        this.status = RefundStatus.REJECTED;
        this.adminNotes = notes;
        this.processedDate = LocalDateTime.now();
    }

    /**
     * Mark as completed
     */
    public void complete() {
        this.status = RefundStatus.COMPLETED;
        this.processedDate = LocalDateTime.now();
    }
}

