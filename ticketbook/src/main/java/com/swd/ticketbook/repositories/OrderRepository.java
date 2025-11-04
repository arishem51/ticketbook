package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.Order;
import com.swd.ticketbook.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Order entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // FR5: Find active pending order for a customer (across ALL events)
    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId " +
           "AND o.bookingStatus = 'PENDING_PAYMENT' " +
           "AND o.reservationExpiresAt > :now")
    Optional<Order> findActivePendingOrderByUserId(
        @Param("userId") Long userId, 
        @Param("now") LocalDateTime now
    );
    
    // Find all orders by user
    List<Order> findByUser_UserIdOrderByOrderDateDesc(Long userId);
    
    // Find orders by user and status
    List<Order> findByUser_UserIdAndBookingStatusOrderByOrderDateDesc(
        Long userId, 
        BookingStatus status
    );
    
    // Find expired pending orders (for cleanup)
    @Query("SELECT o FROM Order o WHERE o.bookingStatus = 'PENDING_PAYMENT' " +
           "AND o.reservationExpiresAt <= :now")
    List<Order> findExpiredPendingOrders(@Param("now") LocalDateTime now);
}
