# Database Physical Schema Definition

## Overview
This document provides the complete database schema definition with DDL specifications, relationships, and mappings to use cases, business rules, and non-functional requirements.

---

## Table: CATEGORY

| Column | DDL Specification | Role & UC/BR/NFR Reference |
|--------|-------------------|---------------------------|
| CategoryID | INT (PK, IDENTITY(1,1), NOT NULL) | Category Identifier. Primary key for event categorization. |
| Name | NVARCHAR(100) (UNIQUE, NOT NULL) | Category Name (Unique). Used for event classification and filtering. |
| Description | NVARCHAR(500) (NULL) | Category Description. Optional descriptive text for category information. |

**Relationships:**
- One-to-Many with EVENT (CategoryID → EVENT.CategoryID)

**Business Rules:**
- Category names must be unique across the system
- Categories are used for event classification and search

---

## Table: USER

| Column | DDL Specification | Role & UC/BR/NFR Reference |
|--------|-------------------|---------------------------|
| UserID | INT (PK, IDENTITY(1,1), NOT NULL) | User Identifier. Primary key for user accounts. |
| FullName | NVARCHAR(255) (NOT NULL) | User's full name. Required for account creation and display. |
| Contact | NVARCHAR(320) (UNIQUE, NOT NULL) | Login Contact (Email/Phone, Unique). **UC-01.1, UC-01.2**: Login identifier. **FR9**: Must be unique across all users. Supports both email and phone number formats. |
| PasswordHash | VARCHAR(128) (NOT NULL) | Hashed Password. **FR1**: MD5 hashed password (Note: MD5 is deprecated, consider BCrypt for production). **NF-04**: Security requirement. |
| Role | NVARCHAR(50) (NOT NULL, DEFAULT 'Customer') | User Role. **FR11**: Default role is 'Customer' at registration. Values: 'CUSTOMER', 'VERIFIED_ORGANIZER', 'ADMIN'. **FR17**: Role-based access control. |
| AccountStatus | NVARCHAR(50) (NOT NULL, DEFAULT 'Active') | Account Status. **FR21**: Account status management. Values: 'Active', 'Inactive', 'Suspended'. Relevant to account lock functionality. |
| FailedLoginAttempts | INT (NOT NULL, DEFAULT 0) | Security tracking. **FR12**: Tracks failed login attempts. Account locked for 30 minutes after 5 failed attempts. **UC-01.2**: Login security. |
| RegistrationDate | DATETIME2 (NOT NULL, DEFAULT GETDATE()) | Registration Date. **UC-01.1**: Registration timestamp. |
| LastLogin | DATETIME2 (NULL) | Last Login Time. Tracks user's most recent login activity. **UC-01.2**: Login tracking. |
| IsDeleted | BIT (NOT NULL, DEFAULT 0) | Soft Delete Flag. **FR19**: Soft delete implementation. **FR13**: Only Customers can self-delete. |
| DeletedAt | DATETIME2 (NULL) | Deletion Timestamp. Records when account was soft deleted. |
| OAuthProvider | NVARCHAR(50) (NULL) | OAuth Provider. **UC-01.1**: Supports Google OAuth registration. Values: 'GOOGLE', 'EMAIL', 'PHONE'. |
| OAuthProviderId | NVARCHAR(255) (NULL) | OAuth Provider ID. External OAuth identifier for social login. |
| IsVerified | BIT (NOT NULL, DEFAULT 0) | Verification Status. Indicates if contact (email/phone) is verified. **UC-01.1**: Registration verification. |
| AccountLockedUntil | DATETIME2 (NULL) | Account Lock Expiry. **FR12**: Timestamp when account lock expires (30 minutes after 5 failed attempts). |

**Relationships:**
- One-to-Many with ORDER (UserID → ORDER.UserID)
- One-to-Many with EVENT (UserID → EVENT.OrganizerID)
- One-to-Many with SESSION (UserID → SESSION.UserID)
- One-to-One with ORGANIZER_PROFILE (UserID → ORGANIZER_PROFILE.OrganizerID)
- One-to-Many with REFUND (UserID → REFUND.UserID)
- One-to-Many with SUPPORT_REQUEST (UserID → SUPPORT_REQUEST.UserID)
- One-to-Many with EVENT_UPDATE_REQUEST (UserID → EVENT_UPDATE_REQUEST.SubmittedByUserID)
- One-to-Many with PASSWORD_RESET_TOKEN (UserID → PASSWORD_RESET_TOKEN.UserID)

**Business Rules:**
- **FR9**: Contact (email/phone) must be unique
- **FR10**: Password must be ≥8 characters with letters and numbers
- **FR11**: Default role is 'Customer' at registration
- **FR12**: Account locked for 30 minutes after 5 failed login attempts
- **FR13**: Only Customers can self-delete accounts
- **FR17**: Role-based access control enforced

---

## Table: VERIFICATION_CODE

| Column | DDL Specification | Role & UC/BR/NFR Reference |
|--------|-------------------|---------------------------|
| CodeID | BIGINT (PK, IDENTITY(1,1), NOT NULL) | Verification Code Identifier. Primary key for verification codes. |
| Contact | NVARCHAR(320) (NOT NULL) | Contact for Verification. Email or phone number receiving the code. **UC-01.1**: Registration verification. **UC-01.3**: Password reset verification. |
| CodeValue | VARCHAR(10) (NOT NULL) | Verification Code. 6-digit numeric code sent via SMS/email. |
| ExpiryTime | DATETIME2 (NOT NULL) | Expiration Time. **UC-01.1, UC-01.3**: Code expires after 5 minutes. |
| Purpose | NVARCHAR(50) (NOT NULL) | Code Purpose. Values: 'Register', 'Reset Password', 'Change Email', 'Change Phone'. Defines the verification context. |
| CreatedAt | DATETIME2 (NOT NULL, DEFAULT GETDATE()) | Creation Timestamp. Records when code was generated. |

