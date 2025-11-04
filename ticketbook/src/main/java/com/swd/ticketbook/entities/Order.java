package com.swd.ticketbook.entities;

import com.swd.ticketbook.enums.BookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a Customer Order
 * Business Rules: FR4, FR5, FR14, FR16, FR25
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @NotNull
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @NotNull
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private BookingStatus bookingStatus = BookingStatus.PENDING_PAYMENT;

    // FR25: Recipient information
    @Column(name = "recipient_name", length = 255)
    private String recipientName;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @Column(name = "recipient_email", length = 255)
    private String recipientEmail;

    @Column(name = "recipient_address", length = 500)
    private String recipientAddress;

    @Column(name = "recipient_notes", length = 1000)
    private String recipientNotes;

    // FR16: Reservation timeout tracking
    @Column(name = "reservation_expires_at")
    private LocalDateTime reservationExpiresAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Relationships
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Ticket> tickets = new HashSet<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    public Order(User user, Event event) {
        this.user = user;
        this.event = event;
        this.orderDate = LocalDateTime.now();
        this.bookingStatus = BookingStatus.PENDING_PAYMENT;
        // FR16: 15-minute reservation from entering payment screen
        this.reservationExpiresAt = LocalDateTime.now().plusMinutes(15);
    }

    /**
     * Check if reservation has expired (FR16)
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(reservationExpiresAt);
    }

    /**
     * Extend reservation timeout
     */
    public void extendReservation() {
        this.reservationExpiresAt = LocalDateTime.now().plusMinutes(15);
    }

    /**
     * Confirm order after payment
     */
    public void confirm() {
        this.bookingStatus = BookingStatus.CONFIRMED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Cancel order
     */
    public void cancel() {
        this.bookingStatus = BookingStatus.CANCELLED;
    }

    /**
     * Mark as expired
     */
    public void expire() {
        this.bookingStatus = BookingStatus.EXPIRED;
    }
}

