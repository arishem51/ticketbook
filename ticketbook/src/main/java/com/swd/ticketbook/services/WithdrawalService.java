package com.swd.ticketbook.services;

import com.swd.ticketbook.dto.organizer.WithdrawalResponse;
import com.swd.ticketbook.entities.*;
import com.swd.ticketbook.enums.BookingStatus;
import com.swd.ticketbook.enums.RefundStatus;
import com.swd.ticketbook.enums.UserRole;
import com.swd.ticketbook.exceptions.BusinessRuleViolationException;
import com.swd.ticketbook.exceptions.ResourceNotFoundException;
import com.swd.ticketbook.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Revenue Withdrawal (UC-03.4)
 * Business Rules: FR17, FR26
 */
@Service
public class WithdrawalService {

    private static final Logger log = LoggerFactory.getLogger(WithdrawalService.class);
    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.05"); // 5% platform fee
    private static final BigDecimal MINIMUM_WITHDRAWAL = new BigDecimal("50.00");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizerProfileRepository organizerProfileRepository;

    @Autowired
    private WithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RefundInfoRepository refundRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private EmailService emailService;

    /**
     * UC-03.4: Request withdrawal
     * FR17, FR26: Only Verified Organizer can request withdrawal
     */
    @Transactional
    public WithdrawalResponse requestWithdrawal(Long organizerId, com.swd.ticketbook.dto.organizer.WithdrawalRequest request) {
        // Get user and verify role
        User organizer = userRepository.findById(organizerId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (organizer.getRole() != UserRole.VERIFIED_ORGANIZER) {
            throw new BusinessRuleViolationException("Only Verified Organizers can request withdrawals");
        }

        // Get organizer profile with bank details
        OrganizerProfile profile = organizerProfileRepository.findByUser(organizer)
            .orElseThrow(() -> new ResourceNotFoundException("Organizer profile not found"));

        if (!profile.getIsBankVerified()) {
            throw new BusinessRuleViolationException("Bank account must be verified before withdrawal");
        }

        // Check for existing pending withdrawal
        if (withdrawalRequestRepository.existsByOrganizer_UserIdAndStatus(organizerId, "PENDING_REVIEW")) {
            throw new BusinessRuleViolationException("You already have a pending withdrawal request");
        }

        // Calculate available balance
        BigDecimal availableBalance = calculateAvailableBalance(organizerId, request.getEventId());

        // Validate withdrawal amount
        if (request.getAmount().compareTo(MINIMUM_WITHDRAWAL) < 0) {
            throw new BusinessRuleViolationException("Minimum withdrawal amount is $" + MINIMUM_WITHDRAWAL);
        }

        if (request.getAmount().compareTo(availableBalance) > 0) {
            throw new BusinessRuleViolationException("Withdrawal amount exceeds available balance: $" + availableBalance);
        }

        // Create withdrawal request
        WithdrawalRequest withdrawalRequest = new WithdrawalRequest(organizer, request.getAmount(), availableBalance);
        
        // Set event if specific event withdrawal
        if (request.getEventId() != null) {
            Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
            
            if (!event.getOrganizer().getUserId().equals(organizerId)) {
                throw new BusinessRuleViolationException("You do not own this event");
            }
            
            if (!event.hasOccurred()) {
                throw new BusinessRuleViolationException("Event must be completed before withdrawal");
            }
            
            withdrawalRequest.setEvent(event);
        }

        // Set bank details from profile
        withdrawalRequest.setBankName(profile.getBankName());
        withdrawalRequest.setBankAccountNumber(profile.getBankAccountNumber());
        withdrawalRequest.setBankAccountHolder(profile.getBankAccountHolderName());
        withdrawalRequest.setBankBranch(profile.getBankBranch());

        // Calculate platform fee
        BigDecimal platformFee = request.getAmount().multiply(PLATFORM_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        withdrawalRequest.setPlatformFee(platformFee);

        withdrawalRequest = withdrawalRequestRepository.save(withdrawalRequest);

        // Log action
        auditLogService.logWithdrawalRequest(organizer, withdrawalRequest.getRequestId());

        // Send confirmation email
        emailService.sendWithdrawalRequestConfirmation(
            organizer.getContact(),
            organizer.getFullName(),
            request.getAmount()
        );

        log.info("Withdrawal requested - Organizer ID: {}, Amount: {}, Request ID: {}", 
                 organizerId, request.getAmount(), withdrawalRequest.getRequestId());

        return mapToWithdrawalResponse(withdrawalRequest);
    }

    /**
     * Get organizer's withdrawal requests
     */
    public List<WithdrawalResponse> getOrganizerWithdrawals(Long organizerId) {
        List<WithdrawalRequest> requests = withdrawalRequestRepository
            .findByOrganizer_UserIdOrderByRequestedAtDesc(organizerId);
        
        return requests.stream()
            .map(this::mapToWithdrawalResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get withdrawal by ID
     */
    public WithdrawalResponse getWithdrawalById(Long organizerId, Long requestId) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Withdrawal request not found"));

        if (!request.getOrganizer().getUserId().equals(organizerId)) {
            throw new BusinessRuleViolationException("You do not have permission to view this withdrawal request");
        }

        return mapToWithdrawalResponse(request);
    }

    /**
     * Calculate available balance for withdrawal
     */
    public BigDecimal calculateAvailableBalance(Long organizerId, Long eventId) {
        List<Event> events;
        
        if (eventId != null) {
            // Calculate for specific event
            Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
            events = List.of(event);
        } else {
            // Calculate for all completed events
            events = eventRepository.findByOrganizer_UserIdOrderByCreatedAtDesc(organizerId).stream()
                .filter(Event::hasOccurred)
                .collect(Collectors.toList());
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Event event : events) {
            // Get confirmed orders
            List<Order> orders = orderRepository.findByEvent_EventId(event.getEventId()).stream()
                .filter(o -> o.getBookingStatus() == BookingStatus.CONFIRMED)
                .collect(Collectors.toList());

            BigDecimal eventRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Subtract refunds
            List<RefundInfo> refunds = refundRepository.findByTicket_Order_Event_EventId(event.getEventId()).stream()
                .filter(r -> r.getStatus() == RefundStatus.COMPLETED)
                .collect(Collectors.toList());

            BigDecimal refundedAmount = refunds.stream()
                .map(RefundInfo::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalRevenue = totalRevenue.add(eventRevenue.subtract(refundedAmount));
        }

        // Subtract platform fee
        BigDecimal platformFee = totalRevenue.multiply(PLATFORM_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        
        // Subtract already withdrawn amounts
        List<WithdrawalRequest> completedWithdrawals = withdrawalRequestRepository
            .findByOrganizer_UserIdOrderByRequestedAtDesc(organizerId).stream()
            .filter(w -> "COMPLETED".equals(w.getStatus()))
            .collect(Collectors.toList());

        BigDecimal withdrawnAmount = completedWithdrawals.stream()
            .map(WithdrawalRequest::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal availableBalance = totalRevenue.subtract(platformFee).subtract(withdrawnAmount);

        return availableBalance.max(BigDecimal.ZERO);
    }

    // Helper methods

    private WithdrawalResponse mapToWithdrawalResponse(WithdrawalRequest request) {
        WithdrawalResponse response = new WithdrawalResponse();
        response.setRequestId(request.getRequestId());
        response.setOrganizerId(request.getOrganizer().getUserId());
        response.setOrganizerName(request.getOrganizer().getFullName());
        response.setAmount(request.getAmount());
        response.setAvailableBalance(request.getAvailableBalance());
        response.setPlatformFee(request.getPlatformFee());
        response.setBankName(request.getBankName());
        response.setBankAccountNumber(maskAccountNumber(request.getBankAccountNumber()));
        response.setBankAccountHolder(request.getBankAccountHolder());
        response.setStatus(request.getStatus());
        response.setRequestedAt(request.getRequestedAt());
        response.setReviewedAt(request.getReviewedAt());
        response.setProcessedAt(request.getProcessedAt());
        response.setAdminNotes(request.getAdminNotes());
        response.setTransactionReference(request.getTransactionReference());
        return response;
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}

