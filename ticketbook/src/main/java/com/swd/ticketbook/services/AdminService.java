package com.swd.ticketbook.services;

import com.swd.ticketbook.dto.admin.*;
import com.swd.ticketbook.dto.auth.UserResponse;
import com.swd.ticketbook.dto.refund.RefundResponse;
import com.swd.ticketbook.entities.*;
import com.swd.ticketbook.enums.EventStatus;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Admin operations (UC-04)
 * Business Rules: FR17, FR18, FR19, FR20, FR21, FR22, FR23
 */
@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private OrganizerProfileRepository organizerProfileRepository;

    @Autowired
    private RefundInfoRepository refundRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private EventUpdateRequestRepository updateRequestRepository;

    @Autowired
    private WithdrawalRequestRepository withdrawalRequestRepository;

    // ==================== USER MANAGEMENT (UC-04.1 - 04.5) ====================

    /**
     * UC-04.1: View all users
     * FR17, FR18: Admin can view all user accounts
     */
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        // FR20: Log admin action
        log.info("Admin viewing all users - Total count: {}", users.size());
        
        return users.stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
    }

    /**
     * UC-04.1: View users with filters
     * FR17, FR18: Admin can filter users
     */
    public List<UserResponse> getUsersByFilters(UserRole role, Boolean isDeleted, String search) {
        List<User> users = userRepository.findAll();
        
        // Apply filters
        if (role != null) {
            users = users.stream()
                .filter(u -> u.getRole() == role)
                .collect(Collectors.toList());
        }
        
        if (isDeleted != null) {
            users = users.stream()
                .filter(u -> u.getIsDeleted().equals(isDeleted))
                .collect(Collectors.toList());
        }
        
        if (search != null && !search.isBlank()) {
            String searchLower = search.toLowerCase();
            users = users.stream()
                .filter(u -> u.getFullName().toLowerCase().contains(searchLower) ||
                           u.getContact().toLowerCase().contains(searchLower))
                .collect(Collectors.toList());
        }
        
        log.info("Admin filtered users - Filters: role={}, deleted={}, search={}, Results: {}", 
                 role, isDeleted, search, users.size());
        
        return users.stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
    }

    /**
     * UC-04.2: View user details
     * FR17: Admin can view detailed user information
     */
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        log.info("Admin viewing user details - User ID: {}", userId);
        
        return mapToUserResponse(user);
    }

    /**
     * UC-04.3: Edit user account details
     * FR17, FR22, FR23: Admin can edit user details with validation
     */
    @Transactional
    public UserResponse updateUser(Long adminId, Long userId, AdminUserUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Update fields if provided
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        
        // Update contact (email or phone)
        String newContact = request.getEmail() != null ? request.getEmail() : request.getPhone();
        if (newContact != null && !newContact.equals(user.getContact())) {
            // Check for duplicate
            if (userRepository.existsByContactIgnoreCase(newContact)) {
                throw new BusinessRuleViolationException("This contact is already in use");
            }
            user.setContact(newContact);
        }
        
        // Update role if provided
        if (request.getRole() != null && request.getRole() != user.getRole()) {
            user.setRole(request.getRole());
            log.info("Admin changed user role - User ID: {}, New Role: {}", userId, request.getRole());
        }
        
        user = userRepository.save(user);
        
        // FR20: Log admin action
        log.info("Admin updated user - Admin ID: {}, User ID: {}", adminId, userId);
        
        return mapToUserResponse(user);
    }

    /**
     * UC-04.4: Change user status (Active/Inactive/Suspended)
     * FR21, FR23: Admin can change user status with validation
     */
    @Transactional
    public UserResponse changeUserStatus(Long adminId, Long userId, Boolean isDeleted) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Update status
        if (isDeleted) {
            user.setIsDeleted(true);
            user.setDeletedAt(LocalDateTime.now());
        } else {
            user.setIsDeleted(false);
            user.setDeletedAt(null);
        }
        
        user = userRepository.save(user);
        
        // FR20: Log admin action
        log.info("Admin changed user status - Admin ID: {}, User ID: {}, Deleted: {}", 
                 adminId, userId, isDeleted);
        
        return mapToUserResponse(user);
    }

    /**
     * UC-04.5: Soft delete user account
     * FR19, FR23: Admin soft deletes user with confirmation
     */
    @Transactional
    public void deleteUser(Long adminId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // FR19: Soft delete
        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // FR20: Log admin action
        log.info("Admin deleted user - Admin ID: {}, User ID: {}", adminId, userId);
    }

    // ==================== EVENT MANAGEMENT (UC-04.6 - 04.10) ====================

    /**
     * UC-04.6: View all events
     * FR17, FR18: Admin can view all events
     */
    public List<EventResponse> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        
        log.info("Admin viewing all events - Total count: {}", events.size());
        
        return events.stream()
            .map(this::mapToEventResponse)
            .collect(Collectors.toList());
    }

    /**
     * UC-04.6: View events with filters
     * FR17, FR18: Admin can filter events
     */
    public List<EventResponse> getEventsByStatus(EventStatus status) {
        List<Event> events;
        
        if (status != null) {
            events = eventRepository.findByStatusOrderByStartDateAsc(status);
        } else {
            events = eventRepository.findAll();
        }
        
        log.info("Admin filtered events - Status: {}, Results: {}", status, events.size());
        
        return events.stream()
            .map(this::mapToEventResponse)
            .collect(Collectors.toList());
    }

    /**
     * UC-04.7: View event details
     * FR17: Admin can view detailed event information
     */
    public EventResponse getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        log.info("Admin viewing event details - Event ID: {}", eventId);
        
        return mapToEventResponse(event);
    }

    /**
     * UC-04.8: Edit event details
     * FR17, FR22, FR23: Admin can edit event with validation
     */
    @Transactional
    public EventResponse updateEvent(Long adminId, Long eventId, AdminEventUpdateRequest request) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        // FR15: Block updates to past events
        if (event.hasOccurred()) {
            throw new BusinessRuleViolationException("Cannot update event that has already occurred");
        }
        
        // Update fields if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            event.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        
        if (request.getEventType() != null) {
            event.setEventType(request.getEventType());
        }
        
        if (request.getStartDate() != null) {
            event.setStartDate(request.getStartDate());
        }
        
        if (request.getEndDate() != null) {
            event.setEndDate(request.getEndDate());
        }
        
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        
        if (request.getVenueName() != null) {
            event.setVenueName(request.getVenueName());
        }
        
        if (request.getMaxTicketQuantity() != null) {
            event.setMaxTicketQuantity(request.getMaxTicketQuantity());
        }
        
        if (request.getRefundAllowed() != null) {
            event.setRefundAllowed(request.getRefundAllowed());
        }
        
        if (request.getPosterImage() != null) {
            event.setPosterImage(request.getPosterImage());
        }
        
        event.setUpdatedAt(LocalDateTime.now());
        event = eventRepository.save(event);
        
        // FR20: Log admin action
        log.info("Admin updated event - Admin ID: {}, Event ID: {}", adminId, eventId);
        
        return mapToEventResponse(event);
    }

    /**
     * UC-04.9: Approve or reject event
     * Admin approval required for new events (FR17, FR20)
     */
    @Transactional
    public EventResponse approveEvent(Long adminId, Long eventId, EventApprovalRequest request) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        if (event.getStatus() != EventStatus.PENDING_APPROVAL) {
            throw new BusinessRuleViolationException("Event is not pending approval");
        }
        
        if (request.isApproved()) {
            event.setStatus(EventStatus.ACTIVE);
            log.info("Admin approved event - Admin ID: {}, Event ID: {}", adminId, eventId);
            
            // Notify organizer
            emailService.sendEventApprovalNotification(
                event.getOrganizer().getContact(),
                event.getName(),
                true,
                request.getAdminNotes()
            );
        } else {
            event.setStatus(EventStatus.CANCELLED);
            log.info("Admin rejected event - Admin ID: {}, Event ID: {}, Reason: {}", 
                     adminId, eventId, request.getAdminNotes());
            
            // Notify organizer
            emailService.sendEventApprovalNotification(
                event.getOrganizer().getContact(),
                event.getName(),
                false,
                request.getAdminNotes()
            );
        }
        
        event.setUpdatedAt(LocalDateTime.now());
        event = eventRepository.save(event);
        
        // FR20: Log admin action
        log.info("Admin processed event approval - Admin ID: {}, Event ID: {}, Approved: {}", 
                 adminId, eventId, request.isApproved());
        
        return mapToEventResponse(event);
    }

    /**
     * UC-04.9: Change event status
     * FR21, FR23: Admin can change event status
     */
    @Transactional
    public EventResponse changeEventStatus(Long adminId, Long eventId, EventStatus newStatus) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        event.setStatus(newStatus);
        event.setUpdatedAt(LocalDateTime.now());
        event = eventRepository.save(event);
        
        // FR20: Log admin action
        log.info("Admin changed event status - Admin ID: {}, Event ID: {}, New Status: {}", 
                 adminId, eventId, newStatus);
        
        return mapToEventResponse(event);
    }

    /**
     * UC-04.10: Soft delete event
     * FR19, FR23: Admin soft deletes event
     */
    @Transactional
    public void deleteEvent(Long adminId, Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        // Mark as cancelled (soft delete)
        event.setStatus(EventStatus.CANCELLED);
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);
        
        // FR20: Log admin action
        log.info("Admin deleted event - Admin ID: {}, Event ID: {}", adminId, eventId);
    }

    // ==================== KYC VERIFICATION ====================

    /**
     * View all pending KYC verifications
     * FR26: Admin reviews KYC requests
     */
    public List<OrganizerProfileResponse> getPendingKycVerifications() {
        List<OrganizerProfile> profiles = organizerProfileRepository.findPendingKycVerifications();
        
        log.info("Admin viewing pending KYC verifications - Count: {}", profiles.size());
        
        return profiles.stream()
            .map(this::mapToOrganizerProfileResponse)
            .collect(Collectors.toList());
    }

    /**
     * View KYC verification details
     * FR26: Admin reviews KYC details
     */
    public OrganizerProfileResponse getKycVerificationById(Long organizerId) {
        OrganizerProfile profile = organizerProfileRepository.findById(organizerId)
            .orElseThrow(() -> new ResourceNotFoundException("Organizer profile not found"));
        
        log.info("Admin viewing KYC details - Organizer ID: {}", organizerId);
        
        return mapToOrganizerProfileResponse(profile);
    }

    /**
     * Approve or reject KYC verification
     * FR26, FR11: Admin approval upgrades user to Verified Organizer
     */
    @Transactional
    public OrganizerProfileResponse processKycVerification(Long adminId, Long organizerId, KycApprovalRequest request) {
        OrganizerProfile profile = organizerProfileRepository.findById(organizerId)
            .orElseThrow(() -> new ResourceNotFoundException("Organizer profile not found"));
        
        if (!"PENDING_VERIFICATION".equals(profile.getKycStatus())) {
            throw new BusinessRuleViolationException("KYC verification is not pending");
        }
        
        User user = profile.getUser();
        
        if (request.isApproved()) {
            // Approve KYC
            profile.approveKyc(adminId);
            
            // FR11, FR26: Upgrade user role to VERIFIED_ORGANIZER
            user.setRole(UserRole.VERIFIED_ORGANIZER);
            userRepository.save(user);
            
            log.info("Admin approved KYC - Admin ID: {}, Organizer ID: {}", adminId, organizerId);
            
            // Notify organizer
            emailService.sendKycApprovalNotification(
                user.getContact(),
                user.getFullName(),
                true,
                request.getNotes()
            );
        } else {
            // Reject KYC
            profile.rejectKyc(request.getNotes());
            
            log.info("Admin rejected KYC - Admin ID: {}, Organizer ID: {}, Reason: {}", 
                     adminId, organizerId, request.getNotes());
            
            // Notify organizer
            emailService.sendKycApprovalNotification(
                user.getContact(),
                user.getFullName(),
                false,
                request.getNotes()
            );
        }
        
        profile = organizerProfileRepository.save(profile);
        
        // FR20: Log admin action
        log.info("Admin processed KYC verification - Admin ID: {}, Organizer ID: {}, Approved: {}", 
                 adminId, organizerId, request.isApproved());
        
        return mapToOrganizerProfileResponse(profile);
    }

    // ==================== REFUND APPROVAL ====================

    /**
     * View all pending refund requests
     * FR7: Admin reviews refund requests
     */
    public List<RefundResponse> getPendingRefunds() {
        List<RefundInfo> refunds = refundRepository.findByStatus(RefundStatus.PENDING_ADMIN_REVIEW);
        
        log.info("Admin viewing pending refunds - Count: {}", refunds.size());
        
        return refunds.stream()
            .map(this::mapToRefundResponse)
            .collect(Collectors.toList());
    }

    /**
     * Approve or reject refund request
     * FR7: Admin approval required for refunds
     * FR8: Invalidate ticket after refund
     */
    @Transactional
    public RefundResponse processRefund(Long adminId, Long refundId, RefundApprovalRequest request) {
        RefundInfo refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new ResourceNotFoundException("Refund request not found"));
        
        if (refund.getStatus() != RefundStatus.PENDING_ADMIN_REVIEW) {
            throw new BusinessRuleViolationException("Refund request is not pending review");
        }
        
        if (request.getApproved()) {
            // Determine refund amount (full or partial)
            BigDecimal refundAmount = request.getRefundAmount() != null ? 
                request.getRefundAmount() : refund.getRefundAmount();
            
            // Approve refund
            refund.approve(adminId, request.getAdminNotes());
            refund.setRefundAmount(refundAmount);
            refundRepository.save(refund);
            
            // Process refund through VNPAY
            try {
                boolean refundSuccess = vnPayService.processRefund(
                    refund.getTicket().getOrder().getOrderId().toString(),
                    refundAmount
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
                        refundAmount
                    );
                    
                    log.info("Admin approved refund - Admin ID: {}, Refund ID: {}, Amount: {}", 
                             adminId, refundId, refundAmount);
                } else {
                    throw new RuntimeException("VNPAY refund processing failed");
                }
            } catch (Exception e) {
                log.error("Refund processing failed - Refund ID: {}", refundId, e);
                throw new BusinessRuleViolationException("Failed to process refund through payment gateway");
            }
        } else {
            // Reject refund
            refund.reject(request.getAdminNotes());
            refundRepository.save(refund);
            
            // Send rejection notification
            emailService.sendRefundRejected(
                refund.getUser().getContact(),
                refund.getRequestId(),
                request.getAdminNotes()
            );
            
            log.info("Admin rejected refund - Admin ID: {}, Refund ID: {}, Reason: {}", 
                     adminId, refundId, request.getAdminNotes());
        }
        
        // FR20: Log admin action
        log.info("Admin processed refund - Admin ID: {}, Refund ID: {}, Approved: {}", 
                 adminId, refundId, request.getApproved());
        
        return mapToRefundResponse(refund);
    }

    // ==================== EVENT UPDATE REQUEST APPROVAL ====================

    /**
     * Get pending event update requests
     */
    public List<com.swd.ticketbook.dto.organizer.EventUpdateRequestResponse> getPendingUpdateRequests() {
        List<com.swd.ticketbook.entities.EventUpdateRequest> requests = updateRequestRepository
            .findByStatus("PENDING_REVIEW");
        
        log.info("Admin viewing pending update requests - Count: {}", requests.size());
        
        return requests.stream()
            .map(this::mapToUpdateRequestResponse)
            .collect(Collectors.toList());
    }

    /**
     * Approve or reject event update request
     */
    @Transactional
    public com.swd.ticketbook.dto.organizer.EventUpdateRequestResponse processUpdateRequest(
            Long adminId, Long requestId, boolean approved, String adminNotes) {
        
        com.swd.ticketbook.entities.EventUpdateRequest request = updateRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Update request not found"));

        if (!request.isPending()) {
            throw new BusinessRuleViolationException("Update request is not pending review");
        }

        Event event = request.getEvent();

        if (approved) {
            // Apply proposed changes
            if (request.getProposedName() != null) {
                event.setName(request.getProposedName());
            }
            if (request.getProposedDescription() != null) {
                event.setDescription(request.getProposedDescription());
            }
            if (request.getProposedStartDate() != null) {
                event.setStartDate(request.getProposedStartDate());
            }
            if (request.getProposedEndDate() != null) {
                event.setEndDate(request.getProposedEndDate());
            }
            if (request.getProposedLocation() != null) {
                event.setLocation(request.getProposedLocation());
            }
            if (request.getProposedVenueName() != null) {
                event.setVenueName(request.getProposedVenueName());
            }
            if (request.getProposedMaxTicketQuantity() != null) {
                event.setMaxTicketQuantity(request.getProposedMaxTicketQuantity());
            }
            if (request.getProposedRefundAllowed() != null) {
                event.setRefundAllowed(request.getProposedRefundAllowed());
            }

            event.setUpdatedAt(LocalDateTime.now());
            eventRepository.save(event);

            request.approve(adminId, adminNotes);
            
            log.info("Admin approved event update - Admin ID: {}, Request ID: {}", adminId, requestId);
        } else {
            request.reject(adminId, adminNotes);
            log.info("Admin rejected event update - Admin ID: {}, Request ID: {}", adminId, requestId);
        }

        updateRequestRepository.save(request);

        // Notify organizer
        emailService.sendEventUpdateRequestDecision(
            request.getOrganizer().getContact(),
            request.getOrganizer().getFullName(),
            event.getName(),
            approved,
            adminNotes
        );

        // FR20: Log admin action
        log.info("Admin processed event update request - Admin ID: {}, Request ID: {}, Approved: {}", 
                 adminId, requestId, approved);

        return mapToUpdateRequestResponse(request);
    }

    // ==================== WITHDRAWAL APPROVAL ====================

    /**
     * Get pending withdrawal requests
     */
    public List<com.swd.ticketbook.dto.organizer.WithdrawalResponse> getPendingWithdrawals() {
        List<com.swd.ticketbook.entities.WithdrawalRequest> requests = withdrawalRequestRepository
            .findPendingWithdrawals();
        
        log.info("Admin viewing pending withdrawals - Count: {}", requests.size());
        
        return requests.stream()
            .map(this::mapToWithdrawalResponse)
            .collect(Collectors.toList());
    }

    /**
     * Approve or reject withdrawal request
     */
    @Transactional
    public com.swd.ticketbook.dto.organizer.WithdrawalResponse processWithdrawal(
            Long adminId, Long requestId, boolean approved, String adminNotes) {
        
        com.swd.ticketbook.entities.WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Withdrawal request not found"));

        if (!request.isPending()) {
            throw new BusinessRuleViolationException("Withdrawal request is not pending review");
        }

        if (approved) {
            request.approve(adminId, adminNotes);
            request.markAsProcessing();
            withdrawalRequestRepository.save(request);

            // TODO: Process actual bank transfer
            // For now, mark as completed immediately
            String transactionRef = "TXN-" + System.currentTimeMillis();
            request.complete(transactionRef);
            withdrawalRequestRepository.save(request);

            // Send confirmation
            emailService.sendWithdrawalApproved(
                request.getOrganizer().getContact(),
                request.getOrganizer().getFullName(),
                request.getAmount(),
                transactionRef
            );

            log.info("Admin approved withdrawal - Admin ID: {}, Request ID: {}, Amount: {}", 
                     adminId, requestId, request.getAmount());
        } else {
            request.reject(adminId, adminNotes);
            withdrawalRequestRepository.save(request);

            // Send rejection notification
            emailService.sendWithdrawalRejected(
                request.getOrganizer().getContact(),
                request.getOrganizer().getFullName(),
                request.getAmount(),
                adminNotes
            );

            log.info("Admin rejected withdrawal - Admin ID: {}, Request ID: {}, Reason: {}", 
                     adminId, requestId, adminNotes);
        }

        // FR20: Log admin action
        log.info("Admin processed withdrawal - Admin ID: {}, Request ID: {}, Approved: {}", 
                 adminId, requestId, approved);

        return mapToWithdrawalResponse(request);
    }

    // ==================== HELPER METHODS ====================

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setFullName(user.getFullName());
        
        // Map contact to email or phone
        if (user.isEmail()) {
            response.setEmail(user.getContact());
            response.setIsEmailVerified(user.getIsVerified());
        } else if (user.isPhone()) {
            response.setPhone(user.getContact());
            response.setIsPhoneVerified(user.getIsVerified());
        }
        
        response.setRole(user.getRole());
        response.setRegistrationDate(user.getRegistrationDate());
        response.setLastLogin(user.getLastLogin());
        response.setOauthProvider(user.getOauthProvider());
        return response;
    }

    private EventResponse mapToEventResponse(Event event) {
        EventResponse response = new EventResponse();
        response.setEventId(event.getEventId());
        response.setOrganizerId(event.getOrganizer().getUserId());
        response.setOrganizerName(event.getOrganizer().getFullName());
        response.setName(event.getName());
        response.setDescription(event.getDescription());
        response.setEventType(event.getEventType());
        response.setStartDate(event.getStartDate());
        response.setEndDate(event.getEndDate());
        response.setLocation(event.getLocation());
        response.setVenueName(event.getVenueName());
        response.setMaxTicketQuantity(event.getMaxTicketQuantity());
        response.setStatus(event.getStatus());
        response.setRefundAllowed(event.getRefundAllowed());
        response.setPosterImage(event.getPosterImage());
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());
        return response;
    }

    private OrganizerProfileResponse mapToOrganizerProfileResponse(OrganizerProfile profile) {
        OrganizerProfileResponse response = new OrganizerProfileResponse();
        response.setOrganizerId(profile.getOrganizerId());
        response.setUserId(profile.getUser().getUserId());
        response.setUserName(profile.getUser().getFullName());
        response.setUserContact(profile.getUser().getContact());
        response.setOrganizationName(profile.getOrganizationName());
        response.setBusinessRegistrationNumber(profile.getBusinessRegistrationNumber());
        response.setTaxId(profile.getTaxId());
        response.setBusinessAddress(profile.getBusinessAddress());
        response.setContactPerson(profile.getContactPerson());
        response.setBusinessPhone(profile.getBusinessPhone());
        response.setBusinessEmail(profile.getBusinessEmail());
        response.setBankName(profile.getBankName());
        response.setBankAccountNumber(profile.getBankAccountNumber());
        response.setBankAccountHolderName(profile.getBankAccountHolderName());
        response.setBankBranch(profile.getBankBranch());
        response.setIdDocumentPath(profile.getIdDocumentPath());
        response.setBusinessRegistrationDocumentPath(profile.getBusinessRegistrationDocumentPath());
        response.setTaxDocumentPath(profile.getTaxDocumentPath());
        response.setAddressProofDocumentPath(profile.getAddressProofDocumentPath());
        response.setKycStatus(profile.getKycStatus());
        response.setKycSubmittedAt(profile.getKycSubmittedAt());
        response.setKycApprovedAt(profile.getKycApprovedAt());
        response.setKycApprovedBy(profile.getKycApprovedBy());
        response.setKycRejectionReason(profile.getKycRejectionReason());
        response.setCreatedAt(profile.getCreatedAt());
        return response;
    }

    private RefundResponse mapToRefundResponse(RefundInfo refund) {
        RefundResponse response = new RefundResponse();
        response.setRequestId(refund.getRequestId());
        response.setTicketId(refund.getTicket().getTicketId());
        response.setRefundAmount(refund.getRefundAmount());
        response.setReason(refund.getReason());
        response.setStatus(refund.getStatus());
        response.setRequestDate(refund.getRequestDate());
        response.setProcessedDate(refund.getProcessedDate());
        response.setAdminNotes(refund.getAdminNotes());
        return response;
    }

    private com.swd.ticketbook.dto.organizer.EventUpdateRequestResponse mapToUpdateRequestResponse(
            com.swd.ticketbook.entities.EventUpdateRequest request) {
        
        com.swd.ticketbook.dto.organizer.EventUpdateRequestResponse response = 
            new com.swd.ticketbook.dto.organizer.EventUpdateRequestResponse();
        
        response.setRequestId(request.getRequestId());
        response.setEventId(request.getEvent().getEventId());
        response.setEventName(request.getEvent().getName());
        response.setOrganizerId(request.getOrganizer().getUserId());
        response.setOrganizerName(request.getOrganizer().getFullName());
        response.setJustification(request.getJustification());
        response.setStatus(request.getStatus());
        response.setRequestedAt(request.getRequestedAt());
        response.setReviewedAt(request.getReviewedAt());
        response.setAdminNotes(request.getAdminNotes());
        
        // Build summary of proposed changes
        StringBuilder changes = new StringBuilder();
        if (request.getProposedName() != null) changes.append("Name, ");
        if (request.getProposedDescription() != null) changes.append("Description, ");
        if (request.getProposedLocation() != null) changes.append("Location, ");
        if (request.getProposedStartDate() != null) changes.append("Start Date, ");
        if (request.getProposedEndDate() != null) changes.append("End Date, ");
        if (changes.length() > 0) {
            changes.setLength(changes.length() - 2);
        }
        response.setProposedChanges(changes.toString());
        
        return response;
    }

    private com.swd.ticketbook.dto.organizer.WithdrawalResponse mapToWithdrawalResponse(
            com.swd.ticketbook.entities.WithdrawalRequest request) {
        
        com.swd.ticketbook.dto.organizer.WithdrawalResponse response = 
            new com.swd.ticketbook.dto.organizer.WithdrawalResponse();
        
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

