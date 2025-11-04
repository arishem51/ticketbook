package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.WithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for WithdrawalRequest entity
 */
@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {
    
    // Find requests by organizer
    List<WithdrawalRequest> findByOrganizer_UserIdOrderByRequestedAtDesc(Long organizerId);
    
    // Find requests by status
    List<WithdrawalRequest> findByStatusOrderByRequestedAtAsc(String status);
    
    // Find pending requests
    List<WithdrawalRequest> findByStatus(String status);
    
    // Check if organizer has pending withdrawal
    boolean existsByOrganizer_UserIdAndStatus(Long organizerId, String status);
    
    // Find all pending requests for admin
    @Query("SELECT w FROM WithdrawalRequest w WHERE w.status = 'PENDING_REVIEW' ORDER BY w.requestedAt ASC")
    List<WithdrawalRequest> findPendingWithdrawals();
}

