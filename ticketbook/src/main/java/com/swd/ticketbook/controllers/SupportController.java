package com.swd.ticketbook.controllers;

import com.swd.ticketbook.dto.ApiResponse;
import com.swd.ticketbook.dto.support.SupportRequest;
import com.swd.ticketbook.dto.support.SupportResponse;
import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.security.CurrentUser;
import com.swd.ticketbook.services.SupportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Support Ticket operations
 * Implements UC-02.4 (Submit Event Support Request)
 * 
 * Base URL: /api/support
 */
@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SupportController {

    @Autowired
    private SupportService supportService;

    /**
     * UC-02.4: Submit Support Request
     * POST /api/support
     * 
     * @param request Support request with event ID, subject, description, category
     * @param user Current authenticated user
     * @return Created support ticket
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SupportResponse>> submitSupportRequest(
            @Valid @RequestBody SupportRequest request,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }

        SupportResponse ticket = supportService.submitSupportRequest(user.getUserId(), request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                ticket, 
                "Support request submitted successfully. Organizer will respond within 24-48 hours."
            ));
    }

    /**
     * UC-02.4: View Customer's Support Requests
     * GET /api/support
     * 
     * @param user Current authenticated user
     * @return List of support tickets
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SupportResponse>>> getCustomerSupportRequests(
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }

        List<SupportResponse> tickets = supportService.getCustomerSupportRequests(user.getUserId());
        
        return ResponseEntity.ok(
            ApiResponse.success(tickets, "Support requests retrieved successfully")
        );
    }

    /**
     * UC-02.4: View Support Request Details
     * GET /api/support/{ticketId}
     * 
     * @param ticketId Support ticket ID
     * @param user Current authenticated user
     * @return Support ticket details
     */
    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<SupportResponse>> getSupportRequestDetails(
            @PathVariable Long ticketId,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }

        SupportResponse ticket = supportService.getSupportRequestDetails(user.getUserId(), ticketId);
        
        return ResponseEntity.ok(
            ApiResponse.success(ticket, "Support details retrieved successfully")
        );
    }

    /**
     * UC-02.4: Confirm Resolution (Customer)
     * POST /api/support/{ticketId}/confirm
     * 
     * @param ticketId Support ticket ID
     * @param user Current authenticated user
     * @return Updated support ticket
     */
    @PostMapping("/{ticketId}/confirm")
    public ResponseEntity<ApiResponse<SupportResponse>> confirmResolution(
            @PathVariable Long ticketId,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }

        SupportResponse ticket = supportService.confirmResolution(user.getUserId(), ticketId);
        
        return ResponseEntity.ok(
            ApiResponse.success(ticket, "Support ticket closed successfully")
        );
    }

    /**
     * Health check endpoint
     * GET /api/support/health
     * 
     * @return Service status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            ApiResponse.success("Support service is running", "OK")
        );
    }
}

