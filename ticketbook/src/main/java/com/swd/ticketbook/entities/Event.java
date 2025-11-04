package com.swd.ticketbook.entities;

import com.swd.ticketbook.enums.EventStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing an Event
 * Business Rules: FR2, FR15
 */
@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @NotNull
    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "location", length = 500)
    private String location;

    @Column(name = "max_ticket_quantity")
    private Integer maxTicketQuantity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatus status = EventStatus.DRAFT;

    @Column(name = "poster_image", length = 500)
    private String posterImage;

    @Column(name = "organizer_committee", length = 1000)
    private String organizerCommittee;

    @Column(name = "venue_name", length = 255)
    private String venueName;

    @Column(name = "refund_allowed", nullable = false)
    private Boolean refundAllowed = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TicketType> ticketTypes = new HashSet<>();

    public Event(User organizer, String name, LocalDateTime startDate, LocalDateTime endDate) {
        this.organizer = organizer;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = LocalDateTime.now();
        this.status = EventStatus.DRAFT;
    }

    /**
     * Check if event has already occurred (FR15)
     */
    public boolean hasOccurred() {
        return LocalDateTime.now().isAfter(endDate);
    }

    /**
     * Check if event is currently happening
     */
    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startDate) && now.isBefore(endDate);
    }
}

