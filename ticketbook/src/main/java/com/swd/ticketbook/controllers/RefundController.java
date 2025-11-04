package com.swd.ticketbook.controllers;

import com.swd.ticketbook.dto.ApiResponse;
import com.swd.ticketbook.dto.refund.RefundRequest;
import com.swd.ticketbook.dto.refund.RefundResponse;
import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.security.CurrentUser;
import com.swd.ticketbook.services.RefundService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Refund operations
 * Implements UC-02.5 (Submit Refund Request)
 * 
 * Base URL: /api/refunds
 */
@RestController
@RequestMapping("/api/refunds")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RefundController {

    @Autowired
    private RefundService refundService;

    /**
     * UC-02.5: Submit Refund Request
     * POST /api/refunds
     * 
     * @param request Refund request with ticket ID and reason
     * @param user Current authenticated user
     * @return Created refund request
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RefundResponse>> submitRefundRequest(
            @Valid @RequestBody RefundRequest request,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }

        RefundResponse refund = refundService.submitRefundRequest(user.getUserId(), request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                refund, 
                "Refund request submitted successfully. Admin will review within 24-48 hours."
            ));
    }

    /**
     * UC-02.5: View Customer's Refund Requests
     * GET /api/refunds
     * 
     * @param user Current authenticated user
     * @return List of refund requests
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getCustomerRefundRequests(
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }

        List<RefundResponse> refunds = refundService.getCustomerRefundRequests(user.getUserId());
        
        return ResponseEntity.ok(
            ApiResponse.success(refunds, "Refund requests retrieved successfully")
        );
    }

    /**
     * UC-02.5: View Refund Request Details
     * GET /api/refunds/{requestId}
     * 
     * @param requestId Refund request ID
     * @param user Current authenticated user
     * @return Refund request details
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<RefundResponse>> getRefundDetails(
            @PathVariable Long requestId,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }

        RefundResponse refund = refundService.getRefundDetails(user.getUserId(), requestId);
        
        return ResponseEntity.ok(
            ApiResponse.success(refund, "Refund details retrieved successfully")
        );
    }

    /**
     * Health check endpoint
     * GET /api/refunds/health
     * 
     * @return Service status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            ApiResponse.success("Refund service is running", "OK")
        );
    }
}

