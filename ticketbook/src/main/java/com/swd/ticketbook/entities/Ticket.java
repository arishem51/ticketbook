package com.swd.ticketbook.entities;

import com.swd.ticketbook.enums.TicketStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing an individual Ticket
 * Business Rules: FR6, FR8
 */
@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;

    @NotNull
    @Column(name = "qr_code", nullable = false, unique = true, length = 255)
    private String qrCode; // FR6: Unique QR code, auto-generated

    @Column(name = "seat_number", length = 50)
    private String seatNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status = TicketStatus.CONFIRMED;

    @Column(name = "check_in_date_time")
    private LocalDateTime checkInDateTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Ticket(Order order, TicketType ticketType, String qrCode) {
        this.order = order;
        this.ticketType = ticketType;
        this.qrCode = qrCode;
        this.status = TicketStatus.CONFIRMED;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Check-in the ticket (FR6)
     */
    public void checkIn() {
        if (this.status == TicketStatus.USED) {
            throw new IllegalStateException("Ticket already used");
        }
        if (this.status == TicketStatus.REFUNDED) {
            throw new IllegalStateException("Ticket has been refunded");
        }
        this.status = TicketStatus.USED;
        this.checkInDateTime = LocalDateTime.now();
    }

    /**
     * Mark ticket as refunded (FR8)
     */
    public void refund() {
        this.status = TicketStatus.REFUNDED;
    }

    /**
     * Check if ticket can be used for check-in
     */
    public boolean canCheckIn() {
        return this.status == TicketStatus.CONFIRMED && this.checkInDateTime == null;
    }
}

