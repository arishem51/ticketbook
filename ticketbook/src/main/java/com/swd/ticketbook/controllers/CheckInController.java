package com.swd.ticketbook.controllers;

import com.swd.ticketbook.dto.ApiResponse;
import com.swd.ticketbook.dto.checkin.CheckInRequest;
import com.swd.ticketbook.dto.checkin.CheckInResponse;
import com.swd.ticketbook.services.CheckInService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Check-in operations
 * Implements UC-02.3 (Check-in)
 * 
 * Base URL: /api/checkin
 */
@RestController
@RequestMapping("/api/checkin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    /**
     * UC-02.3: Check-in with QR Code
     * POST /api/checkin
     * 
     * @param request Check-in request with QR code
     * @return Check-in result (success or rejection reason)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CheckInResponse>> checkIn(
            @Valid @RequestBody CheckInRequest request) {
        
        CheckInResponse response = checkInService.checkIn(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(
                ApiResponse.success(response, "✓ Entry Granted - Welcome to the event!")
            );
        } else {
            return ResponseEntity.ok(
                ApiResponse.success(response, response.getMessage())
            );
        }
    }

    /**
     * Preview Ticket Details by QR Code
     * GET /api/checkin/preview/{qrCode}
     * 
     * @param qrCode QR code to preview
     * @return Ticket details
     */
    @GetMapping("/preview/{qrCode}")
    public ResponseEntity<ApiResponse<CheckInResponse>> previewTicket(
            @PathVariable String qrCode) {
        
        CheckInResponse response = checkInService.getTicketDetails(qrCode);
        
        return ResponseEntity.ok(
            ApiResponse.success(response, "Ticket details retrieved")
        );
    }

    /**
     * Health check endpoint
     * GET /api/checkin/health
     * 
     * @return Service status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            ApiResponse.success("Check-in service is running", "OK")
        );
    }
}

