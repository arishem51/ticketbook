# Integrated Communication Diagrams (ICD)
## Ticket Booking System

---

## 1. High-Level System Architecture

```mermaid
graph TB
    subgraph "External Systems"
        FE[Frontend Application<br/>React/Vue/Angular]
        DB[(PostgreSQL<br/>Database)]
        VNPAY[VNPay Payment<br/>Gateway]
        EMAIL[Email Service<br/>SMTP/SendGrid]
        SMS[SMS Service<br/>Twilio/AWS SNS]
    end

    subgraph "Spring Boot Application"
        API[REST API<br/>Controllers]
        SEC[Security Layer<br/>Session Auth]
        SVC[Business Services]
        REPO[Repositories<br/>JPA]
    end

    FE -->|HTTP/REST| API
    API --> SEC
    SEC --> SVC
    SVC --> REPO
    REPO <-->|JDBC| DB
    SVC -->|Payment API| VNPAY
    SVC -->|Email API| EMAIL
    SVC -->|SMS API| SMS
    VNPAY -->|Callback| API
```

---

## 2. Authentication Flow Communication

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AuthController
    participant AuthService
    participant EmailService
    participant SMSService
    participant VerificationService
    participant SessionService
    participant UserRepository
    participant DB

    User->>Frontend: Register/Login Request
    Frontend->>AuthController: POST /api/auth/register
    AuthController->>AuthService: register(request)
    
    AuthService->>VerificationService: generateCode(email/phone)
    VerificationService-->>AuthService: verificationCode
    
    alt Email Registration
        AuthService->>EmailService: sendVerificationCode(email, code)
        EmailService-->>AuthService: Email sent
    else Phone Registration
        AuthService->>SMSService: sendVerificationCode(phone, code)
        SMSService-->>AuthService: SMS sent
    end
    
    AuthService->>UserRepository: save(user)
    UserRepository->>DB: INSERT INTO users
    DB-->>UserRepository: User saved
    UserRepository-->>AuthService: User entity
    
    AuthService->>SessionService: createSession(user)
    SessionService-->>AuthService: Session token
    
    AuthService-->>AuthController: AuthResponse(token)
    AuthController-->>Frontend: 201 Created + AuthResponse
    Frontend-->>User: Login successful
```

---

## 3. Order & Payment Flow Communication

```mermaid
sequenceDiagram
    participant Customer
    participant Frontend
    participant OrderController
    participant OrderService
    participant EventService
    participant TicketRepository
    participant VNPayService
    participant VNPay
    participant EmailService
    participant QRCodeService
    participant DB

    Customer->>Frontend: Select tickets & create order
    Frontend->>OrderController: POST /api/orders
    OrderController->>OrderService: createOrder(userId, request)
    
    OrderService->>EventService: validateEventAvailability(eventId)
    EventService-->>OrderService: Event available
    
    OrderService->>TicketRepository: reserveTickets(ticketTypes)
    TicketRepository->>DB: UPDATE tickets SET status='RESERVED'
    DB-->>TicketRepository: Tickets reserved
    TicketRepository-->>OrderService: Reserved tickets
    
    OrderService->>DB: INSERT INTO orders
    DB-->>OrderService: Order created
    OrderService-->>OrderController: OrderResponse
    OrderController-->>Frontend: Order created (15 min expiry)
    
    Customer->>Frontend: Initiate payment
    Frontend->>OrderController: POST /api/orders/payment
    OrderController->>OrderService: initiatePayment(userId, orderId)
    
    OrderService->>VNPayService: createPaymentUrl(orderId, amount)
    VNPayService-->>OrderService: Payment URL
    OrderService-->>OrderController: Payment URL
    OrderController-->>Frontend: Payment URL
    Frontend->>VNPay: Redirect to payment page
    
    Customer->>VNPay: Complete payment
    VNPay->>OrderController: POST /api/orders/{orderId}/confirm?transactionId=xxx
    OrderController->>OrderService: confirmPayment(orderId, transactionId)
    
    OrderService->>VNPayService: validatePaymentResponse(params)
    VNPayService->>VNPay: Verify transaction
    VNPay-->>VNPayService: Transaction valid
    VNPayService-->>OrderService: Payment confirmed
    
    OrderService->>DB: UPDATE orders SET status='CONFIRMED'
    OrderService->>DB: UPDATE tickets SET status='BOOKED'
    OrderService->>QRCodeService: generateQRCode(ticket)
    QRCodeService-->>OrderService: QR Code image
    
    OrderService->>EmailService: sendETickets(order, tickets)
    EmailService-->>OrderService: Email sent
    
    OrderService-->>OrderController: OrderResponse with tickets
    OrderController-->>Frontend: Payment confirmed
    Frontend-->>Customer: E-tickets received