**Business Rules:**
- Verification codes expire after 5 minutes
- Maximum 3 attempts per verification code
- Codes are single-use only

---

## Table: EVENT

| Column | DDL Specification | Role & UC/BR/NFR Reference |
|--------|-------------------|---------------------------|
| EventID | INT (PK, IDENTITY(1,1), NOT NULL) | Event Identifier. **FR2**: Unique auto-generated event ID. Primary key. |
| OrganizerID | INT (FK → USER.UserID, NOT NULL) | Event Organizer. **UC-03.1**: Organizer who creates the event. **FR17, FR26**: Only Verified Organizer role can create events. |
| CategoryID | INT (FK → CATEGORY.CategoryID, NOT NULL) | Event Category. Links event to category for classification. |
| EventName | NVARCHAR(255) (NOT NULL) | Event Name. Display name of the event. |
| Description | NVARCHAR(MAX) (NULL) | Event Description. Detailed information about the event. |
| EventType | NVARCHAR(100) (NULL) | Event Type. Classification of event type (e.g., 'Concert', 'Conference'). |
| StartDate | DATETIME2 (NOT NULL) | Event Start Date/Time. **UC-03.1**: Event must be in future. **FR15**: No modifications after event occurred. |
| EndDate | DATETIME2 (NOT NULL) | Event End Date/Time. **UC-03.1**: Must be after StartDate. |
| Location | NVARCHAR(500) (NULL) | Event Location. Physical address or location description. |
| VenueName | NVARCHAR(255) (NULL) | Venue Name. Name of the venue where event takes place. |
| MaxTicketQuantity | INT (NULL) | Maximum Ticket Quantity. **FR14**: Maximum tickets per order set by organizer. |
| Status | NVARCHAR(50) (NOT NULL, DEFAULT 'Draft') | Event Status. **UC-04.5**: Event status management. Values: 'DRAFT', 'PENDING_APPROVAL', 'ACTIVE', 'INACTIVE', 'CANCELLED', 'COMPLETED'. **UC-03.1**: New events start as 'Pending Approval', require Admin approval. |
| PosterImage | NVARCHAR(500) (NULL) | Poster Image URL. Path to event poster/image. |
| OrganizerCommittee | NVARCHAR(1000) (NULL) | Organizer Committee. Information about organizing committee. |
| RefundAllowed | BIT (NOT NULL, DEFAULT 0) | Refund Allowed Flag. **FR7**: Organizer's refund policy. Determines if refunds are allowed for this event. |
| CreatedAt | DATETIME2 (NOT NULL, DEFAULT GETDATE()) | Creation Date. **UC-03.1**: Event creation timestamp. **FR20**: Audit logging. |
| UpdatedAt | DATETIME2 (NULL) | Last Update Date. Tracks when event was last modified. |

**Relationships:**
- Many-to-One with USER (OrganizerID → USER.UserID)
- Many-to-One with CATEGORY (CategoryID → CATEGORY.CategoryID)
- One-to-Many with TICKET_TYPE (EventID → TICKET_TYPE.EventID)
- One-to-Many with ORDER (EventID → ORDER.EventID)
- One-to-Many with EVENT_UPDATE_REQUEST (EventID → EVENT_UPDATE_REQUEST.EventID)
- One-to-Many with SUPPORT_REQUEST (EventID → SUPPORT_REQUEST.EventID)

**Business Rules:**
- **FR2**: Unique auto-generated event ID
- **FR15**: No modifications to events that have occurred
- **FR17, FR26**: Only Verified Organizer can create events
- **UC-03.1**: Event must be approved by Admin before going live
- **FR20**: Event creation and updates are logged

---

## Table: EVENT_UPDATE_REQUEST

| Column | DDL Specification | Role & UC/BR/NFR Reference |
|--------|-------------------|---------------------------|
| RequestID | INT (PK, IDENTITY(1,1), NOT NULL) | Request Identifier. Primary key for update requests. |
| EventID | INT (FK → EVENT.EventID, NOT NULL) | Event to Update. Links request to specific event. **UC-03.2**: Event update workflow. |
| SubmittedByUserID | INT (FK → USER.UserID, NOT NULL) | Request Submitter. **UC-03.2, FR17, FR26**: Organizer who requests the update. Only event creator (Verified Organizer) can request updates. |
| RequestStatus | NVARCHAR(50) (NOT NULL, DEFAULT 'Pending') | Approval Status. **UC-04.6**: Approve/Reject workflow. Values: 'PENDING_REVIEW', 'APPROVED', 'REJECTED'. |
| RequestDate | DATETIME2 (NOT NULL, DEFAULT GETDATE()) | Request Date. Timestamp when update was requested. |
| ApprovalDate | DATETIME2 (NULL) | Admin Approval Date. **UC-04.6**: Timestamp when Admin reviewed the request. |
| AdminID | INT (FK → USER.UserID, NULL) | Processing Admin. **UC-04.6**: Admin who approved/rejected the request. NULL if pending. |
| Justification | NVARCHAR(MAX) (NOT NULL) | Change Justification. **UC-03.2**: Required justification text from organizer explaining the changes. |
| ChangeDetails | NVARCHAR(MAX) (NULL) | Change Content. **UC-03.2**: JSON/XML format storing proposed changes (name, description, location, dates, etc.). |

**Relationships:**
- Many-to-One with EVENT (EventID → EVENT.EventID)
- Many-to-One with USER (SubmittedByUserID → USER.UserID)
- Many-to-One with USER (AdminID → USER.UserID)

**Business Rules:**
- **FR3**: No ticket price changes after sales started
- **FR15**: No updates to events that already occurred
- **FR17, FR26**: Only event creator (Verified Organizer) can request updates
- **UC-03.2**: All updates require Admin approval
- **FR20**: Update requests and Admin decisions are logged

---

## Table: ORDER

