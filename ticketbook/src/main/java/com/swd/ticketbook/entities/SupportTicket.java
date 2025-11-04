package com.swd.ticketbook.entities;

import com.swd.ticketbook.enums.SupportTicketStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a Support Ticket/Request
 * Used in UC-02.4
 */
@Entity
@Table(name = "support_tickets")
@Data
@NoArgsConstructor
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @NotNull
    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @NotNull
    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @NotNull
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SupportTicketStatus status = SupportTicketStatus.PENDING;

    @Column(name = "organizer_response", columnDefinition = "TEXT")
    private String organizerResponse;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public SupportTicket(User user, Event event, String category, String subject, String description) {
        this.user = user;
        this.event = event;
        this.category = category;
        this.subject = subject;
        this.description = description;
        this.status = SupportTicketStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Update status to in progress
     */
    public void markInProgress() {
        this.status = SupportTicketStatus.IN_PROGRESS;
    }

    /**
     * Resolve ticket
     */
    public void resolve(String response) {
        this.status = SupportTicketStatus.RESOLVED;
        this.organizerResponse = response;
        this.resolvedAt = LocalDateTime.now();
    }

    /**
     * Close ticket
     */
    public void close() {
        this.status = SupportTicketStatus.CLOSED;
        if (this.resolvedAt == null) {
            this.resolvedAt = LocalDateTime.now();
        }
    }
}