```

---

## 4. Event Management Communication (Organizer)

```mermaid
sequenceDiagram
    participant Organizer
    participant Frontend
    participant OrganizerController
    participant EventService
    participant EventRepository
    participant OrganizerService
    participant AdminService
    participant EmailService
    participant DB

    Organizer->>Frontend: Create new event
    Frontend->>OrganizerController: POST /api/organizer/events
    OrganizerController->>EventService: createEvent(organizerId, request)
    
    EventService->>OrganizerService: validateOrganizerStatus(organizerId)
    OrganizerService-->>EventService: Organizer verified
    
    EventService->>EventRepository: save(event)
    EventRepository->>DB: INSERT INTO events
    DB-->>EventRepository: Event saved
    
    alt Submit for Approval
        EventService->>DB: UPDATE events SET status='PENDING_APPROVAL'
        EventService->>AdminService: notifyAdminForReview(eventId)
        AdminService->>EmailService: notifyAdmin(event)
        EmailService-->>AdminService: Notification sent
    else Save as Draft
        EventService->>DB: UPDATE events SET status='DRAFT'
    end
    
    EventRepository-->>EventService: Event entity
    EventService-->>OrganizerController: EventResponse
    OrganizerController-->>Frontend: Event created
    Frontend-->>Organizer: Event saved/submitted
    
    Note over AdminService: Admin reviews and approves/rejects
    AdminService->>EventRepository: updateEventStatus(eventId, 'APPROVED')
    EventRepository->>DB: UPDATE events
    AdminService->>EmailService: notifyOrganizer(event, status)
    EmailService-->>Organizer: Approval notification
```

---

## 5. Check-In Process Communication

```mermaid
sequenceDiagram
    participant Staff
    participant Frontend
    participant CheckInController
    participant CheckInService
    participant TicketRepository
    participant QRCodeService
    participant OrderRepository
    participant AuditLogService
    participant DB

    Staff->>Frontend: Scan QR code
    Frontend->>CheckInController: POST /api/checkin/scan
    CheckInController->>CheckInService: scanTicket(qrCodeData)
    
    CheckInService->>QRCodeService: decodeQRCode(qrCodeData)
    QRCodeService-->>CheckInService: Ticket ID
    
    CheckInService->>TicketRepository: findByTicketId(ticketId)
    TicketRepository->>DB: SELECT * FROM tickets WHERE ticket_id=?
    DB-->>TicketRepository: Ticket data
    TicketRepository-->>CheckInService: Ticket entity
    
    CheckInService->>OrderRepository: findOrderByTicket(ticketId)
    OrderRepository->>DB: SELECT * FROM orders WHERE order_id=?
    DB-->>OrderRepository: Order data
    OrderRepository-->>CheckInService: Order entity
    
    alt Ticket Valid
        CheckInService->>DB: UPDATE tickets SET status='CHECKED_IN'
        CheckInService->>AuditLogService: logCheckIn(ticketId, staffId)
        AuditLogService->>DB: INSERT INTO audit_logs
        CheckInService-->>CheckInController: CheckInResponse(success)
    else Ticket Invalid
        CheckInService-->>CheckInController: CheckInResponse(error: "Already checked in")
    end
    
    CheckInController-->>Frontend: Check-in result
    Frontend-->>Staff: Display result
