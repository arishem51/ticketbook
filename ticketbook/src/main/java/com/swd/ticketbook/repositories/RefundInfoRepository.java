package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.RefundInfo;
import com.swd.ticketbook.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for RefundInfo entity
 */
@Repository
public interface RefundInfoRepository extends JpaRepository<RefundInfo, Long> {
    
    // Find refund by ticket
    Optional<RefundInfo> findByTicket_TicketId(Long ticketId);
    
    // Check if ticket has pending refund request
    boolean existsByTicket_TicketIdAndStatus(Long ticketId, RefundStatus status);
    
    // Find all refunds by user
    List<RefundInfo> findByUser_UserIdOrderByRequestDateDesc(Long userId);
    
    // Find refunds by status
    List<RefundInfo> findByStatus(RefundStatus status);
    
    // Find refunds by status ordered
    List<RefundInfo> findByStatusOrderByRequestDateAsc(RefundStatus status);
    
    // Find user's refunds by status
    List<RefundInfo> findByUser_UserIdAndStatusOrderByRequestDateDesc(
        Long userId, 
        RefundStatus status
    );
    
    // Find refunds by event (through ticket and order)
    List<RefundInfo> findByTicket_Order_Event_EventId(Long eventId);
}
