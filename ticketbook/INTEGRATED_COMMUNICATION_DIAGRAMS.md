# Integrated Communication Diagrams (ICD)
## Ticket Booking System

## Giải thích về các loại Diagram

### Communication Diagram (Diagram Giao Tiếp) là gì?

**Communication Diagram** (trong UML còn gọi là **Collaboration Diagram**) là một loại diagram mô tả:
- **Các đối tượng/component** trong hệ thống
- **Cách chúng tương tác và giao tiếp** với nhau
- **Luồng thông điệp** giữa các đối tượng
- **Mối quan hệ** giữa các thành phần

**Đặc điểm:**
- Tập trung vào **cấu trúc** và **mối quan hệ** giữa các đối tượng
- Hiển thị các **đường kết nối** (links) giữa các đối tượng
- Đánh số thứ tự các thông điệp để thể hiện **thứ tự thực thi**

**So sánh với Sequence Diagram:**
- **Sequence Diagram**: Tập trung vào **thứ tự thời gian**, các đối tượng được sắp xếp theo trục thời gian
- **Communication Diagram**: Tập trung vào **cấu trúc và mối quan hệ**, các đối tượng được sắp xếp tự do trong không gian

### Các loại Diagram trong file này:

File này bao gồm nhiều loại diagram để mô tả đầy đủ hệ thống:

1. **Graph/Flowchart Diagrams** (Diagram 1, 9, 10): Mô tả cấu trúc và luồng dữ liệu
2. **Sequence Diagrams** (Diagram 2-8, 11): Mô tả luồng tương tác theo thời gian
3. **Component Diagrams** (Diagram 9): Mô tả cấu trúc component và dependencies

**Lý do sử dụng "Communication" trong tên:**
- Tất cả các diagram đều tập trung vào **communication** (giao tiếp) giữa các thành phần
- Mô tả cách các module **tương tác và trao đổi thông tin** với nhau
- Phù hợp với mục đích của **Integrated Communication Diagrams (ICD)** - mô tả toàn bộ giao tiếp trong hệ thống

### Ví dụ Communication Diagram thuần túy (UML):

```mermaid
graph LR
    A[User] -->|1: register| B[AuthController]
    B -->|2: register| C[AuthService]
    C -->|3: generateCode| D[VerificationService]
    C -->|4: sendEmail| E[EmailService]
    C -->|5: save| F[UserRepository]
    F -->|6: save| G[(Database)]
    
    style A fill:#e1f5ff
    style B fill:#fff4e1
    style C fill:#fff4e1
    style D fill:#ffe1f5
    style E fill:#ffe1f5
    style F fill:#e1ffe1
    style G fill:#f5e1ff
```

**Giải thích:**
- Các số 1, 2, 3... thể hiện **thứ tự** các thông điệp được gửi
- Các mũi tên thể hiện **hướng giao tiếp** giữa các đối tượng
- Layout tự do, tập trung vào **mối quan hệ** giữa các component

**So sánh với Sequence Diagram:**
- Sequence Diagram: Sắp xếp theo trục thời gian (từ trên xuống)
- Communication Diagram: Sắp xếp tự do, tập trung vào cấu trúc

---

## III. Analysis Models - Interaction Diagrams

### III.1.1. Sequence Diagrams

Các Sequence Diagrams được mô tả ở các phần sau (Diagram 2-8, 11), tập trung vào **thứ tự thời gian** của các tương tác.

---

### III.1.2. Communication Diagrams (UML Collaboration Diagrams)

**Communication Diagrams** mô tả cùng một luồng tương tác như Sequence Diagrams nhưng tập trung vào **cấu trúc và mối quan hệ** giữa các đối tượng. Các đặc điểm:

- **Objects**: Các đối tượng được biểu diễn bằng hình chữ nhật
- **Links**: Đường kết nối giữa các đối tượng (thể hiện mối quan hệ)
- **Messages**: Được đánh số theo thứ tự (1, 2, 3...) để thể hiện luồng thực thi
- **Nested Messages**: Có thể có số thứ tự lồng nhau (1.1, 1.2, ...)
- **Layout**: Tự do, tập trung vào cấu trúc quan hệ, không bắt buộc theo thời gian