| Column | DDL Specification | Role & UC/BR/NFR Reference |
|--------|-------------------|---------------------------|
| OrderID | INT (PK, IDENTITY(1,1), NOT NULL) | Order Identifier. **FR4**: Unique order ID per customer. Primary key. |
| UserID | INT (FK → USER.UserID, NOT NULL) | Customer ID. **UC-02.1**: Customer who places the order. |
| EventID | INT (FK → EVENT.EventID, NOT NULL) | Event ID. Links order to specific event. |
| OrderDate | DATETIME2 (NOT NULL, DEFAULT GETDATE()) | Order Date. Timestamp when order was created. **UC-02.1**: Order creation. |
| TotalAmount | DECIMAL(18,2) (NOT NULL) | Total Order Amount. **NF-03**: Precision requirement. Sum of all ticket prices in order. |
| TotalQuantity | INT (NOT NULL) | Total Ticket Quantity. Number of tickets in this order. |
| BookingStatus | NVARCHAR(50) (NOT NULL, DEFAULT 'Pending Payment') | Order Status. Values: 'PENDING_PAYMENT', 'CONFIRMED', 'EXPIRED', 'CANCELLED', 'COMPLETED'. **UC-02.1**: Order status flow. |
| ReservationExpiryTime | DATETIME2 (NULL) | Reservation Expiration Time. **FR16**: 15-minute reservation timeout from payment screen. Timer runs continuously in background. **UC-02.1**: Auto-cancel after timeout. |
| RecipientName | NVARCHAR(255) (NULL) | Recipient Name. **FR25**: Recipient information for tickets. |
| RecipientPhone | NVARCHAR(20) (NULL) | Recipient Phone. **FR25**: Mandatory phone number during booking. **FR22**: Phone number format validation. |
| RecipientEmail | NVARCHAR(255) (NULL) | Recipient Email. **FR25**: Recipient email address. |
| RecipientAddress | NVARCHAR(500) (NULL) | Recipient Address. **FR25**: Physical address for ticket delivery if needed. |
| RecipientNotes | NVARCHAR(1000) (NULL) | Recipient Notes. Additional notes or instructions. |
| CompletedAt | DATETIME2 (NULL) | Completion Timestamp. Records when order was confirmed (after payment). |

**Relationships:**
- Many-to-One with USER (UserID → USER.UserID)
- Many-to-One with EVENT (EventID → EVENT.EventID)
- One-to-Many with TICKET (OrderID → TICKET.OrderID)
- One-to-One with PAYMENT (OrderID → PAYMENT.OrderID)
- One-to-Many with SUPPORT_REQUEST (OrderID → SUPPORT_REQUEST.OrderID)

**Business Rules:**
- **FR4**: Unique order ID per customer
- **FR5**: Customer can only have ONE active pending order across ALL events
- **FR14**: Maximum tickets per order set by organizer
- **FR16**: 15-minute reservation timeout from payment screen
- **FR25**: Phone number and recipient info required during booking
- **FR22**: Phone number format validation
- **UC-02.1**: Order status flow: "Pending Payment" → "Confirmed" → "Used"/"Refunded"
- **FR20**: Order creation, payment, and cancellation are logged

---

## Table: ORGANIZER_PROFILE

| Column | DDL Specification | Role & UC/BR/NFR Reference |
|--------|-------------------|---------------------------|
| OrganizerID | INT (PK, FK → USER.UserID, NOT NULL) | Organizer Identifier. **UC-03.1 AF 3.1.1**: 1:1 Link to USER (Role = Organizer). Primary key and foreign key to USER. |
| OrganizationName | NVARCHAR(255) (NULL) | Organization Name. **UC-03.1 AF 3.1.1**: KYC data - organization name. |
| BusinessRegistrationNumber | NVARCHAR(100) (NULL) | Business Registration Number. **UC-03.1 AF 3.1.1**: KYC data - business registration document. |
| TaxID | NVARCHAR(50) (UNIQUE, NULL) | Tax Identifier. **UC-03.1 AF 3.1.1**: Tax ID (Unique, NULL if not provided). |
| BusinessAddress | NVARCHAR(500) (NULL) | Business Address. **UC-03.1 AF 3.1.1**: KYC data - business address. |
| ContactPerson | NVARCHAR(255) (NULL) | Contact Person. **UC-03.1 AF 3.1.1**: KYC data - contact person name. |
| BusinessPhone | NVARCHAR(20) (NULL) | Business Phone. **UC-03.1 AF 3.1.1**: KYC data - business phone number. |
| BusinessEmail | NVARCHAR(255) (NULL) | Business Email. **UC-03.1 AF 3.1.1**: KYC data - business email. |
| BankName | NVARCHAR(100) (NOT NULL) | Bank Name. **UC-03.4**: Revenue withdrawal requirement. Bank account information for revenue withdrawal. |
| BankAccountNumber | NVARCHAR(50) (NOT NULL) | Bank Account Number. **UC-03.4**: Required for revenue withdrawal. |
| BankAccountHolderName | NVARCHAR(255) (NOT NULL) | Bank Account Holder Name. **UC-03.4**: Account holder name for bank account. |
| BankBranch | NVARCHAR(255) (NULL) | Bank Branch. Optional bank branch information. |
| IDDocumentPath | NVARCHAR(500) (NULL) | ID Document Path. **UC-03.1 AF 3.1.1**: KYC document - ID document file path (<10MB, PDF/JPG/PNG only). |
| BusinessRegistrationDocumentPath | NVARCHAR(500) (NULL) | Business Registration Document Path. **UC-03.1 AF 3.1.1**: KYC document - business registration file path. |
| TaxDocumentPath | NVARCHAR(500) (NULL) | Tax Document Path. **UC-03.1 AF 3.1.1**: KYC document - tax document file path. |
| AddressProofDocumentPath | NVARCHAR(500) (NULL) | Address Proof Document Path. **UC-03.1 AF 3.1.1**: KYC document - address proof file path. |
| KYCStatus | NVARCHAR(50) (NOT NULL, DEFAULT 'Pending Verification') | KYC Verification Status. **UC-03.1 AF 3.1.1**: Values: 'PENDING_VERIFICATION', 'APPROVED', 'REJECTED'. **FR26**: Only upgrade to "Verified Organizer" role after Admin approval. |
| KYCSubmittedAt | DATETIME2 (NULL) | KYC Submission Date. Timestamp when KYC documents were submitted. |
| KYCApprovedAt | DATETIME2 (NULL) | KYC Approval Date. **UC-03.1 AF 3.1.1**: Timestamp when Admin approved KYC. |
| KYCApprovedBy | INT (FK → USER.UserID, NULL) | KYC Approver. **UC-03.1 AF 3.1.1**: Admin who approved KYC verification. |
| KYCRejectionReason | NVARCHAR(1000) (NULL) | KYC Rejection Reason. **UC-03.1 AF 3.1.1**: Reason provided if KYC was rejected. |
| PaymentConfigStatus | NVARCHAR(50) (NOT NULL, DEFAULT 'Pending') | Payment Configuration Status. Status of payment configuration setup. |
| CreatedAt | DATETIME2 (NOT NULL, DEFAULT GETDATE()) | Creation Date. Timestamp when profile was created. |

