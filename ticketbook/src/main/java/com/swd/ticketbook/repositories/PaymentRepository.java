package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.Order;
import com.swd.ticketbook.entities.Payment;
import com.swd.ticketbook.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByOrder(Order order);
    
    Optional<Payment> findByVnpayTransactionId(String transactionId);
    
    List<Payment> findByPaymentStatus(PaymentStatus status);
}

