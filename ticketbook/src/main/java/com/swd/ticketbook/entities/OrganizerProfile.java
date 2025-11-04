package com.swd.ticketbook.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing Organizer Profile for Verified Organizers.
 * Created after Admin approves KYC verification (FR26).
 */
@Entity
@Table(name = "organizer_profiles")
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

    @Column(name = "is_bank_verified", nullable = false)
    private Boolean isBankVerified = false;

    // KYC Documents
    @Column(name = "id_document_path", length = 500)
    private String idDocumentPath;

    @Column(name = "business_registration_document_path", length = 500)
    private String businessRegistrationDocumentPath;

    @Column(name = "tax_document_path", length = 500)
    private String taxDocumentPath;

    @Column(name = "address_proof_document_path", length = 500)
    private String addressProofDocumentPath;

    @Column(name = "payment_certificate_file", length = 500)
    private String paymentCertificateFile;

    // Verification status
    @Column(name = "kyc_status", length = 50, nullable = false)
    private String kycStatus = "PENDING_VERIFICATION"; // PENDING_VERIFICATION, APPROVED, REJECTED

    @Column(name = "kyc_submitted_at")
    private LocalDateTime kycSubmittedAt;

    @Column(name = "kyc_approved_at")
    private LocalDateTime kycApprovedAt;

    @Column(name = "kyc_approved_by")
    private Long kycApprovedBy; // Admin user ID

    @Column(name = "kyc_rejection_reason", length = 1000)
    private String kycRejectionReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public OrganizerProfile() {
        this.createdAt = LocalDateTime.now();
        this.kycSubmittedAt = LocalDateTime.now();
    }

    public OrganizerProfile(User user) {
        this();
        this.user = user;
        this.organizerId = user.getUserId();
    }

    // Getters and Setters
    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.organizerId = user.getUserId();
        }
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getBusinessRegistrationNumber() {
        return businessRegistrationNumber;
    }

    public void setBusinessRegistrationNumber(String businessRegistrationNumber) {
        this.businessRegistrationNumber = businessRegistrationNumber;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getBusinessPhone() {
        return businessPhone;
    }

    public void setBusinessPhone(String businessPhone) {
        this.businessPhone = businessPhone;
    }

    public String getBusinessEmail() {
        return businessEmail;
    }

    public void setBusinessEmail(String businessEmail) {
        this.businessEmail = businessEmail;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankAccountHolderName() {
        return bankAccountHolderName;
    }

    public void setBankAccountHolderName(String bankAccountHolderName) {
        this.bankAccountHolderName = bankAccountHolderName;
    }

    public String getBankBranch() {
        return bankBranch;
    }

    public void setBankBranch(String bankBranch) {
        this.bankBranch = bankBranch;
    }

    public Boolean getIsBankVerified() {
        return isBankVerified;
    }

    public void setIsBankVerified(Boolean isBankVerified) {
        this.isBankVerified = isBankVerified;
    }

    public String getIdDocumentPath() {
        return idDocumentPath;
    }

    public void setIdDocumentPath(String idDocumentPath) {
        this.idDocumentPath = idDocumentPath;
    }

    public String getBusinessRegistrationDocumentPath() {
        return businessRegistrationDocumentPath;
    }

    public void setBusinessRegistrationDocumentPath(String businessRegistrationDocumentPath) {
        this.businessRegistrationDocumentPath = businessRegistrationDocumentPath;
    }

    public String getTaxDocumentPath() {
        return taxDocumentPath;
    }

    public void setTaxDocumentPath(String taxDocumentPath) {
        this.taxDocumentPath = taxDocumentPath;
    }

    public String getAddressProofDocumentPath() {
        return addressProofDocumentPath;
    }

    public void setAddressProofDocumentPath(String addressProofDocumentPath) {
        this.addressProofDocumentPath = addressProofDocumentPath;
    }

    public String getPaymentCertificateFile() {
        return paymentCertificateFile;
    }

    public void setPaymentCertificateFile(String paymentCertificateFile) {
        this.paymentCertificateFile = paymentCertificateFile;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public LocalDateTime getKycSubmittedAt() {
        return kycSubmittedAt;
    }

    public void setKycSubmittedAt(LocalDateTime kycSubmittedAt) {
        this.kycSubmittedAt = kycSubmittedAt;
    }

    public LocalDateTime getKycApprovedAt() {
        return kycApprovedAt;
    }

    public void setKycApprovedAt(LocalDateTime kycApprovedAt) {
        this.kycApprovedAt = kycApprovedAt;
    }

    public Long getKycApprovedBy() {
        return kycApprovedBy;
    }

    public void setKycApprovedBy(Long kycApprovedBy) {
        this.kycApprovedBy = kycApprovedBy;
    }

    public String getKycRejectionReason() {
        return kycRejectionReason;
    }

    public void setKycRejectionReason(String kycRejectionReason) {
        this.kycRejectionReason = kycRejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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

