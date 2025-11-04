package com.swd.ticketbook.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for individual order item (ticket type + quantity)
 */
@Data
public class OrderItemRequest {
    
    @NotNull(message = "Ticket type ID is required")
    private Long ticketTypeId;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    private String seatNumber; // Optional: for assigned seating
}