---

#### Communication Diagram 1: Authentication Flow

```mermaid
graph TB
    subgraph "Authentication Flow Communication"
        U[User]
        FE[Frontend]
        AC[AuthController]
        AS[AuthService]
        VS[VerificationService]
        ES[EmailService]
        SMS[SMSService]
        SS[SessionService]
        UR[UserRepository]
        DB[(Database)]
    end

    U -->|1: Register Request| FE
    FE -->|2: POST /api/auth/register| AC
    AC -->|3: register| AS
    AS -->|4: generateCode| VS
    VS -.->|4.1: verificationCode| AS
    AS -->|5: sendVerificationCode| ES
    ES -.->|5.1: Email sent| AS
    AS -->|6: sendVerificationCode| SMS
    SMS -.->|6.1: SMS sent| AS
    AS -->|7: save| UR
    UR -->|8: INSERT INTO users| DB
    DB -.->|8.1: User saved| UR
    UR -.->|7.1: User entity| AS
    AS -->|9: createSession| SS
    SS -.->|9.1: Session token| AS
    AS -.->|3.1: AuthResponse| AC
    AC -.->|2.1: 201 Created| FE
    FE -.->|1.1: Login successful| U

    style U fill:#e1f5ff
    style FE fill:#fff4e1
    style AC fill:#ffe1f5
    style AS fill:#ffe1f5
    style VS fill:#e1ffe1
    style ES fill:#e1ffe1
    style SMS fill:#e1ffe1
    style SS fill:#e1ffe1
    style UR fill:#f5e1ff
    style DB fill:#f5e1ff
```

**Ghi chú:**
- Mũi tên liền (`-->`): Gọi method/request
- Mũi tên đứt (`-.->`): Response/return value
- Số thứ tự (1, 2, 3...): Thể hiện thứ tự thực thi
- Layout tự do: Tập trung vào mối quan hệ giữa các đối tượng

---

#### Communication Diagram 2: Order & Payment Flow

```mermaid
graph TB
    subgraph "Order & Payment Flow Communication"
        C[Customer]
        FE[Frontend]
        OC[OrderController]
        OS[OrderService]
        ES[EventService]
        TR[TicketRepository]
        VNS[VNPayService]
        VNP[VNPay Gateway]
        EML[EmailService]
        QRS[QRCodeService]
        DB[(Database)]
    end

    C -->|1: Select tickets| FE
    FE -->|2: POST /api/orders| OC
    OC -->|3: createOrder| OS
    OS -->|4: validateEventAvailability| ES
    ES -.->|4.1: Event available| OS
    OS -->|5: reserveTickets| TR
    TR -->|6: UPDATE tickets| DB
    DB -.->|6.1: Tickets reserved| TR
    TR -.->|5.1: Reserved tickets| OS
    OS -->|7: INSERT INTO orders| DB
    DB -.->|7.1: Order created| OS
    OS -.->|3.1: OrderResponse| OC
    OC -.->|2.1: Order created| FE
    
    C -->|8: Initiate payment| FE
    FE -->|9: POST /api/orders/payment| OC
    OC -->|10: initiatePayment| OS
    OS -->|11: createPaymentUrl| VNS
    VNS -.->|11.1: Payment URL| OS
    OS -.->|10.1: Payment URL| OC
    OC -.->|9.1: Payment URL| FE
    FE -->|12: Redirect| VNP
    
    VNP -->|13: POST callback| OC
    OC -->|14: confirmPayment| OS
    OS -->|15: validatePaymentResponse| VNS
    VNS -->|16: Verify transaction| VNP
    VNP -.->|16.1: Transaction valid| VNS
    VNS -.->|15.1: Payment confirmed| OS
    OS -->|17: UPDATE orders| DB
    OS -->|18: UPDATE tickets| DB
    OS -->|19: generateQRCode| QRS
    QRS -.->|19.1: QR Code| OS
    OS -->|20: sendETickets| EML
    EML -.->|20.1: Email sent| OS
    OS -.->|14.1: OrderResponse| OC
    OC -.->|13.1: Payment confirmed| FE
    FE -.->|8.1: E-tickets received| C

    style C fill:#e1f5ff
    style FE fill:#fff4e1
    style OC fill:#ffe1f5
    style OS fill:#ffe1f5
    style ES fill:#e1ffe1
    style TR fill:#f5e1ff
    style VNS fill:#ffe1f5
    style VNP fill:#ff9800
    style EML fill:#e1ffe1
    style QRS fill:#e1ffe1
    style DB fill:#f5e1ff
```

