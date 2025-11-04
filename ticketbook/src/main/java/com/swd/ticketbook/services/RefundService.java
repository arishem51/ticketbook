package com.swd.ticketbook.services;

import com.swd.ticketbook.dto.refund.RefundRequest;
import com.swd.ticketbook.dto.refund.RefundResponse;
import com.swd.ticketbook.entities.Event;
import com.swd.ticketbook.entities.RefundInfo;
import com.swd.ticketbook.entities.Ticket;
import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.enums.RefundStatus;
import com.swd.ticketbook.enums.TicketStatus;
import com.swd.ticketbook.exceptions.BusinessRuleViolationException;
import com.swd.ticketbook.exceptions.ResourceNotFoundException;
import com.swd.ticketbook.repositories.RefundInfoRepository;
import com.swd.ticketbook.repositories.TicketRepository;
import com.swd.ticketbook.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for Refund operations (UC-02.5)
 * Business Rules: FR7, FR8, FR20, FR22
 */
@Service
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

    @Autowired
    private RefundInfoRepository refundRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private EmailService emailService;

    /**
     * UC-02.5: Submit Refund Request
     * FR7: Check event refund policy
     * FR8: Validate ticket hasn't been used
     * FR22: Validate refund reason provided
     */
    @Transactional
    public RefundResponse submitRefundRequest(Long userId, RefundRequest request) {
        // Get user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get ticket
        Ticket ticket = ticketRepository.findById(request.getTicketId())
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        // Verify ticket belongs to user
        if (!ticket.getOrder().getUser().getUserId().equals(userId)) {
            throw new BusinessRuleViolationException("Ticket does not belong to this user");
        }

        // Validate ticket status (FR8)
        if (ticket.getStatus() == TicketStatus.USED) {
            throw new BusinessRuleViolationException("Cannot refund ticket that has been used for check-in");
        }

        if (ticket.getStatus() == TicketStatus.REFUNDED) {
            throw new BusinessRuleViolationException("Ticket has already been refunded");
        }

        // Check event refund policy (FR7)
        Event event = ticket.getOrder().getEvent();
        if (!event.getRefundAllowed()) {
            throw new BusinessRuleViolationException("Event organizer does not allow refunds");
        }

        // Check event hasn't occurred yet
        if (event.hasOccurred()) {
            throw new BusinessRuleViolationException("Cannot refund tickets for events that have already occurred");
        }

        // Check for existing pending refund request
        boolean hasPendingRefund = refundRepository.existsByTicket_TicketIdAndStatus(
            ticket.getTicketId(), 
            RefundStatus.PENDING_ADMIN_REVIEW
        );
        if (hasPendingRefund) {
            throw new BusinessRuleViolationException("You already have a pending refund request for this ticket");
        }

        // FR22: Validate refund reason
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new BusinessRuleViolationException("Please provide a reason for your refund request");
        }

        // Calculate refund amount (full ticket price)
        BigDecimal refundAmount = ticket.getTicketType().getPrice();

        // Create refund request
        RefundInfo refundInfo = new RefundInfo(ticket, user, request.getReason(), refundAmount);
        refundInfo = refundRepository.save(refundInfo);

        // Send notification to customer
        emailService.sendRefundRequestConfirmation(
            user.getContact(),
            refundInfo.getRequestId(),
            event.getName(),
            refundAmount
        );

        // FR20: Log refund request
        log.info("Refund request submitted - Request: {}, Ticket: {}, User: {}, Amount: {}",
            refundInfo.getRequestId(),
            ticket.getTicketId(),
            userId,
            refundAmount
        );

        return mapToRefundResponse(refundInfo);
    }

    /**
     * UC-02.5: Get Customer's Refund Requests
     */
    public List<RefundResponse> getCustomerRefundRequests(Long userId) {
        List<RefundInfo> refunds = refundRepository.findByUser_UserIdOrderByRequestDateDesc(userId);
        return refunds.stream()
            .map(this::mapToRefundResponse)
            .toList();
    }

    /**
     * UC-02.5: Get Refund Request Details
     */
    public RefundResponse getRefundDetails(Long userId, Long requestId) {
        RefundInfo refund = refundRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Refund request not found"));

        // Verify request belongs to user
        if (!refund.getUser().getUserId().equals(userId)) {
            throw new BusinessRuleViolationException("Refund request does not belong to this user");
        }

        return mapToRefundResponse(refund);
    }

    /**
     * Admin: Approve Refund Request
     * FR7: Admin approval required
     * FR8: Invalidate ticket after refund
     */
    @Transactional
    public RefundResponse approveRefund(Long adminId, Long requestId, String adminNotes) {
        RefundInfo refund = refundRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Refund request not found"));

        if (refund.getStatus() != RefundStatus.PENDING_ADMIN_REVIEW) {
            throw new BusinessRuleViolationException("Refund request is not pending review");
        }

        // Approve refund
        refund.approve(adminId, adminNotes);
        refundRepository.save(refund);

        // Process refund through VNPAY
        try {
            boolean refundSuccess = vnPayService.processRefund(
                refund.getTicket().getOrder().getOrderId().toString(),
                refund.getRefundAmount()
            );

            if (refundSuccess) {
                // Mark as completed
                refund.complete();
                
                // FR8: Invalidate ticket
                Ticket ticket = refund.getTicket();
                ticket.refund();
                ticketRepository.save(ticket);

                refundRepository.save(refund);

                // Send confirmation to customer
                emailService.sendRefundCompleted(
                    refund.getUser().getContact(),
                    refund.getRequestId(),
                    refund.getRefundAmount()
                );

                // FR20: Log refund completion
                log.info("Refund completed - Request: {}, Admin: {}, Amount: {}",
                    requestId, adminId, refund.getRefundAmount());
            } else {
                throw new RuntimeException("VNPAY refund processing failed");
            }

        } catch (Exception e) {
            log.error("Refund processing failed for request: {}", requestId, e);
            throw new BusinessRuleViolationException("Refund processing encountered an error. Our team will resolve this manually.");
        }

        return mapToRefundResponse(refund);
    }

    /**
     * Admin: Reject Refund Request
     */
    @Transactional
    public RefundResponse rejectRefund(Long requestId, String rejectionReason) {
        RefundInfo refund = refundRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Refund request not found"));

        if (refund.getStatus() != RefundStatus.PENDING_ADMIN_REVIEW) {
            throw new BusinessRuleViolationException("Refund request is not pending review");
        }

        // Reject refund
        refund.reject(rejectionReason);
        refundRepository.save(refund);

        // Send rejection notification to customer
        emailService.sendRefundRejected(
            refund.getUser().getContact(),
            refund.getRequestId(),
            rejectionReason
        );

        // FR20: Log rejection
        log.info("Refund rejected - Request: {}, Reason: {}", requestId, rejectionReason);

        return mapToRefundResponse(refund);
    }

    /**
     * Admin: Get All Pending Refund Requests
     */
    public List<RefundResponse> getPendingRefundRequests() {
        List<RefundInfo> refunds = refundRepository.findByStatusOrderByRequestDateAsc(
            RefundStatus.PENDING_ADMIN_REVIEW
        );
        return refunds.stream()
            .map(this::mapToRefundResponse)
            .toList();
    }

    /**
     * Map RefundInfo entity to RefundResponse DTO
     */
    private RefundResponse mapToRefundResponse(RefundInfo refund) {
        RefundResponse response = new RefundResponse();
        response.setRequestId(refund.getRequestId());
        response.setTicketId(refund.getTicket().getTicketId());
        response.setReason(refund.getReason());
        response.setStatus(refund.getStatus());
        response.setRefundAmount(refund.getRefundAmount());
        response.setAdminNotes(refund.getAdminNotes());
        response.setRequestDate(refund.getRequestDate());
        response.setProcessedDate(refund.getProcessedDate());
        return response;
    }
}

