package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.Event;
import com.swd.ticketbook.entities.SupportTicket;
import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.enums.SupportTicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    
    List<SupportTicket> findByUser(User user);
    
    List<SupportTicket> findByEvent(Event event);
    
    List<SupportTicket> findByStatus(SupportTicketStatus status);
    
    List<SupportTicket> findByEventAndStatus(Event event, SupportTicketStatus status);
}

