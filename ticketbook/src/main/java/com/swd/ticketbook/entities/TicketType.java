package com.swd.ticketbook.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing Ticket Type for an Event
 * Business Rule FR3, FR14
 */
@Entity
@Table(name = "ticket_types")
@Data
@NoArgsConstructor
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_type_id")
    private Long ticketTypeId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @NotNull
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull
    @Column(name = "ticket_quantity", nullable = false)
    private Integer ticketQuantity;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @NotNull
    @Column(name = "type_name", nullable = false, length = 255)
    private String typeName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "sale_start_date")
    private LocalDateTime saleStartDate;

    @Column(name = "sale_end_date")
    private LocalDateTime saleEndDate;

    public TicketType(Event event, String typeName, BigDecimal price, Integer ticketQuantity) {
        this.event = event;
        this.typeName = typeName;
        this.price = price;
        this.ticketQuantity = ticketQuantity;
        this.availableQuantity = ticketQuantity;
    }

    /**
     * Check if tickets are available for purchase
     */
    public boolean isAvailable() {
        return availableQuantity > 0;
    }

    /**
     * Reserve tickets (FR14, FR16)
     */
    public void reserve(int quantity) {
        if (quantity > availableQuantity) {
            throw new IllegalStateException("Insufficient tickets available");
        }
        this.availableQuantity -= quantity;
    }

    /**
     * Release reserved tickets (when order expires)
     */
    public void release(int quantity) {
        this.availableQuantity += quantity;
        if (this.availableQuantity > this.ticketQuantity) {
            this.availableQuantity = this.ticketQuantity;
        }
    }
}

