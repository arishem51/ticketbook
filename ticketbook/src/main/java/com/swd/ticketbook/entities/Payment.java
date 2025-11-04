package com.swd.ticketbook.entities;

import com.swd.ticketbook.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a Payment transaction
 */
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "vnpay_transaction_id", length = 255)
    private String vnpayTransactionId;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod = "VNPAY";

    public Payment(Order order, BigDecimal amount) {
        this.order = order;
        this.amount = amount;
        this.paymentDate = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.PENDING;
    }

    /**
     * Mark payment as successful
     */
    public void markAsPaid(String transactionId) {
        this.paymentStatus = PaymentStatus.PAID;
        this.vnpayTransactionId = transactionId;
    }

    /**
     * Mark payment as failed
     */
    public void markAsFailed() {
        this.paymentStatus = PaymentStatus.FAILED;
    }

    /**
     * Process refund
     */
    public void refund() {
        this.paymentStatus = PaymentStatus.REFUNDED;
    }
}

