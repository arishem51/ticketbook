package com.swd.ticketbook.dto.refund;

import com.swd.ticketbook.enums.RefundStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for refund response
 */
@Data
public class RefundResponse {
    private Long requestId;
    private Long ticketId;
    private String reason;
    private RefundStatus status;
    private BigDecimal refundAmount;
    private String adminNotes;
    private LocalDateTime requestDate;
    private LocalDateTime processedDate;
}

