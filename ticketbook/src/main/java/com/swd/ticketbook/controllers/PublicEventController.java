package com.swd.ticketbook.controllers;

import com.swd.ticketbook.dto.ApiResponse;
import com.swd.ticketbook.dto.event.PublicEventResponse;
import com.swd.ticketbook.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for public event browsing
 * No authentication required - accessible to all users
 */
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PublicEventController {

    @Autowired
    private EventService eventService;

    /**
     * Browse all active events
     * GET /api/events
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PublicEventResponse>>> browseEvents() {
        List<PublicEventResponse> events = eventService.browsePublicEvents();
        
        return ResponseEntity.ok(
            ApiResponse.success(events, "Events retrieved successfully")
        );
    }

    /**
     * Get event details by ID
     * GET /api/events/{eventId}
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<PublicEventResponse>> getEventById(@PathVariable Long eventId) {
        PublicEventResponse event = eventService.getPublicEventById(eventId);
        
        return ResponseEntity.ok(
            ApiResponse.success(event, "Event details retrieved successfully")
        );
    }
}

