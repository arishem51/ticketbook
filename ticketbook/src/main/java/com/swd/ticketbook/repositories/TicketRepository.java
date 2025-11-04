package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.Order;
import com.swd.ticketbook.entities.Ticket;
import com.swd.ticketbook.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    List<Ticket> findByOrder(Order order);
    
    /**
     * Find ticket by QR code (FR6: Unique QR code)
     */
    Optional<Ticket> findByQrCode(String qrCode);
    
    List<Ticket> findByOrderAndStatus(Order order, TicketStatus status);
    
    long countByOrderAndStatus(Order order, TicketStatus status);
    
    boolean existsByQrCode(String qrCode);
}