```

---

## 6. Refund Process Communication

```mermaid
sequenceDiagram
    participant Customer
    participant Frontend
    participant RefundController
    participant RefundService
    participant OrderService
    participant VNPayService
    participant VNPay
    participant TicketRepository
    participant EmailService
    participant DB

    Customer->>Frontend: Request refund
    Frontend->>RefundController: POST /api/refunds
    RefundController->>RefundService: requestRefund(userId, orderId, reason)
    
    RefundService->>OrderService: getOrderDetails(orderId)
    OrderService-->>RefundService: Order details
    
    RefundService->>RefundService: validateRefundEligibility(order)
    
    alt Refund Eligible
        RefundService->>DB: INSERT INTO refund_info
        RefundService->>VNPayService: processRefund(orderId, amount)
        VNPayService->>VNPay: Refund API call
        VNPay-->>VNPayService: Refund processed
        VNPayService-->>RefundService: Refund successful
        
        RefundService->>DB: UPDATE refund_info SET status='APPROVED'
        RefundService->>DB: UPDATE orders SET status='REFUNDED'
        RefundService->>TicketRepository: updateTicketStatus(tickets, 'REFUNDED')
        TicketRepository->>DB: UPDATE tickets SET status='REFUNDED'
        
        RefundService->>EmailService: sendRefundConfirmation(order)
        EmailService-->>RefundService: Email sent
        
        RefundService-->>RefundController: RefundResponse
        RefundController-->>Frontend: Refund processed
    else Refund Not Eligible
        RefundService-->>RefundController: Error: "Refund not eligible"
        RefundController-->>Frontend: Refund rejected
    end
    
    Frontend-->>Customer: Refund status
```

---

## 7. Support Ticket Communication Flow

```mermaid
sequenceDiagram
    participant Customer
    participant Frontend
    participant SupportController
    participant SupportService
    participant SupportTicketRepository
    participant EmailService
    participant AdminService
    participant DB

    Customer->>Frontend: Create support ticket
    Frontend->>SupportController: POST /api/support/tickets
    SupportController->>SupportService: createTicket(userId, request)
    
    SupportService->>SupportTicketRepository: save(ticket)
    SupportTicketRepository->>DB: INSERT INTO support_tickets
    DB-->>SupportTicketRepository: Ticket saved
    SupportTicketRepository-->>SupportService: Ticket entity
    
    SupportService->>EmailService: notifySupportTeam(ticket)
    EmailService-->>SupportService: Notification sent
    
    SupportService->>AdminService: assignTicketToAdmin(ticketId)
    AdminService-->>SupportService: Ticket assigned
    
    SupportService-->>SupportController: SupportTicketResponse
    SupportController-->>Frontend: Ticket created
    Frontend-->>Customer: Ticket ID received
    
    Note over AdminService: Admin responds to ticket
    AdminService->>SupportService: respondToTicket(ticketId, response)
    SupportService->>DB: UPDATE support_tickets SET status='RESOLVED'
    SupportService->>EmailService: notifyCustomer(ticket, response)
    EmailService-->>Customer: Response notification