---

#### Communication Diagram 3: Event Management (Organizer)

```mermaid
graph TB
    subgraph "Event Management Communication"
        ORG[Organizer]
        FE[Frontend]
        ORGC[OrganizerController]
        EVS[EventService]
        ORGS[OrganizerService]
        ER[EventRepository]
        ADMS[AdminService]
        EML[EmailService]
        DB[(Database)]
    end

    ORG -->|1: Create event| FE
    FE -->|2: POST /api/organizer/events| ORGC
    ORGC -->|3: createEvent| EVS
    EVS -->|4: validateOrganizerStatus| ORGS
    ORGS -.->|4.1: Organizer verified| EVS
    EVS -->|5: save| ER
    ER -->|6: INSERT INTO events| DB
    DB -.->|6.1: Event saved| ER
    ER -.->|5.1: Event entity| EVS
    
    EVS -->|7: UPDATE status='PENDING_APPROVAL'| DB
    EVS -->|8: notifyAdminForReview| ADMS
    ADMS -->|9: notifyAdmin| EML
    EML -.->|9.1: Notification sent| ADMS
    ADMS -.->|8.1: Notified| EVS
    
    EVS -.->|3.1: EventResponse| ORGC
    ORGC -.->|2.1: Event created| FE
    FE -.->|1.1: Event submitted| ORG
    
    ADMS -->|10: approveEvent| EVS
    EVS -->|11: UPDATE status='APPROVED'| DB
    EVS -->|12: notifyOrganizer| EML
    EML -.->|12.1: Notification sent| EVS

    style ORG fill:#e1f5ff
    style FE fill:#fff4e1
    style ORGC fill:#ffe1f5
    style EVS fill:#ffe1f5
    style ORGS fill:#e1ffe1
    style ER fill:#f5e1ff
    style ADMS fill:#ffe1f5
    style EML fill:#e1ffe1
    style DB fill:#f5e1ff
```

---

#### Communication Diagram 4: Check-In Process

```mermaid
graph TB
    subgraph "Check-In Process Communication"
        STF[Staff]
        FE[Frontend]
        CHKC[CheckInController]
        CHKS[CheckInService]
        TR[TicketRepository]
        QRS[QRCodeService]
        OR[OrderRepository]
        ALS[AuditLogService]
        DB[(Database)]
    end

    STF -->|1: Scan QR code| FE
    FE -->|2: POST /api/checkin/scan| CHKC
    CHKC -->|3: scanTicket| CHKS
    CHKS -->|4: decodeQRCode| QRS
    QRS -.->|4.1: Ticket ID| CHKS
    CHKS -->|5: findByTicketId| TR
    TR -->|6: SELECT * FROM tickets| DB
    DB -.->|6.1: Ticket data| TR
    TR -.->|5.1: Ticket entity| CHKS
    CHKS -->|7: findOrderByTicket| OR
    OR -->|8: SELECT * FROM orders| DB
    DB -.->|8.1: Order data| OR
    OR -.->|7.1: Order entity| CHKS
    
    CHKS -->|9: UPDATE tickets status='CHECKED_IN'| DB
    CHKS -->|10: logCheckIn| ALS
    ALS -->|11: INSERT INTO audit_logs| DB
    CHKS -.->|3.1: CheckInResponse success| CHKC
    CHKC -.->|2.1: Check-in result| FE
    FE -.->|1.1: Display result| STF

    style STF fill:#e1f5ff
    style FE fill:#fff4e1
    style CHKC fill:#ffe1f5
    style CHKS fill:#ffe1f5
    style TR fill:#f5e1ff
    style QRS fill:#e1ffe1
    style OR fill:#f5e1ff
    style ALS fill:#e1ffe1
    style DB fill:#f5e1ff
```

---

#### Communication Diagram 5: Refund Process

