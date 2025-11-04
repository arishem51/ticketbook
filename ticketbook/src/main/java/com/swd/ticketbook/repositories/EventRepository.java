package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.Event;
import com.swd.ticketbook.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Event entity
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    // Find active events
    List<Event> findByStatusOrderByStartDateAsc(EventStatus status);
    
    // Find events by organizer
    List<Event> findByOrganizer_UserIdOrderByCreatedAtDesc(Long organizerId);
    
    // Find events by organizer and status
    List<Event> findByOrganizer_UserIdAndStatusOrderByCreatedAtDesc(
        Long organizerId, 
        EventStatus status
    );
}
