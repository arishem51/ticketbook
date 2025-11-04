package com.swd.ticketbook.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for payment processing
 */
@Data
public class PaymentRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    private String paymentMethod = "VNPAY"; // Default to VNPAY
    
    private String returnUrl; // For payment callback
}

