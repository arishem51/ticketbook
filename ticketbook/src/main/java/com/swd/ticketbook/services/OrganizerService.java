package com.swd.ticketbook.services;

import com.swd.ticketbook.dto.admin.OrganizerProfileResponse;
import com.swd.ticketbook.dto.organizer.KycSubmissionRequest;
import com.swd.ticketbook.entities.OrganizerProfile;
import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.enums.UserRole;
import com.swd.ticketbook.exceptions.BusinessRuleViolationException;
import com.swd.ticketbook.exceptions.ResourceNotFoundException;
import com.swd.ticketbook.repositories.OrganizerProfileRepository;
import com.swd.ticketbook.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for Organizer-related operations
 * Handles KYC submission (UC-03.1 AF 3.1.1)
 * Business Rules: FR26
 */
@Service
public class OrganizerService {

    private static final Logger log = LoggerFactory.getLogger(OrganizerService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizerProfileRepository organizerProfileRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private EmailService emailService;

    /**
     * UC-03.1 AF 3.1.1: Submit KYC verification
     * FR26: Customer submits KYC, Admin approves, upgrades to Verified Organizer
     */
    @Transactional
    public OrganizerProfileResponse submitKycVerification(Long userId, KycSubmissionRequest request) {
        // Get user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Only Customers can submit KYC
        if (user.getRole() != UserRole.CUSTOMER) {
            throw new BusinessRuleViolationException("Only Customer accounts can apply for organizer verification");
        }

        // Check if user already has a profile
        if (organizerProfileRepository.existsByUser(user)) {
            OrganizerProfile existingProfile = organizerProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile error"));

            if ("PENDING_VERIFICATION".equals(existingProfile.getKycStatus())) {
                throw new BusinessRuleViolationException("You already have a pending KYC verification request");
            }

            if ("APPROVED".equals(existingProfile.getKycStatus())) {
                throw new BusinessRuleViolationException("Your KYC has already been approved");
            }

            // If previously rejected, allow resubmission
            if ("REJECTED".equals(existingProfile.getKycStatus())) {
                updateExistingProfile(existingProfile, request);
                existingProfile.setKycStatus("PENDING_VERIFICATION");
                existingProfile.setKycSubmittedAt(LocalDateTime.now());
                existingProfile.setKycRejectionReason(null);
                existingProfile = organizerProfileRepository.save(existingProfile);

                log.info("KYC resubmitted - User ID: {}", userId);
                return mapToOrganizerProfileResponse(existingProfile);
            }
        }

        // Create new organizer profile
        OrganizerProfile profile = new OrganizerProfile(user);
        
        // Set business information
        profile.setOrganizationName(request.getOrganizationName());
        profile.setBusinessRegistrationNumber(request.getBusinessRegistrationNumber());
        profile.setTaxId(request.getTaxId());
        profile.setBusinessAddress(request.getBusinessAddress());
        profile.setContactPerson(request.getContactPerson());
        profile.setBusinessPhone(request.getBusinessPhone());
        profile.setBusinessEmail(request.getBusinessEmail());
        
        // Set bank information
        profile.setBankName(request.getBankName());
        profile.setBankAccountNumber(request.getBankAccountNumber());
        profile.setBankAccountHolderName(request.getBankAccountHolderName());
        profile.setBankBranch(request.getBankBranch());
        
        // Set document paths
        profile.setIdDocumentPath(request.getIdDocumentPath());
        profile.setBusinessRegistrationDocumentPath(request.getBusinessRegistrationDocumentPath());
        profile.setTaxDocumentPath(request.getTaxDocumentPath());
        profile.setAddressProofDocumentPath(request.getAddressProofDocumentPath());

        profile = organizerProfileRepository.save(profile);

        // Log action
        auditLogService.logKycSubmission(user);

        // Send confirmation email
        emailService.sendKycSubmissionConfirmation(user.getContact(), user.getFullName());

        log.info("KYC submitted - User ID: {}, Profile ID: {}", userId, profile.getOrganizerId());

        return mapToOrganizerProfileResponse(profile);
    }

    /**
     * Get user's organizer profile
     */
    public OrganizerProfileResponse getOrganizerProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        OrganizerProfile profile = organizerProfileRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Organizer profile not found"));

        return mapToOrganizerProfileResponse(profile);
    }

    /**
     * Check KYC status
     */
    public String getKycStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        OrganizerProfile profile = organizerProfileRepository.findByUser(user)
            .orElse(null);

        if (profile == null) {
            return "NOT_SUBMITTED";
        }

        return profile.getKycStatus();
    }

    // Helper methods

    private void updateExistingProfile(OrganizerProfile profile, KycSubmissionRequest request) {
        profile.setOrganizationName(request.getOrganizationName());
        profile.setBusinessRegistrationNumber(request.getBusinessRegistrationNumber());
        profile.setTaxId(request.getTaxId());
        profile.setBusinessAddress(request.getBusinessAddress());
        profile.setContactPerson(request.getContactPerson());
        profile.setBusinessPhone(request.getBusinessPhone());
        profile.setBusinessEmail(request.getBusinessEmail());
        profile.setBankName(request.getBankName());
        profile.setBankAccountNumber(request.getBankAccountNumber());
        profile.setBankAccountHolderName(request.getBankAccountHolderName());
        profile.setBankBranch(request.getBankBranch());
        profile.setIdDocumentPath(request.getIdDocumentPath());
        profile.setBusinessRegistrationDocumentPath(request.getBusinessRegistrationDocumentPath());
        profile.setTaxDocumentPath(request.getTaxDocumentPath());
        profile.setAddressProofDocumentPath(request.getAddressProofDocumentPath());
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
}

