package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.RefundInfo;
import com.swd.ticketbook.entities.Ticket;
import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundInfoRepository extends JpaRepository<RefundInfo, Long> {
    
    List<RefundInfo> findByUser(User user);
    
    List<RefundInfo> findByStatus(RefundStatus status);
    
    Optional<RefundInfo> findByTicket(Ticket ticket);
    
    /**
     * Check if ticket already has a pending refund request
     */
    boolean existsByTicketAndStatus(Ticket ticket, RefundStatus status);
}

