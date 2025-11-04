package com.swd.ticketbook.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * DTO for creating a new order (UC-02.1)
 */
@Data
public class CreateOrderRequest {
    
    @NotNull(message = "Event ID is required")
    private Long eventId;
    
    @NotEmpty(message = "At least one ticket item is required")
    @Valid
    private List<OrderItemRequest> items;
    
    @NotNull(message = "Recipient information is required")
    @Valid
    private RecipientInfoRequest recipientInfo;
}

