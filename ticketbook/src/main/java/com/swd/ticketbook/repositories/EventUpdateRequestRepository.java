package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.EventUpdateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for EventUpdateRequest entity
 */
@Repository
public interface EventUpdateRequestRepository extends JpaRepository<EventUpdateRequest, Long> {
    
    // Find requests by event
    List<EventUpdateRequest> findByEvent_EventIdOrderByRequestedAtDesc(Long eventId);
    
    // Find requests by organizer
    List<EventUpdateRequest> findByOrganizer_UserIdOrderByRequestedAtDesc(Long organizerId);
    
    // Find requests by status
    List<EventUpdateRequest> findByStatusOrderByRequestedAtAsc(String status);
    
    // Find pending requests
    List<EventUpdateRequest> findByStatus(String status);
    
    // Check if event has pending update request
    boolean existsByEvent_EventIdAndStatus(Long eventId, String status);
}

