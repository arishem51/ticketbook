package com.swd.ticketbook.controllers;

import com.swd.ticketbook.dto.ApiResponse;
import com.swd.ticketbook.dto.admin.OrganizerProfileResponse;
import com.swd.ticketbook.dto.organizer.*;
import com.swd.ticketbook.dto.admin.EventResponse;
import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.security.CurrentUser;
import com.swd.ticketbook.services.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller for Organizer operations
 * Handles event management, KYC, statistics, and withdrawals
 * Business Rules: FR17, FR26
 */
@RestController
@RequestMapping("/api/organizer")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrganizerController {

    @Autowired
    private EventService eventService;

    @Autowired
    private OrganizerService organizerService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private WithdrawalService withdrawalService;

    // ==================== KYC VERIFICATION ====================

    /**
     * Submit KYC verification
     * POST /api/organizer/kyc
     */
    @PostMapping("/kyc")
    public ResponseEntity<ApiResponse<OrganizerProfileResponse>> submitKyc(
            @Valid @RequestBody KycSubmissionRequest request,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        OrganizerProfileResponse profile = organizerService.submitKycVerification(user.getUserId(), request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(profile, "KYC verification submitted successfully. Admin will review within 3-5 business days."));
    }

    /**
     * Get own KYC status
     * GET /api/organizer/kyc
     */
    @GetMapping("/kyc")
    public ResponseEntity<ApiResponse<OrganizerProfileResponse>> getKycStatus(
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        OrganizerProfileResponse profile = organizerService.getOrganizerProfile(user.getUserId());
        
        return ResponseEntity.ok(
            ApiResponse.success(profile, "KYC status retrieved successfully")
        );
    }

    // ==================== EVENT MANAGEMENT ====================

    /**
     * Create event
     * POST /api/organizer/events
     */
    @PostMapping("/events")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        EventResponse event = eventService.createEvent(user.getUserId(), request);
        
        String message = request.isSubmitForApproval() ? 
            "Event submitted for admin approval" : 
            "Event saved as draft";
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(event, message));
    }

    /**
     * Get organizer's events
     * GET /api/organizer/events
     */
    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getOrganizerEvents(
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        List<EventResponse> events = eventService.getOrganizerEvents(user.getUserId());
        
        return ResponseEntity.ok(
            ApiResponse.success(events, "Events retrieved successfully")
        );
    }

    /**
     * Get event by ID
     * GET /api/organizer/events/{eventId}
     */
    @GetMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(
            @PathVariable Long eventId,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        EventResponse event = eventService.getEventById(user.getUserId(), eventId);
        
        return ResponseEntity.ok(
            ApiResponse.success(event, "Event details retrieved successfully")
        );
    }

    /**
     * Request event update
     * POST /api/organizer/events/{eventId}/update-request
     */
    @PostMapping("/events/{eventId}/update-request")
    public ResponseEntity<ApiResponse<EventUpdateRequestResponse>> requestEventUpdate(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventRequest request,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        EventUpdateRequestResponse updateRequest = eventService.requestEventUpdate(
            user.getUserId(), eventId, request
        );
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(updateRequest, "Update request submitted for admin review"));
    }

    /**
     * Get organizer's update requests
     * GET /api/organizer/update-requests
     */
    @GetMapping("/update-requests")
    public ResponseEntity<ApiResponse<List<EventUpdateRequestResponse>>> getUpdateRequests(
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        List<EventUpdateRequestResponse> requests = eventService.getOrganizerUpdateRequests(user.getUserId());
        
        return ResponseEntity.ok(
            ApiResponse.success(requests, "Update requests retrieved successfully")
        );
    }

    // ==================== STATISTICS ====================

    /**
     * Get event statistics
     * GET /api/organizer/events/{eventId}/statistics
     */
    @GetMapping("/events/{eventId}/statistics")
    public ResponseEntity<ApiResponse<EventStatisticsResponse>> getEventStatistics(
            @PathVariable Long eventId,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        EventStatisticsResponse stats = statisticsService.getEventStatistics(user.getUserId(), eventId);
        
        return ResponseEntity.ok(
            ApiResponse.success(stats, "Event statistics retrieved successfully")
        );
    }

    /**
     * Get organizer summary
     * GET /api/organizer/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrganizerSummary(
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        Map<String, Object> summary = statisticsService.getOrganizerSummary(user.getUserId());
        
        return ResponseEntity.ok(
            ApiResponse.success(summary, "Organizer summary retrieved successfully")
        );
    }

    // ==================== REVENUE WITHDRAWAL ====================

    /**
     * Request withdrawal
     * POST /api/organizer/withdrawals
     */
    @PostMapping("/withdrawals")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> requestWithdrawal(
            @Valid @RequestBody com.swd.ticketbook.dto.organizer.WithdrawalRequest request,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        WithdrawalResponse withdrawal = withdrawalService.requestWithdrawal(user.getUserId(), request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(withdrawal, "Withdrawal request submitted for admin review"));
    }

    /**
     * Get withdrawal requests
     * GET /api/organizer/withdrawals
     */
    @GetMapping("/withdrawals")
    public ResponseEntity<ApiResponse<List<WithdrawalResponse>>> getWithdrawals(
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        List<WithdrawalResponse> withdrawals = withdrawalService.getOrganizerWithdrawals(user.getUserId());
        
        return ResponseEntity.ok(
            ApiResponse.success(withdrawals, "Withdrawals retrieved successfully")
        );
    }

    /**
     * Get withdrawal by ID
     * GET /api/organizer/withdrawals/{requestId}
     */
    @GetMapping("/withdrawals/{requestId}")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> getWithdrawalById(
            @PathVariable Long requestId,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        WithdrawalResponse withdrawal = withdrawalService.getWithdrawalById(user.getUserId(), requestId);
        
        return ResponseEntity.ok(
            ApiResponse.success(withdrawal, "Withdrawal details retrieved successfully")
        );
    }

    /**
     * Get available balance
     * GET /api/organizer/balance
     */
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getAvailableBalance(
            @RequestParam(required = false) Long eventId,
            @CurrentUser User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        BigDecimal balance = withdrawalService.calculateAvailableBalance(user.getUserId(), eventId);
        
        return ResponseEntity.ok(
            ApiResponse.success(Map.of("availableBalance", balance), "Balance retrieved successfully")
        );
    }
}