**Relationships:**
- One-to-One with USER (OrganizerID → USER.UserID)

**Business Rules:**
- **UC-03.1 AF 3.1.1**: KYC verification required before creating events
- **FR26**: Only upgrade to "Verified Organizer" role after Admin approval
- **FR11**: Customer role at registration, Verified Organizer after KYC approval
- **UC-03.4**: Bank account information required for revenue withdrawal
- **FR20**: KYC submission and approval are logged

---

## Table: PASSWORD_RESET_TOKEN

| Column | DDL Specification | Role & UC/BR/NFR Reference |
|--------|-------------------|---------------------------|
| TokenID | BIGINT (PK, IDENTITY(1,1), NOT NULL) | Token Identifier. Primary key for password reset tokens. |
| UserID | INT (FK → USER.UserID, NOT NULL) | User Identifier. Links token to user account. |
| TokenValue | VARCHAR(128) (UNIQUE, NOT NULL) | Unique Reset Token. **UC-01.3, UC-01.7**: Password reset token. Unique token for password reset. |
| ExpiryTime | DATETIME2 (NOT NULL) | Expiration Time. **UC-01.3**: Reset token expires after 15 minutes. |
| IsUsed | BIT (NOT NULL, DEFAULT 0) | Token Usage Flag. Indicates if token has been used. Single-use only. |
| CreatedAt | DATETIME2 (NOT NULL, DEFAULT GETDATE()) | Creation Timestamp. Records when token was generated. |
| UsedAt | DATETIME2 (NULL) | Usage Timestamp. Records when token was used. |

**Relationships:**
- Many-to-One with USER (UserID → USER.UserID)

**Business Rules:**
- **UC-01.3**: Reset token expires after 15 minutes
- **UC-01.7**: Change password requires current password verification
- Tokens are single-use only
- All active sessions are terminated after password change

---

## Table: PAYMENT

| Column | DDL Specification | Role & UC/BR/NFR Reference |
|--------|-------------------|---------------------------|
| PaymentID | INT (PK, IDENTITY(1,1), NOT NULL) | Payment Identifier. Primary key for payment transactions. |
| OrderID | INT (UNIQUE, FK → ORDER.OrderID, NOT NULL) | Order Link. **UC-02.1**: 1:1 Link to Order. Each order has exactly one payment. |
| VNPayTransactionID | VARCHAR(100) (UNIQUE, NULL) | VNPay Transaction ID. **UC-02.1**: VNPay Transaction ID (NULL if offline/pending). VNPAY payment gateway integration. |
| Amount | DECIMAL(18,2) (NOT NULL) | Payment Amount. **NF-03**: Precision requirement. Amount paid for the order. |
| PaymentDate | DATETIME2 (NOT NULL, DEFAULT GETDATE()) | Payment Date. **UC-02.1**: Timestamp when payment was processed. |
| PaymentStatus | NVARCHAR(50) (NOT NULL) | Payment Status. **UC-02.1**: Values: 'PENDING', 'PAID', 'FAILED', 'REFUNDED'. |
| PaymentMethod | NVARCHAR(50) (NULL, DEFAULT 'VNPAY') | Payment Method. Payment gateway used (VNPAY only). |

**Relationships:**
- One-to-One with ORDER (OrderID → ORDER.OrderID)

**Business Rules:**
- **UC-02.1**: VNPAY payment gateway integration only
- **UC-02.1**: Auto-generate unique QR code for each ticket immediately after payment success (FR6)
- **FR20**: Payment transactions are logged
- Payment signature verification for security

---

## Table: SESSION

| Column | DDL Specification | Role & UC/BR/NFR Reference |
|--------|-------------------|---------------------------|
| SessionID | VARCHAR(128) (PK, NOT NULL) | Session Identifier. **UC-01.1, UC-01.2**: Session token for authentication. Primary key. |
| UserID | INT (FK → USER.UserID, NOT NULL) | User Identifier. Links session to user account. |
| CreationTime | DATETIME2 (NOT NULL, DEFAULT GETDATE()) | Session Creation Time. Timestamp when session was created. **UC-01.2**: Login creates session. |
| ExpiryTime | DATETIME2 (NOT NULL) | Session Expiry Time. **UC-01.2**: Session token valid for 24 hours. |
| IPAddress | VARCHAR(45) (NULL) | IP Address. **FR20**: IP address for audit logging. IPv4 or IPv6 format. |
| UserAgent | NVARCHAR(500) (NULL) | User Agent. Browser/client information for audit logging. |
| LastActivity | DATETIME2 (NULL) | Last Activity Time. Tracks last activity to extend session. |
| IsActive | BIT (NOT NULL, DEFAULT 1) | Active Status. Indicates if session is currently active. |

