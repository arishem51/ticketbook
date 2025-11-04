package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Ticket entity
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    // FR6: Find ticket by unique QR code
    Optional<Ticket> findByQrCode(String qrCode);
    
    // Check if QR code exists (for uniqueness validation)
    boolean existsByQrCode(String qrCode);
    
    // Find tickets by order
    List<Ticket> findByOrder_OrderId(Long orderId);
    
    // Find tickets by user (through order)
    List<Ticket> findByOrder_User_UserId(Long userId);
    
    // Find tickets by event (through order)
    List<Ticket> findByOrder_Event_EventId(Long eventId);
}
