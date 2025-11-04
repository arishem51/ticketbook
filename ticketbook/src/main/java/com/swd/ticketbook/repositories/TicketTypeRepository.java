package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.Event;
import com.swd.ticketbook.entities.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    
    List<TicketType> findByEvent(Event event);
    
    List<TicketType> findByEventAndAvailableQuantityGreaterThan(Event event, Integer quantity);
}