```mermaid
graph TB
    subgraph "Refund Process Communication"
        C[Customer]
        FE[Frontend]
        RC[RefundController]
        RS[RefundService]
        OS[OrderService]
        VNS[VNPayService]
        VNP[VNPay Gateway]
        TR[TicketRepository]
        EML[EmailService]
        DB[(Database)]
    end

    C -->|1: Request refund| FE
    FE -->|2: POST /api/refunds| RC
    RC -->|3: requestRefund| RS
    RS -->|4: getOrderDetails| OS
    OS -.->|4.1: Order details| RS
    RS -->|5: validateRefundEligibility| RS
    
    RS -->|6: INSERT INTO refund_info| DB
    RS -->|7: processRefund| VNS
    VNS -->|8: Refund API call| VNP
    VNP -.->|8.1: Refund processed| VNS
    VNS -.->|7.1: Refund successful| RS
    
    RS -->|9: UPDATE refund_info status='APPROVED'| DB
    RS -->|10: UPDATE orders status='REFUNDED'| DB
    RS -->|11: updateTicketStatus| TR
    TR -->|12: UPDATE tickets status='REFUNDED'| DB
    RS -->|13: sendRefundConfirmation| EML
    EML -.->|13.1: Email sent| RS
    
    RS -.->|3.1: RefundResponse| RC
    RC -.->|2.1: Refund processed| FE
    FE -.->|1.1: Refund status| C

    style C fill:#e1f5ff
    style FE fill:#fff4e1
    style RC fill:#ffe1f5
    style RS fill:#ffe1f5
    style OS fill:#e1ffe1
    style VNS fill:#ffe1f5
    style VNP fill:#ff9800
    style TR fill:#f5e1ff
    style EML fill:#e1ffe1
    style DB fill:#f5e1ff
```

---

#### Communication Diagram 6: Support Ticket Flow

```mermaid
graph TB
    subgraph "Support Ticket Communication"
        C[Customer]
        FE[Frontend]
        SC[SupportController]
        SS[SupportService]
        STR[SupportTicketRepository]
        EML[EmailService]
        ADMS[AdminService]
        DB[(Database)]
    end

    C -->|1: Create ticket| FE
    FE -->|2: POST /api/support/tickets| SC
    SC -->|3: createTicket| SS
    SS -->|4: save| STR
    STR -->|5: INSERT INTO support_tickets| DB
    DB -.->|5.1: Ticket saved| STR
    STR -.->|4.1: Ticket entity| SS
    SS -->|6: notifySupportTeam| EML
    EML -.->|6.1: Notification sent| SS
    SS -->|7: assignTicketToAdmin| ADMS
    ADMS -.->|7.1: Ticket assigned| SS
    SS -.->|3.1: SupportTicketResponse| SC
    SC -.->|2.1: Ticket created| FE
    FE -.->|1.1: Ticket ID received| C
    
    ADMS -->|8: respondToTicket| SS
    SS -->|9: UPDATE status='RESOLVED'| DB
    SS -->|10: notifyCustomer| EML
    EML -.->|10.1: Response notification| SS
    SS -.->|8.1: Response sent| ADMS

    style C fill:#e1f5ff
    style FE fill:#fff4e1
    style SC fill:#ffe1f5
    style SS fill:#ffe1f5
    style STR fill:#f5e1ff
    style EML fill:#e1ffe1
    style ADMS fill:#ffe1f5
    style DB fill:#f5e1ff
```

---

#### Communication Diagram 7: Admin Management

