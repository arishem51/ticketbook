package com.swd.ticketbook.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing Organizer Profile for Verified Organizers.
 * Created after Admin approves KYC verification (FR26).
 */
@Entity
@Table(name = "organizer_profiles")
@Data
@NoArgsConstructor
public class OrganizerProfile {

    @Id
    @Column(name = "organizer_id")
    private Long organizerId; // Same as User ID

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "organizer_id")
    private User user;

    @Column(name = "organization_name", length = 255)
    private String organizationName;

    @Column(name = "business_registration_number", length = 100)
    private String businessRegistrationNumber;

    @Column(name = "tax_id", length = 100)
    private String taxId;

    @Column(name = "business_address", length = 500)
    private String businessAddress;

    @Column(name = "contact_person", length = 255)
    private String contactPerson;

    @Column(name = "business_phone", length = 20)
    private String businessPhone;

    @Column(name = "business_email", length = 255)
    private String businessEmail;

    // Bank account information for revenue withdrawal
    @NotNull
    @Column(name = "bank_name", nullable = false, length = 255)
    private String bankName;

    @NotNull
    @Column(name = "bank_account_number", nullable = false, length = 100)
    private String bankAccountNumber;

    @NotNull
    @Column(name = "bank_account_holder_name", nullable = false, length = 255)
    private String bankAccountHolderName;

    @Column(name = "bank_branch", length = 255)
    private String bankBranch;

    // KYC Documents
    @Column(name = "id_document_path", length = 500)
    private String idDocumentPath;

    @Column(name = "business_registration_document_path", length = 500)
    private String businessRegistrationDocumentPath;

    @Column(name = "tax_document_path", length = 500)
    private String taxDocumentPath;

    @Column(name = "address_proof_document_path", length = 500)
    private String addressProofDocumentPath;

    // Verification status
    @Column(name = "kyc_status", length = 50, nullable = false)
    private String kycStatus = "PENDING_VERIFICATION";

    @Column(name = "kyc_submitted_at")
    private LocalDateTime kycSubmittedAt;

    @Column(name = "kyc_approved_at")
    private LocalDateTime kycApprovedAt;

    @Column(name = "kyc_approved_by")
    private Long kycApprovedBy;

    @Column(name = "kyc_rejection_reason", length = 1000)
    private String kycRejectionReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Custom constructor
    public OrganizerProfile(User user) {
        this.user = user;
        this.organizerId = user.getUserId();
        this.createdAt = LocalDateTime.now();
        this.kycSubmittedAt = LocalDateTime.now();
        this.kycStatus = "PENDING_VERIFICATION";
    }

    // Business methods
    public boolean isKycApproved() {
        return "APPROVED".equals(kycStatus);
    }

    public void approveKyc(Long adminId) {
        this.kycStatus = "APPROVED";
        this.kycApprovedAt = LocalDateTime.now();
        this.kycApprovedBy = adminId;
        this.kycRejectionReason = null;
    }

    public void rejectKyc(String reason) {
        this.kycStatus = "REJECTED";
        this.kycRejectionReason = reason;
        this.kycApprovedAt = null;
        this.kycApprovedBy = null;
    }
}
