package com.swd.ticketbook.controllers;

import com.swd.ticketbook.dto.ApiResponse;
import com.swd.ticketbook.dto.order.*;
import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.security.CurrentUser;
import com.swd.ticketbook.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Order/Ticket operations
 * Implements UC-02.1 (Order Ticket) and UC-02.2 (View Purchased Tickets)
 * 
 * Base URL: /api/orders
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * UC-02.1: Check for Active Pending Order (FR5)
     * GET /api/orders/pending
     * 
     * @param user Current authenticated user
     * @return Active pending order if exists
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<OrderResponse>> getActivePendingOrder(@CurrentUser User user) {
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }

        Optional<OrderResponse> pendingOrder = orderService.getActivePendingOrder(user.getUserId());
        
        if (pendingOrder.isPresent()) {
            return ResponseEntity.ok(
                ApiResponse.success(
                    pendingOrder.get(), 
                    "You have a pending order. Please complete or cancel it first."
                )
            );
        } else {
            return ResponseEntity.ok(
                ApiResponse.success(null, "No pending order found")
            );
        }
    }

    /**
     * UC-02.1: Create Order
     * POST /api/orders
     * 
     * @param request Order creation request with ticket selection and recipient info
     * @param user Current authenticated user
     * @return Created order with reservation details
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }

        OrderResponse order = orderService.createOrder(user.getUserId(), request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                order, 
                "Order created successfully! Please complete payment within 15 minutes."
            ));
    }

    /**
     * UC-02.1: Initiate Payment
     * POST /api/orders/payment
     * 
     * @param request Payment request with order ID
     * @param user Current authenticated user
     * @return VNPAY payment URL
     */
    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<String>> initiatePayment(
            @Valid @RequestBody PaymentRequest request,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }

        String paymentUrl = orderService.initiatePayment(user.getUserId(), request);
        
        return ResponseEntity.ok(
            ApiResponse.success(paymentUrl, "Payment URL generated. Please complete payment.")
        );
    }

    /**
     * UC-02.1: Confirm Payment (VNPAY Callback)
     * POST /api/orders/{orderId}/confirm
     * 
     * @param orderId Order ID
     * @param transactionId VNPAY transaction ID
     * @return Confirmed order with tickets and QR codes
     */
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmPayment(
            @PathVariable Long orderId,
            @RequestParam String transactionId) {
        
        OrderResponse order = orderService.confirmPayment(orderId, transactionId);
        
        return ResponseEntity.ok(
            ApiResponse.success(
                order, 
                "Payment confirmed! E-tickets have been sent to your email."
            )
        );
    }

    /**
     * UC-02.1: Cancel Order
     * DELETE /api/orders/{orderId}
     * 
     * @param orderId Order ID to cancel
     * @param user Current authenticated user
     * @return Success message
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long orderId,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }

        orderService.cancelOrder(user.getUserId(), orderId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Order cancelled successfully. Reserved tickets have been released.")
        );
    }

    /**
     * UC-02.2: View All Purchased Tickets/Orders
     * GET /api/orders
     * 
     * @param user Current authenticated user
     * @return List of all orders with tickets
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getCustomerOrders(@CurrentUser User user) {
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }

        List<OrderResponse> orders = orderService.getCustomerOrders(user.getUserId());
        
        return ResponseEntity.ok(
            ApiResponse.success(orders, "Orders retrieved successfully")
        );
    }

    /**
     * UC-02.2: View Order Details with Tickets
     * GET /api/orders/{orderId}
     * 
     * @param orderId Order ID
     * @param user Current authenticated user
     * @return Order details with tickets and QR codes
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(
            @PathVariable Long orderId,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }

        OrderResponse order = orderService.getOrderDetails(user.getUserId(), orderId);
        
        return ResponseEntity.ok(
            ApiResponse.success(order, "Order details retrieved successfully")
        );
    }

    /**
     * Health check endpoint
     * GET /api/orders/health
     * 
     * @return Service status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            ApiResponse.success("Order service is running", "OK")
        );
    }
}