```mermaid
graph TB
    subgraph "Admin Management Communication"
        ADM[Admin]
        FE[Frontend]
        ADC[AdminController]
        ADMS[AdminService]
        EVS[EventService]
        ORGS[OrganizerService]
        WDS[WithdrawalService]
        SS[SupportService]
        STS[StatisticsService]
        EML[EmailService]
        DB[(Database)]
    end

    ADM -->|1: Review requests| FE
    FE -->|2: GET /api/admin/events/pending| ADC
    ADC -->|3: getPendingEvents| ADMS
    ADMS -->|4: SELECT * FROM events| DB
    DB -.->|4.1: Pending events| ADMS
    ADMS -.->|3.1: List of events| ADC
    ADC -.->|2.1: Events list| FE
    
    ADM -->|5: Approve event| FE
    FE -->|6: PUT /api/admin/events/{id}/approve| ADC
    ADC -->|7: approveEvent| ADMS
    ADMS -->|8: updateEventStatus| EVS
    EVS -->|9: UPDATE status='APPROVED'| DB
    EVS -->|10: notifyOrganizer| EML
    EML -.->|10.1: Notification sent| EVS
    
    ADMS -->|11: approveKyc| ORGS
    ORGS -->|12: UPDATE kyc_status='APPROVED'| DB
    ORGS -->|13: notifyOrganizer| EML
    EML -.->|13.1: Notification sent| ORGS
    
    ADMS -->|14: processWithdrawal| WDS
    WDS -->|15: UPDATE status='PROCESSED'| DB
    WDS -->|16: notifyOrganizer| EML
    EML -.->|16.1: Notification sent| WDS
    
    ADMS -->|17: getSystemStatistics| STS
    STS -->|18: Aggregate queries| DB
    DB -.->|18.1: Statistics data| STS
    STS -.->|17.1: System stats| ADMS
    ADMS -.->|6.1: Statistics response| ADC
    ADC -.->|5.1: Dashboard data| FE

    style ADM fill:#e1f5ff
    style FE fill:#fff4e1
    style ADC fill:#ffe1f5
    style ADMS fill:#ffe1f5
    style EVS fill:#e1ffe1
    style ORGS fill:#e1ffe1
    style WDS fill:#e1ffe1
    style SS fill:#e1ffe1
    style STS fill:#e1ffe1
    style EML fill:#e1ffe1
    style DB fill:#f5e1ff
```

---

#### Communication Diagram 8: Session Management & Security

```mermaid
graph TB
    subgraph "Session Management Communication"
        CL[Client]
        SF[SecurityFilter]
        SAF[SessionAuthFilter]
        SS[SessionService]
        SR[SessionRepository]
        CTRL[Controller]
        CUR[CurrentUserResolver]
        DB[(Database)]
    end

    CL -->|1: HTTP Request with token| SF
    SF -->|2: Process request| SAF
    SAF -->|3: Extract token| SAF
    SAF -->|4: validateSession| SS
    SS -->|5: findByToken| SR
    SR -->|6: SELECT * FROM sessions| DB
    DB -.->|6.1: Session data| SR
    SR -.->|5.1: Session entity| SS
    
    SS -->|7: checkExpiry| SS
    SS -->|8: updateLastAccess| SR
    SR -->|9: UPDATE last_access| DB
    SS -.->|4.1: Valid session| SAF
    SAF -->|10: Set authentication| SF
    SAF -->|11: Forward request| CTRL
    CTRL -->|12: Resolve @CurrentUser| CUR
    CUR -.->|12.1: User entity| CTRL
    CTRL -.->|11.1: Response| SAF
    SAF -.->|2.1: Response| SF
    SF -.->|1.1: Response| CL
    
    SS -->|13: DELETE expired session| SR
    SR -->|14: DELETE FROM sessions| DB

    style CL fill:#e1f5ff
    style SF fill:#ffe1f5
    style SAF fill:#ffe1f5
    style SS fill:#ffe1f5
    style SR fill:#f5e1ff
    style CTRL fill:#fff4e1
    style CUR fill:#e1ffe1
    style DB fill:#f5e1ff
```

---

### So sánh Sequence Diagram vs Communication Diagram

| Đặc điểm | Sequence Diagram | Communication Diagram |
|----------|------------------|----------------------|
| **Trọng tâm** | Thứ tự thời gian | Cấu trúc và mối quan hệ |
| **Layout** | Sắp xếp theo trục thời gian (trên→dưới) | Layout tự do |
| **Đối tượng** | Lifelines (đường sống) | Objects (hình chữ nhật) |
| **Messages** | Mũi tên theo thời gian | Số thứ tự trên links |
| **Ưu điểm** | Dễ theo dõi luồng thời gian | Dễ thấy cấu trúc quan hệ |
| **Phù hợp** | Phân tích luồng thực thi | Phân tích kiến trúc |

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

