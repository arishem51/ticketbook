package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.Event;
import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    List<Event> findByStatus(EventStatus status);
    
    List<Event> findByOrganizer(User organizer);
    
    List<Event> findByOrganizerAndStatus(User organizer, EventStatus status);
    
    @Query("SELECT e FROM Event e WHERE e.status = :status AND e.startDate >= :now ORDER BY e.startDate ASC")
    List<Event> findUpcomingEvents(@Param("status") EventStatus status, @Param("now") LocalDateTime now);
    
    @Query("SELECT e FROM Event e WHERE e.name LIKE %:keyword% OR e.description LIKE %:keyword%")
    List<Event> searchEvents(@Param("keyword") String keyword);
    
    @Query("SELECT e FROM Event e WHERE e.category.categoryId = :categoryId AND e.status = 'ACTIVE'")
    List<Event> findActiveByCategoryId(@Param("categoryId") Long categoryId);
}