```

---

## 8. Admin Management Communication

```mermaid
sequenceDiagram
    participant Admin
    participant Frontend
    participant AdminController
    participant AdminService
    participant EventService
    participant OrganizerService
    participant WithdrawalService
    participant SupportService
    participant StatisticsService
    participant EmailService
    participant DB

    Admin->>Frontend: Review pending requests
    Frontend->>AdminController: GET /api/admin/events/pending
    AdminController->>AdminService: getPendingEvents()
    AdminService->>DB: SELECT * FROM events WHERE status='PENDING_APPROVAL'
    DB-->>AdminService: Pending events
    AdminService-->>AdminController: List of events
    AdminController-->>Frontend: Events list
    
    Admin->>Frontend: Approve event
    Frontend->>AdminController: PUT /api/admin/events/{eventId}/approve
    AdminController->>AdminService: approveEvent(eventId)
    
    AdminService->>EventService: updateEventStatus(eventId, 'APPROVED')
    EventService->>DB: UPDATE events SET status='APPROVED'
    EventService->>EmailService: notifyOrganizer(event, 'APPROVED')
    EmailService-->>EventService: Notification sent
    
    AdminService->>OrganizerService: approveKyc(organizerId)
    OrganizerService->>DB: UPDATE organizer_profiles SET kyc_status='APPROVED'
    OrganizerService->>EmailService: notifyOrganizer(kycStatus)
    
    AdminService->>WithdrawalService: processWithdrawal(withdrawalId)
    WithdrawalService->>DB: UPDATE withdrawal_requests SET status='PROCESSED'
    WithdrawalService->>EmailService: notifyOrganizer(withdrawal)
    
    AdminService->>StatisticsService: getSystemStatistics()
    StatisticsService->>DB: Aggregate queries
    DB-->>StatisticsService: Statistics data
    StatisticsService-->>AdminService: System stats
    AdminService-->>AdminController: Statistics response
    AdminController-->>Frontend: Dashboard data
```

---

## 9. Component Interaction Overview

```mermaid
graph LR
    subgraph "Presentation Layer"
        AC[AuthController]
        OC[OrderController]
        EC[PublicEventController]
        ORG[OrganizerController]
        ADM[AdminController]
        CHK[CheckInController]
        REF[RefundController]
        SUP[SupportController]
    end

    subgraph "Business Logic Layer"
        AS[AuthService]
        OS[OrderService]
        ES[EventService]
        ORGS[OrganizerService]
        ADMS[AdminService]
        CHKS[CheckInService]
        REFS[RefundService]
        SUPS[SupportService]
        STS[StatisticsService]
        WDS[WithdrawalService]
    end

    subgraph "Integration Services"
        VNS[VNPayService]
        EMS[EmailService]
        SMSS[SMSService]
        QRS[QRCodeService]
        VS[VerificationService]
        SS[SessionService]
        ALS[AuditLogService]
    end

    subgraph "Data Access Layer"
        UR[UserRepository]
        OR[OrderRepository]
        ER[EventRepository]
        TR[TicketRepository]
        OPR[OrganizerProfileRepository]
        STR[SupportTicketRepository]
        RIR[RefundInfoRepository]
    end

    AC --> AS
    OC --> OS
    EC --> ES
    ORG --> ES
    ORG --> ORGS
    ORG --> STS
    ORG --> WDS
    ADM --> ADMS
    CHK --> CHKS
    REF --> REFS
    SUP --> SUPS

    AS --> SS
    AS --> VS
    AS --> EMS
    AS --> SMSS
    AS --> UR

    OS --> ES
    OS --> VNS
    OS --> EMS
    OS --> QRS
    OS --> OR
    OS --> TR

    ES --> ER
    ES --> EMS

    CHKS --> TR
    CHKS --> QRS
    CHKS --> ALS

    REFS --> OS
    REFS --> VNS
    REFS --> EMS
    REFS --> TR

    ORGS --> OPR
    ORGS --> EMS

    ADMS --> ES
    ADMS --> ORGS
    ADMS --> SUPS
    ADMS --> STS
    ADMS --> WDS
    ADMS --> EMS

    UR --> DB[(Database)]
    OR --> DB
    ER --> DB
    TR --> DB
    OPR --> DB
    STR --> DB
    RIR --> DB