**Relationships:**
- Many-to-One with USER (UserID → USER.UserID)

**Business Rules:**
- **UC-01.2**: Session token valid for 24 hours
- **UC-01.3**: All active sessions terminated after password change
- **FR20**: Session creation and invalidation are logged
- Sessions are validated on every protected endpoint

---

## Table: SUPPORT_REQUEST

| Column | DDL Specification | Role & UC/BR/NFR Reference |
|--------|-------------------|---------------------------|
| RequestID | INT (PK, IDENTITY(1,1), NOT NULL) | Request Identifier. Primary key for support requests. Unique support ticket ID. |
| UserID | INT (FK → USER.UserID, NOT NULL) | Request Creator. **UC-02.4**: Customer who submits the support request. |
| EventID | INT (FK → EVENT.EventID, NOT NULL) | Related Event. **UC-02.4**: Support requests routed to specific event's Organizer (not Admin). Links to event. |
| OrderID | INT (FK → ORDER.OrderID, NULL) | Related Order. **UC-02.4**: Optional link to specific order if support is related to an order. |
| Category | NVARCHAR(100) (NOT NULL) | Support Category. **UC-02.4, FR22**: Required category for support request. |
| Subject | NVARCHAR(255) (NOT NULL) | Subject Line. **UC-02.4, FR22**: Required subject for support request. |
| Description | NVARCHAR(MAX) (NOT NULL) | Description. **UC-02.4, FR22**: Required description. Optional file attachments (<10MB, JPG/PNG/PDF only). |
| Status | NVARCHAR(50) (NOT NULL, DEFAULT 'New') | Request Status. **UC-04.7**: Values: 'PENDING', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'. **UC-02.4**: Status flow. |
| RequestDate | DATETIME2 (NOT NULL, DEFAULT GETDATE()) | Request Date. Timestamp when support request was created. |
| AdminID | INT (FK → USER.UserID, NULL) | Processing Admin. **UC-02.4**: Organizer who processes the request (NULL if pending). Note: Support requests go to event's Organizer, not Admin. |
| OrganizerResponse | NVARCHAR(MAX) (NULL) | Organizer Response. **UC-02.4**: Response from organizer. |
| ResolutionDetails | NVARCHAR(MAX) (NULL) | Resolution Details. **UC-02.4**: Details of how issue was resolved. |
| RespondedAt | DATETIME2 (NULL) | Response Timestamp. Records when organizer responded. |
| ResolvedAt | DATETIME2 (NULL) | Resolution Timestamp. **UC-02.4**: Records when ticket was resolved. Auto-closes after 7 days if customer doesn't confirm resolution. |

**Relationships:**
- Many-to-One with USER (UserID → USER.UserID)
- Many-to-One with EVENT (EventID → EVENT.EventID)
- Many-to-One with ORDER (OrderID → ORDER.OrderID)
- Many-to-One with USER (AdminID → USER.UserID) - Note: Actually refers to Organizer

**Business Rules:**
- **UC-02.4**: Support requests routed to event's Organizer (not Admin)
- **FR22**: Required fields: category, subject, description
- **UC-02.4**: Auto-closes after 7 days if customer doesn't confirm resolution
- **FR20**: Support request creation and responses are logged

---

## Table: TICKET_TYPE

| Column | DDL Specification | Role & UC/BR/NFR Reference |
|--------|-------------------|---------------------------|
| TicketTypeID | INT (PK, IDENTITY(1,1), NOT NULL) | Ticket Type Identifier. Primary key for ticket types. |
| EventID | INT (FK → EVENT.EventID, NOT NULL) | Event Link. Links ticket type to specific event. |
| TypeName | NVARCHAR(100) (NOT NULL) | Ticket Type Name. Display name of ticket type (e.g., 'VIP', 'Standard', 'Early Bird'). |
| Price | DECIMAL(18,2) (NOT NULL) | Ticket Price. **FR3**: Ticket price. Cannot be changed after sales started. **NF-03**: Precision requirement. |
| TotalQuantity | INT (NOT NULL) | Initial Total Quantity. Total number of tickets available for this type. |
| AvailableQuantity | INT (NOT NULL, CHECK >= 0) | Remaining Inventory. **UC-02.1**: Available tickets for purchase. Must be >= 0. Decremented on reservation, incremented on release. |
| Description | NVARCHAR(1000) (NULL) | Ticket Type Description. Optional description of ticket type features. |
| SaleStartDate | DATETIME2 (NULL) | Sale Start Date. **UC-03.1**: When ticket sales begin for this type. |
| SaleEndDate | DATETIME2 (NULL) | Sale End Date. **UC-03.1**: When ticket sales end for this type. |

**Relationships:**
- Many-to-One with EVENT (EventID → EVENT.EventID)
- One-to-Many with TICKET (TicketTypeID → TICKET.TicketTypeID)

**Business Rules:**
- **FR3**: No price changes after sales started
- **UC-03.1**: At least 1 ticket type with price > 0 required for event
- **FR14**: Maximum tickets per order set by organizer (via Event.MaxTicketQuantity)
- AvailableQuantity cannot be negative
- Tickets are reserved for 15 minutes (FR16) before being released

---

## Table: TICKET

