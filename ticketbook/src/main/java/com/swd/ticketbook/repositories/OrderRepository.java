package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.Order;
import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.entities.Event;
import com.swd.ticketbook.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUser(User user);
    
    List<Order> findByUserAndBookingStatus(User user, BookingStatus status);
    
    /**
     * Find pending order for user (FR5: Single pending order rule)
     */
    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.bookingStatus = 'PENDING_PAYMENT' AND o.reservationExpiresAt > :now")
    Optional<Order> findPendingOrderByUser(@Param("user") User user, @Param("now") LocalDateTime now);
    
    /**
     * Find pending order for specific user and event
     */
    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.event = :event AND o.bookingStatus = 'PENDING_PAYMENT' AND o.reservationExpiresAt > :now")
    Optional<Order> findPendingOrderByUserAndEvent(@Param("user") User user, @Param("event") Event event, @Param("now") LocalDateTime now);
    
    /**
     * Find expired pending orders for cleanup
     */
    @Query("SELECT o FROM Order o WHERE o.bookingStatus = 'PENDING_PAYMENT' AND o.reservationExpiresAt < :now")
    List<Order> findExpiredOrders(@Param("now") LocalDateTime now);
    
    List<Order> findByEvent(Event event);
    
    long countByEventAndBookingStatus(Event event, BookingStatus status);
}

