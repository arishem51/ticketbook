package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.SupportTicket;
import com.swd.ticketbook.enums.SupportTicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for SupportTicket entity
 */
@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    
    // Find support tickets by user (customer)
    List<SupportTicket> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
    
    // Find support tickets by event organizer
    List<SupportTicket> findByEvent_Organizer_UserIdOrderByCreatedAtAsc(Long organizerId);
    
    // Find support tickets by status
    List<SupportTicket> findByStatusOrderByCreatedAtAsc(SupportTicketStatus status);
    
    // Find user's support tickets by status
    List<SupportTicket> findByUser_UserIdAndStatusOrderByCreatedAtDesc(
        Long userId, 
        SupportTicketStatus status
    );
}