| Column | DDL Specification | Role & UC/BR/NFR Reference |
|--------|-------------------|---------------------------|
| TicketID | BIGINT (PK, IDENTITY(1,1), NOT NULL) | Individual Ticket ID. Primary key for individual tickets. |
| OrderID | INT (FK → ORDER.OrderID, NOT NULL) | Order Link. Links ticket to order. |
| TicketTypeID | INT (FK → TICKET_TYPE.TicketTypeID, NOT NULL) | Ticket Type Link. Links ticket to ticket type. |
| QRCode | VARCHAR(128) (UNIQUE, NOT NULL) | Unique QR Code. **FR6**: Unique QR code per ticket. **UC-02.3**: Essential for fast Check-in. Auto-generated immediately after payment success. |
| Status | NVARCHAR(50) (NOT NULL, DEFAULT 'Confirmed') | Ticket Status. **FR8**: Values: 'CONFIRMED', 'USED', 'REFUNDED', 'CANCELLED'. **UC-02.3**: Ticket status validation. |
| SeatNumber | NVARCHAR(50) (NULL) | Seat Details. **UC-02.1**: Seat details for assigned seating events (Nullable). |
| CheckInStatus | BIT (NOT NULL, DEFAULT 0) | Check-in Flag. **UC-02.3**: Indicates if ticket has been checked in. |
| CheckInTimestamp | DATETIME2 (NULL) | Check-in Timestamp. **UC-02.3**: Timestamp when ticket was checked in (NULL if not checked-in). |
| CreatedAt | DATETIME2 (NOT NULL, DEFAULT GETDATE()) | Creation Timestamp. Records when ticket was created (after payment). |

**Relationships:**
- Many-to-One with ORDER (OrderID → ORDER.OrderID)
- Many-to-One with TICKET_TYPE (TicketTypeID → TICKET_TYPE.TicketTypeID)
- One-to-One with REFUND (TicketID → REFUND.TicketID)

**Business Rules:**
- **FR6**: Unique QR code per ticket, auto-generated after payment, single-use only
- **FR8**: Refunded tickets invalidated for check-in
- **UC-02.3**: QR code can only be used ONCE
- **UC-02.3**: Verify ticket status is "Confirmed" (not Pending/Cancelled/Refunded)
- **UC-02.3**: Verify ticket not already used (no prior check-in timestamp)
- **UC-02.3**: Verify current date/time is within event period
- **FR20**: All check-in attempts including failures are logged

---

## Table: REFUND

| Column | DDL Specification | Role & UC/BR/NFR Reference |
|--------|-------------------|---------------------------|
| RefundID | INT (PK, IDENTITY(1,1), NOT NULL) | Refund Identifier. Primary key for refund requests. |
| TicketID | BIGINT (UNIQUE, FK → TICKET.TicketID, NOT NULL) | Ticket Link. **UC-02.5**: 1:1 Link to Ticket. Each ticket can have at most one refund request. |
| UserID | INT (FK → USER.UserID, NOT NULL) | Requesting User. Customer who requested the refund. |
| AdminID | INT (FK → USER.UserID, NOT NULL) | Approval Admin. **UC-02.5, FR7**: Admin who approved/rejected the refund. All refunds require Admin approval. |
| RefundAmount | DECIMAL(18,2) (NOT NULL) | Refund Amount. **UC-02.5**: Amount to be refunded (full or partial). **NF-03**: Precision requirement. |
| Reason | NVARCHAR(MAX) (NOT NULL) | Refund Reason. **UC-02.5, FR22**: Required refund reason from customer. |
| RefundDate | DATETIMEOFFSET (NOT NULL, DEFAULT GETUTCDATE()) | Refund Execution Time. **UC-02.5**: Timestamp when refund was executed (UTC). |
| VNPayRefundID | NVARCHAR(50) (NULL) | VNPay Refund Transaction ID. **UC-02.5**: VNPay refund transaction ID after processing. |
| RefundStatus | NVARCHAR(20) (NOT NULL) | Refund Status. **UC-02.5**: Values: 'PENDING_ADMIN_REVIEW', 'APPROVED_PROCESSING', 'COMPLETED', 'REJECTED'. Status flow. |
| RequestDate | DATETIME2 (NOT NULL, DEFAULT GETDATE()) | Request Date. Timestamp when refund was requested. |
| ProcessedDate | DATETIME2 (NULL) | Processing Date. Timestamp when refund was processed (completed or rejected). |
| AdminNotes | NVARCHAR(MAX) (NULL) | Admin Notes. Notes from Admin regarding approval/rejection. |

**Relationships:**
- One-to-One with TICKET (TicketID → TICKET.TicketID)
- Many-to-One with USER (UserID → USER.UserID) - Requesting customer
- Many-to-One with USER (AdminID → USER.UserID) - Approving admin

**Business Rules:**
- **FR7**: Refunds require organizer policy + Admin approval
- **UC-02.5**: Only allow refunds if event organizer's policy permits
- **UC-02.5**: Ticket must not be used for check-in
- **UC-02.5**: Refund within allowed timeframe
- **UC-02.5**: Event must not have occurred yet
- **FR8**: Refunded tickets invalidated for check-in
- **UC-02.5**: Only one pending refund request per ticket
- **FR22**: Refund reason required
- **FR20**: Refund request, Admin decision, and VNPAY transaction are logged

---

## Table: WITHDRAWAL_REQUEST

| Column | DDL Specification | Role & UC/BR/NFR Reference |
|--------|-------------------|---------------------------|
| RequestID | INT (PK, IDENTITY(1,1), NOT NULL) | Request Identifier. Primary key for withdrawal requests. |
| OrganizerID | INT (FK → USER.UserID, NOT NULL) | Organizer Identifier. **UC-03.4**: Organizer requesting withdrawal. **FR17, FR26**: Only Verified Organizer can request withdrawal. |
| EventID | INT (FK → EVENT.EventID, NULL) | Related Event. Optional - can be for specific event or all events. |
| Amount | DECIMAL(18,2) (NOT NULL) | Withdrawal Amount. **UC-03.4**: Amount to withdraw. **NF-03**: Precision requirement. |
| AvailableBalance | DECIMAL(18,2) (NOT NULL) | Available Balance. **UC-03.4**: Balance at time of request (total - platform fees). |
| PlatformFee | DECIMAL(18,2) (NULL) | Platform Fee. Platform fee deducted from revenue. |
| BankName | NVARCHAR(255) (NOT NULL) | Bank Name. **UC-03.4**: Bank account information for withdrawal. |
| BankAccountNumber | NVARCHAR(100) (NOT NULL) | Bank Account Number. **UC-03.4**: Bank account number. |
| BankAccountHolder | NVARCHAR(255) (NOT NULL) | Bank Account Holder. **UC-03.4**: Account holder name. |
| BankBranch | NVARCHAR(255) (NULL) | Bank Branch. Optional bank branch information. |
| Status | NVARCHAR(50) (NOT NULL, DEFAULT 'Pending Review') | Request Status. **UC-03.4**: Values: 'PENDING_REVIEW', 'APPROVED', 'PROCESSING', 'COMPLETED', 'REJECTED'. |
| RequestedAt | DATETIME2 (NOT NULL, DEFAULT GETDATE()) | Request Date. Timestamp when withdrawal was requested. |
| ReviewedAt | DATETIME2 (NULL) | Review Date. **UC-03.4**: Timestamp when Admin reviewed the request. |
| ReviewedBy | INT (FK → USER.UserID, NULL) | Reviewing Admin. **UC-03.4**: Admin who reviewed the request. |
| ProcessedAt | DATETIME2 (NULL) | Processing Date. Timestamp when withdrawal was processed. |
| AdminNotes | NVARCHAR(MAX) (NULL) | Admin Notes. Notes from Admin regarding approval/rejection. |
| TransactionReference | NVARCHAR(255) (NULL) | Transaction Reference. Bank transfer transaction reference after processing. |

