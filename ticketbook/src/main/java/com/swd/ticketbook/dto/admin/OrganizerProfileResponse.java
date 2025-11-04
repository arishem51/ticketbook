package com.swd.ticketbook.dto.admin;

import java.time.LocalDateTime;

/**
 * DTO for OrganizerProfile response (Admin view)
 */
public class OrganizerProfileResponse {

    private Long organizerId;
    private Long userId;
    private String userName;
    private String userContact;
    private String organizationName;
    private String businessRegistrationNumber;
    private String taxId;
    private String businessAddress;
    private String contactPerson;
    private String businessPhone;
    private String businessEmail;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountHolderName;
    private String bankBranch;
    private Boolean isBankVerified;
    private String idDocumentPath;
    private String businessRegistrationDocumentPath;
    private String taxDocumentPath;
    private String addressProofDocumentPath;
    private String kycStatus;
    private LocalDateTime kycSubmittedAt;
    private LocalDateTime kycApprovedAt;
    private Long kycApprovedBy;
    private String kycRejectionReason;
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserContact() {
        return userContact;
    }

    public void setUserContact(String userContact) {
        this.userContact = userContact;
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
}