```

---

## 10. External Service Integration

```mermaid
graph TB
    subgraph "Ticket Booking System"
        APP[Spring Boot Application]
    end

    subgraph "Payment Services"
        VNPAY[VNPay Gateway<br/>HTTPS API]
    end

    subgraph "Communication Services"
        EMAIL[Email Provider<br/>SMTP/SendGrid]
        SMS[SMS Provider<br/>Twilio/AWS SNS]
    end

    subgraph "Data Storage"
        DB[(PostgreSQL<br/>Database)]
    end

    subgraph "Client Applications"
        WEB[Web Frontend<br/>React/Vue/Angular]
        MOBILE[Mobile App<br/>iOS/Android]
    end

    WEB -->|REST API| APP
    MOBILE -->|REST API| APP
    
    APP -->|Payment Requests| VNPAY
    VNPAY -->|Callbacks| APP
    
    APP -->|Send Email| EMAIL
    APP -->|Send SMS| SMS
    
    APP <-->|JDBC/JPA| DB

    style APP fill:#4CAF50
    style VNPAY fill:#FF9800
    style EMAIL fill:#2196F3
    style SMS fill:#2196F3
    style DB fill:#9C27B0
    style WEB fill:#00BCD4
    style MOBILE fill:#00BCD4
```

---

## 11. Security & Session Management Flow

```mermaid
sequenceDiagram
    participant Client
    participant SecurityFilter
    participant SessionAuthFilter
    participant SessionService
    participant SessionRepository
    participant Controller
    participant CurrentUserResolver
    participant DB

    Client->>SecurityFilter: HTTP Request with Authorization header
    SecurityFilter->>SessionAuthFilter: Process request
    
    SessionAuthFilter->>SessionAuthFilter: Extract token from header
    SessionAuthFilter->>SessionService: validateSession(token)
    
    SessionService->>SessionRepository: findByToken(token)
    SessionRepository->>DB: SELECT * FROM sessions WHERE token=?
    DB-->>SessionRepository: Session data
    
    alt Session Valid
        SessionRepository-->>SessionService: Session entity
        SessionService->>SessionService: checkExpiry(session)
        
        alt Session Not Expired
            SessionService->>SessionRepository: updateLastAccess(session)
            SessionRepository->>DB: UPDATE sessions SET last_access=?
            SessionService-->>SessionAuthFilter: Valid session with user
            SessionAuthFilter->>SecurityContext: Set authentication
            SessionAuthFilter->>Controller: Forward request
            Controller->>CurrentUserResolver: Resolve @CurrentUser
            CurrentUserResolver-->>Controller: User entity
            Controller-->>Client: Response
        else Session Expired
            SessionService->>SessionRepository: delete(session)
            SessionRepository->>DB: DELETE FROM sessions
            SessionService-->>SessionAuthFilter: Session expired
            SessionAuthFilter-->>Client: 401 Unauthorized
        end
    else Session Invalid
        SessionRepository-->>SessionService: Session not found
        SessionService-->>SessionAuthFilter: Invalid session
        SessionAuthFilter-->>Client: 401 Unauthorized
    end
```

---

## Notes

### Communication Patterns:

1. **REST API**: All client communication uses REST over HTTP/HTTPS
2. **Session-based Authentication**: Custom session management with token-based approach
3. **Service Layer Pattern**: Controllers delegate to services, services handle business logic
4. **Repository Pattern**: Data access abstracted through JPA repositories
5. **External Service Integration**: Asynchronous communication with payment, email, and SMS services
6. **Database**: PostgreSQL with JPA/Hibernate for ORM

### Key Integrations:

- **VNPay**: Payment gateway for processing payments and refunds
- **Email Service**: SMTP/SendGrid for sending notifications and e-tickets
- **SMS Service**: Twilio/AWS SNS for verification codes and alerts
- **Database**: PostgreSQL for persistent data storage

### Security:

- Session tokens stored in database
- Session expiry (24 hours default)
- Role-based access control (CUSTOMER, VERIFIED_ORGANIZER, ADMIN)
- CORS configuration for frontend integration