**Relationships:**
- Many-to-One with USER (OrganizerID → USER.UserID)
- Many-to-One with EVENT (EventID → EVENT.EventID)
- Many-to-One with USER (ReviewedBy → USER.UserID)

**Business Rules:**
- **FR17, FR26**: Only Verified Organizer can request withdrawal
- **UC-03.4**: Block withdrawal if event not completed or pending refunds exist
- **UC-03.4**: Require verified bank account on file
- **UC-03.4**: Enforce minimum withdrawal amount (e.g., $50)
- **UC-03.4**: All withdrawals need Admin approval
- **UC-03.4**: Only one pending withdrawal request at a time
- **UC-03.4**: Allow partial withdrawal
- **FR20**: Withdrawal requests and Admin approvals are logged

---

## Table: AUDIT_LOG

| Column | DDL Specification | Role & UC/BR/NFR Reference |
|--------|-------------------|---------------------------|
| LogID | BIGINT (PK, IDENTITY(1,1), NOT NULL) | Log Identifier. Primary key for audit logs. |
| UserID | INT (NULL) | User ID. **FR20**: User who performed the action (who). |
| Username | NVARCHAR(255) (NULL) | Username. User's username for audit trail. |
| ActionType | NVARCHAR(100) (NOT NULL) | Action Type. **FR20**: What action was performed (what). Values: 'LOGIN', 'LOGOUT', 'CREATE_EVENT', 'UPDATE_USER', 'APPROVE_REFUND', 'CHECK_IN', etc. |
| EntityType | NVARCHAR(100) (NULL) | Entity Type. **FR20**: Which entity was affected. Values: 'USER', 'EVENT', 'ORDER', 'REFUND', 'TICKET', etc. |
| EntityID | BIGINT (NULL) | Entity ID. **FR20**: ID of affected record (which data). |
| Description | NVARCHAR(MAX) (NULL) | Description. Detailed description of the action. |
| IPAddress | VARCHAR(50) (NULL) | IP Address. **FR20**: IP address where action originated (where from). |
| UserAgent | NVARCHAR(500) (NULL) | User Agent. Browser/client information. |
| Timestamp | DATETIME2 (NOT NULL, DEFAULT GETDATE()) | Timestamp. **FR20**: When action occurred (when). |
| Result | NVARCHAR(50) (NULL) | Result. **FR20**: Action result (success/failure). Values: 'SUCCESS', 'FAILURE', 'ERROR'. |
| ErrorMessage | NVARCHAR(MAX) (NULL) | Error Message. Technical error message if action failed. |
| OldValue | NVARCHAR(MAX) (NULL) | Old Value. **FR20**: Previous value for update operations. |
| NewValue | NVARCHAR(MAX) (NULL) | New Value. **FR20**: New value for update operations. |

**Indexes:**
- idx_user_id (UserID)
- idx_action_type (ActionType)
- idx_timestamp (Timestamp)

**Business Rules:**
- **FR20**: Log all critical actions with:
  - User ID (who performed action)
  - Timestamp (when)
  - Action type (what)
  - Affected records (which data)
  - IP address (where from)
  - Result (success/failure)
- **FR20**: Log payments, refunds, event creation/updates, account changes, check-ins, Admin actions
- **FR20**: Preserve logs indefinitely for compliance
- Logs are read-only (no updates/deletes)

---

## Summary of Key Relationships

### Foreign Key Relationships

1. **USER** → **ORGANIZER_PROFILE**: One-to-One (OrganizerID)
2. **USER** → **ORDER**: One-to-Many (UserID)
3. **USER** → **EVENT**: One-to-Many (OrganizerID)
4. **USER** → **SESSION**: One-to-Many (UserID)
5. **USER** → **REFUND**: One-to-Many (UserID, AdminID)
6. **USER** → **SUPPORT_REQUEST**: One-to-Many (UserID, AdminID)
7. **USER** → **EVENT_UPDATE_REQUEST**: One-to-Many (SubmittedByUserID, AdminID)
8. **USER** → **PASSWORD_RESET_TOKEN**: One-to-Many (UserID)
9. **USER** → **WITHDRAWAL_REQUEST**: One-to-Many (OrganizerID, ReviewedBy)
10. **USER** → **AUDIT_LOG**: One-to-Many (UserID)

11. **EVENT** → **CATEGORY**: Many-to-One (CategoryID)
12. **EVENT** → **TICKET_TYPE**: One-to-Many (EventID)
13. **EVENT** → **ORDER**: One-to-Many (EventID)
14. **EVENT** → **EVENT_UPDATE_REQUEST**: One-to-Many (EventID)
15. **EVENT** → **SUPPORT_REQUEST**: One-to-Many (EventID)
16. **EVENT** → **WITHDRAWAL_REQUEST**: One-to-Many (EventID)

