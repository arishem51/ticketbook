package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TicketType entity
 */
@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    
    List<TicketType> findByEvent_EventId(Long eventId);
}
