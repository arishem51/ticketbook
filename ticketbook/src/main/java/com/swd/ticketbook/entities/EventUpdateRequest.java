package com.swd.ticketbook.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing an Event Update Request by Organizer
 * Business Rules: FR3, FR15, FR17, FR26
 * UC-03.2: Organizers request updates, Admin approves
 */
@Entity
@Table(name = "event_update_requests")
@Data
@NoArgsConstructor
public class EventUpdateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @NotNull
    @Column(name = "justification", columnDefinition = "TEXT", nullable = false)
    private String justification;

    // Proposed changes (stored as JSON-like text or individual fields)
    @Column(name = "proposed_name", length = 500)
    private String proposedName;

    @Column(name = "proposed_description", columnDefinition = "TEXT")
    private String proposedDescription;

    @Column(name = "proposed_location", length = 500)
    private String proposedLocation;

    @Column(name = "proposed_venue_name", length = 255)
    private String proposedVenueName;

    @Column(name = "proposed_start_date")
    private LocalDateTime proposedStartDate;

    @Column(name = "proposed_end_date")
    private LocalDateTime proposedEndDate;

    @Column(name = "proposed_max_ticket_quantity")
    private Integer proposedMaxTicketQuantity;

    @Column(name = "proposed_refund_allowed")
    private Boolean proposedRefundAllowed;

    @NotNull
    @Column(name = "status", length = 50, nullable = false)
    private String status = "PENDING_REVIEW"; // PENDING_REVIEW, APPROVED, REJECTED

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by")
    private Long reviewedBy; // Admin user ID

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    // Constructor
    public EventUpdateRequest(Event event, User organizer, String justification) {
        this.event = event;
        this.organizer = organizer;
        this.justification = justification;
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

    public boolean isPending() {
        return "PENDING_REVIEW".equals(status);
    }
}