17. **ORDER** → **USER**: Many-to-One (UserID)
18. **ORDER** → **EVENT**: Many-to-One (EventID)
19. **ORDER** → **TICKET**: One-to-Many (OrderID)
20. **ORDER** → **PAYMENT**: One-to-One (OrderID)
21. **ORDER** → **SUPPORT_REQUEST**: One-to-Many (OrderID)

22. **TICKET** → **ORDER**: Many-to-One (OrderID)
23. **TICKET** → **TICKET_TYPE**: Many-to-One (TicketTypeID)
24. **TICKET** → **REFUND**: One-to-One (TicketID)

25. **TICKET_TYPE** → **EVENT**: Many-to-One (EventID)

26. **CATEGORY** → **EVENT**: One-to-Many (CategoryID)

---

## Constraints and Validation Rules

### Unique Constraints
- USER.Contact (Email/Phone must be unique)
- USER.OAuthProviderId (if OAuth provider is used)
- ORGANIZER_PROFILE.TaxID (if provided)
- TICKET.QRCode (must be unique)
- PAYMENT.OrderID (one payment per order)
- PAYMENT.VNPayTransactionID (if provided)
- REFUND.TicketID (one refund per ticket)
- PASSWORD_RESET_TOKEN.TokenValue (unique token)
- SESSION.SessionID (unique session token)
- VERIFICATION_CODE (CodeValue + Contact + Purpose combination should be unique within expiry window)

### Check Constraints
- TICKET_TYPE.AvailableQuantity >= 0
- USER.FailedLoginAttempts >= 0
- EVENT.StartDate < EVENT.EndDate
- ORDER.TotalAmount > 0
- PAYMENT.Amount > 0
- REFUND.RefundAmount > 0
- WITHDRAWAL_REQUEST.Amount > 0

### Default Values
- USER.Role = 'Customer'
- USER.AccountStatus = 'Active'
- USER.FailedLoginAttempts = 0
- EVENT.Status = 'Draft'
- ORDER.BookingStatus = 'Pending Payment'
- TICKET.Status = 'Confirmed'
- TICKET.CheckInStatus = 0
- ORGANIZER_PROFILE.KYCStatus = 'Pending Verification'
- ORGANIZER_PROFILE.PaymentConfigStatus = 'Pending'
- SUPPORT_REQUEST.Status = 'New'
- EVENT_UPDATE_REQUEST.RequestStatus = 'Pending'
- WITHDRAWAL_REQUEST.Status = 'Pending Review'

---

## Indexes (Performance Optimization)

### Primary Indexes
- All tables have clustered index on Primary Key

### Foreign Key Indexes
- USER.UserID (referenced by multiple tables)
- EVENT.EventID (referenced by multiple tables)
- ORDER.OrderID (referenced by multiple tables)
- TICKET.TicketID (referenced by REFUND)

### Business Logic Indexes
- USER.Contact (UNIQUE index for login)
- TICKET.QRCode (UNIQUE index for fast check-in lookup)
- SESSION.SessionID (UNIQUE index for session validation)
- PASSWORD_RESET_TOKEN.TokenValue (UNIQUE index)
- AUDIT_LOG.UserID (for audit queries)
- AUDIT_LOG.ActionType (for audit queries)
- AUDIT_LOG.Timestamp (for time-based audit queries)
- ORDER.ReservationExpiryTime (for expired order cleanup)
- SESSION.ExpiryTime (for expired session cleanup)

---

## Data Types Summary

- **INT**: User IDs, Event IDs, Order IDs, Request IDs
- **BIGINT**: Ticket IDs, Log IDs, Token IDs, Code IDs
- **DECIMAL(18,2)**: Monetary amounts (TotalAmount, Price, RefundAmount, etc.)
- **NVARCHAR/VARCHAR**: Text fields with appropriate lengths
- **NVARCHAR(MAX)**: Large text fields (descriptions, notes, JSON)
- **DATETIME2**: Timestamps (local time)
- **DATETIMEOFFSET**: UTC timestamps with timezone
- **BIT**: Boolean flags (IsDeleted, IsUsed, CheckInStatus, etc.)

---

## Security Considerations

1. **Password Hashing**: Passwords stored as MD5 hash (FR1) - Note: Consider upgrading to BCrypt for production
2. **Session Management**: Sessions expire after 24 hours, validated on every request
3. **Account Lockout**: Accounts locked after 5 failed login attempts (FR12)
4. **Soft Delete**: User accounts use soft delete (FR19) for audit trail
5. **Audit Logging**: All critical actions logged (FR20)
6. **Unique Constraints**: Prevent duplicate emails/phones, QR codes, tokens
7. **FK Constraints**: Ensure referential integrity
8. **Check Constraints**: Enforce business rules at database level

---

## Non-Functional Requirements (NFR)

- **NF-01**: Fast Check-in performance (QR code indexing)
- **NF-03**: Monetary precision (DECIMAL(18,2))
- **NF-04**: Security (password hashing, session management)
- **NF-05**: Audit trail (comprehensive logging)
- **NF-06**: Data integrity (FK constraints, unique constraints, check constraints)

---

## Migration Notes

When implementing this schema:

1. Create tables in dependency order:
   - CATEGORY, USER (base tables)
   - ORGANIZER_PROFILE, SESSION, PASSWORD_RESET_TOKEN, VERIFICATION_CODE (user-related)
   - EVENT, TICKET_TYPE (event-related)
   - ORDER, PAYMENT, TICKET (order-related)
   - REFUND, SUPPORT_REQUEST, EVENT_UPDATE_REQUEST, WITHDRAWAL_REQUEST (request-related)
   - AUDIT_LOG (logging)

2. Add foreign key constraints after all tables are created
3. Add indexes after tables and constraints are created
4. Add check constraints for data validation
5. Set up default values and triggers if needed

---

## End of Document

