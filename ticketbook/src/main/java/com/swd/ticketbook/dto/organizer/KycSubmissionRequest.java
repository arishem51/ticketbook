package com.swd.ticketbook.dto.organizer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for KYC submission by Customer (UC-03.1 AF 3.1.1)
 * Business Rules: FR26
 */
public class KycSubmissionRequest {

    @NotBlank(message = "Organization name is required")
    @Size(max = 255, message = "Organization name must not exceed 255 characters")
    private String organizationName;

    @NotBlank(message = "Business registration number is required")
    @Size(max = 100, message = "Business registration number must not exceed 100 characters")
    private String businessRegistrationNumber;

    @NotBlank(message = "Tax ID is required")
    @Size(max = 100, message = "Tax ID must not exceed 100 characters")
    private String taxId;

    @NotBlank(message = "Business address is required")
    @Size(max = 500, message = "Business address must not exceed 500 characters")
    private String businessAddress;

    @NotBlank(message = "Contact person is required")
    @Size(max = 255, message = "Contact person must not exceed 255 characters")
    private String contactPerson;

    @NotBlank(message = "Business phone is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Business phone must be 10-15 digits")
    private String businessPhone;

    @NotBlank(message = "Business email is required")
    @Email(message = "Invalid email format")
    private String businessEmail;

    @NotBlank(message = "Bank name is required")
    @Size(max = 255, message = "Bank name must not exceed 255 characters")
    private String bankName;

    @NotBlank(message = "Bank account number is required")
    @Size(max = 100, message = "Bank account number must not exceed 100 characters")
    private String bankAccountNumber;

    @NotBlank(message = "Bank account holder name is required")
    @Size(max = 255, message = "Bank account holder name must not exceed 255 characters")
    private String bankAccountHolderName;

    @Size(max = 255, message = "Bank branch must not exceed 255 characters")
    private String bankBranch;

    // Document paths (uploaded separately via file upload endpoint)
    @NotBlank(message = "ID document is required")
    private String idDocumentPath;

    @NotBlank(message = "Business registration document is required")
    private String businessRegistrationDocumentPath;

    private String taxDocumentPath;

    private String addressProofDocumentPath;

    // Getters and Setters
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
}

