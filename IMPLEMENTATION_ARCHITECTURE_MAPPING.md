# V. Implementation

## V.1 Map Architecture to the Structure of the Project

### 1. Overview of the Chosen Architecture

#### Architectural Style: **Layered Architecture (Multi-Tier Architecture)**

The Ticket Booking System follows a **Layered Architecture** pattern, also known as **Multi-Tier Architecture** or **N-Tier Architecture**. This architectural style organizes the application into distinct horizontal layers, each with specific responsibilities and clear separation of concerns.

#### Why Layered Architecture Was Selected

1. **Maintainability**
   - Clear separation of concerns makes it easy to locate and modify code
   - Each layer has a well-defined responsibility (e.g., controllers handle HTTP, services handle business logic)
   - Changes in one layer have minimal impact on other layers

2. **Scalability**
   - Stateless API design allows horizontal scaling
   - Service layer can be scaled independently from data layer
   - Clear boundaries enable microservices migration if needed

3. **Reusability**
   - Service layer methods can be reused across multiple controllers
   - Repository layer provides reusable data access patterns
   - Utility classes can be shared across the application

4. **Testability**
   - Each layer can be tested independently
   - Mock dependencies easily for unit testing
   - Clear interfaces between layers facilitate integration testing

5. **Security**
   - Centralized security configuration in SecurityConfig
   - Consistent authentication/authorization across all endpoints
   - Clear separation of public and protected resources

6. **Team Collaboration**
   - Different teams can work on different layers simultaneously
   - Clear contracts (DTOs, interfaces) between layers
   - Standard Spring Boot patterns familiar to Java developers

### 2. Mapping to Project Structure

The layered architecture maps to the project structure as follows:

#### Project Folder Structure

```
ticketbook/
└── src/main/java/com/swd/ticketbook/
    ├── controllers/          # Presentation Layer
    ├── services/             # Business Logic Layer
    ├── repositories/          # Data Access Layer
    ├── entities/             # Domain Model Layer
    ├── dto/                  # Data Transfer Objects
    ├── enums/                # Enumerations
    ├── exceptions/            # Exception Handling
    ├── security/              # Security Layer
    ├── config/                # Configuration Layer
    └── utils/                 # Utility Layer
```

#### Architecture Layers and Their Mappings

##### **Layer 1: Presentation Layer (Controllers)**

**Package**: `com.swd.ticketbook.controllers`

**Purpose**: Handles HTTP requests and responses, acts as the API gateway for the application.

**Mapping**:
- `AdminController.java` → Admin operations endpoints
- `AuthController.java` → Authentication endpoints (`/api/auth/*`)
- `OrderController.java` → Order management endpoints (`/api/orders/*`)
- `OrganizerController.java` → Organizer operations (`/api/organizer/*`)
- `PublicEventController.java` → Public event browsing (`/api/events/*`)
- `SupportController.java` → Support ticket endpoints (`/api/support/*`)
- `RefundController.java` → Refund endpoints (`/api/refunds/*`)
- `CheckInController.java` → Ticket verification (`/api/checkin/*`)

**Responsibilities**:
- Receive HTTP requests
- Validate request parameters (using `@Valid` annotations)
- Delegate business logic to service layer
- Transform responses to DTOs
- Handle HTTP status codes
- Inject current user via `@CurrentUser` annotation

**Key Technologies**:
- Spring `@RestController`
- Spring `@RequestMapping`
- Spring `@Valid` for validation
- Custom `@CurrentUser` argument resolver

---

##### **Layer 2: Security Layer**

**Package**: `com.swd.ticketbook.security`

**Purpose**: Implements authentication and authorization mechanisms.

**Mapping**:
- `SecurityConfig.java` → Spring Security configuration, role-based access control
- `SessionAuthenticationFilter.java` → Custom filter for session token validation
- `AuthenticationEntryPointImpl.java` → Handles unauthorized access
- `CurrentUser.java` → Annotation for injecting authenticated user
- `CurrentUserArgumentResolver.java` → Resolves `@CurrentUser` annotation

**Responsibilities**:
- Authenticate users via session tokens
- Authorize based on user roles (CUSTOMER, VERIFIED_ORGANIZER, ADMIN)
- Configure CORS policies
- Protect endpoints with role-based access control
- Extract and validate session tokens from HTTP headers

**Key Technologies**:
- Spring Security
- Custom `OncePerRequestFilter` for authentication
- `HandlerMethodArgumentResolver` for user injection

---

##### **Layer 3: Business Logic Layer (Services)**

**Package**: `com.swd.ticketbook.services`

**Purpose**: Contains all business logic and orchestrates operations between different components.

**Mapping**:
- **Core Business Services**:
  - `AuthService.java` → Authentication, registration, password management
  - `OrderService.java` → Order creation, payment processing, ticket generation
  - `EventService.java` → Event CRUD, search, filtering
  - `OrganizerService.java` → KYC verification, profile management
  - `CheckInService.java` → QR code verification, ticket validation

- **Payment & Financial Services**:
  - `VNPayService.java` → Payment gateway integration
  - `RefundService.java` → Refund processing
  - `WithdrawalService.java` → Organizer withdrawal requests

- **Administrative Services**:
  - `AdminService.java` → Admin operations
  - `StatisticsService.java` → Analytics and reporting
  - `AuditLogService.java` → Audit logging

- **Support Services**:
  - `SupportService.java` → Support ticket management
  - `EmailService.java` → Email notifications
  - `SMSService.java` → SMS notifications
  - `QRCodeService.java` → QR code generation
  - `VerificationCodeService.java` → Verification code management
  - `SessionService.java` → Session management

**Responsibilities**:
- Implement business rules (FR1-FR26)
- Orchestrate operations between repositories
- Coordinate with external services
- Handle transactions (`@Transactional`)
- Schedule background jobs (`@Scheduled`)
- Validate business constraints

**Key Technologies**:
- Spring `@Service`
- Spring `@Transactional` for transaction management
- Spring `@Scheduled` for background jobs
- Dependency injection via `@Autowired`

---

##### **Layer 4: Data Access Layer (Repositories)**

**Package**: `com.swd.ticketbook.repositories`

**Purpose**: Abstracts database operations using JPA/Hibernate.

**Mapping**:
- `UserRepository.java` → User entity operations
- `OrderRepository.java` → Order entity operations
- `EventRepository.java` → Event entity operations
- `TicketRepository.java` → Ticket entity operations
- `SessionRepository.java` → Session entity operations
- `PaymentRepository.java` → Payment entity operations
- `RefundInfoRepository.java` → Refund entity operations
- `SupportTicketRepository.java` → Support ticket operations
- `OrganizerProfileRepository.java` → Organizer profile operations
- `WithdrawalRequestRepository.java` → Withdrawal request operations
- `EventUpdateRequestRepository.java` → Event update request operations
- `TicketTypeRepository.java` → Ticket type operations
- `CategoryRepository.java` → Category operations
- `PasswordResetTokenRepository.java` → Password reset token operations
- `AuditLogRepository.java` → Audit log operations

**Responsibilities**:
- Define database queries
- Provide CRUD operations
- Implement custom query methods
- Handle entity relationships

**Key Technologies**:
- Spring Data JPA
- `JpaRepository<T, ID>` interface
- Custom query methods using method naming conventions
- `@Query` annotations for complex queries

---

##### **Layer 5: Domain Model Layer (Entities)**

**Package**: `com.swd.ticketbook.entities`

**Purpose**: Represents core business entities and domain models.

**Mapping**:
- **Core Entities**:
  - `User.java` → User entity with authentication and profile
  - `Event.java` → Event entity with status and details
  - `Order.java` → Order entity with booking status
  - `Ticket.java` → Ticket entity with QR codes
  - `TicketType.java` → Ticket type with pricing
  - `Payment.java` → Payment entity
  - `Category.java` → Event category

- **Supporting Entities**:
  - `OrganizerProfile.java` → Organizer KYC and profile
  - `Session.java` → User session management
  - `RefundInfo.java` → Refund information
  - `SupportTicket.java` → Support ticket
  - `EventUpdateRequest.java` → Event update requests
  - `WithdrawalRequest.java` → Withdrawal requests
  - `PasswordResetToken.java` → Password reset tokens
  - `AuditLog.java` → Audit logging

**Responsibilities**:
- Define entity relationships (OneToMany, ManyToOne, etc.)
- Implement domain logic (business methods)
- Enforce business rules at entity level
- Map to database tables via JPA annotations

**Key Technologies**:
- JPA/Hibernate
- `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
- `@OneToMany`, `@ManyToOne`, `@OneToOne` for relationships
- Lombok `@Data` for getters/setters

---

##### **Layer 6: Data Transfer Objects (DTOs)**

**Package**: `com.swd.ticketbook.dto`

**Purpose**: Transfers data between layers without exposing entity internals.

**Sub-packages**:
- `dto.admin/` → Admin-related DTOs
- `dto.auth/` → Authentication DTOs
- `dto.checkin/` → Check-in DTOs
- `dto.event/` → Event DTOs
- `dto.order/` → Order DTOs
- `dto.organizer/` → Organizer DTOs
- `dto.refund/` → Refund DTOs
- `dto.support/` → Support DTOs

**Responsibilities**:
- Define request/response structures
- Validate input data (Bean Validation)
- Transform between entities and DTOs
- Prevent over-fetching of data

**Key Technologies**:
- Bean Validation (`@Valid`, `@NotNull`, `@Email`, etc.)
- Lombok for data classes

---

##### **Layer 7: Configuration Layer**

**Package**: `com.swd.ticketbook.config`

**Purpose**: Application-wide configuration and setup.

**Mapping**:
- `WebConfig.java` → Web configuration (CORS, argument resolvers, etc.)

**Responsibilities**:
- Configure Spring MVC components
- Register custom argument resolvers
- Configure CORS policies
- Set up interceptors

---

##### **Layer 8: Exception Handling Layer**

**Package**: `com.swd.ticketbook.exceptions`

**Purpose**: Centralized exception handling.

**Mapping**:
- `GlobalExceptionHandler.java` → Global exception handler
- `ResourceNotFoundException.java` → Resource not found exception
- `BusinessRuleViolationException.java` → Business rule violation
- `UnauthorizedException.java` → Unauthorized access
- `DuplicateResourceException.java` → Duplicate resource

**Responsibilities**:
- Catch and handle exceptions globally
- Return consistent error responses
- Log exceptions appropriately
- Map exceptions to HTTP status codes

**Key Technologies**:
- Spring `@RestControllerAdvice`
- `@ExceptionHandler` annotations

---

##### **Layer 9: Utility Layer**

**Package**: `com.swd.ticketbook.utils`

**Purpose**: Reusable utility classes.

**Mapping**:
- `PasswordEncoderUtil.java` → Password encoding utilities

---

##### **Layer 10: Enumerations**

**Package**: `com.swd.ticketbook.enums`

**Purpose**: Define type-safe constants for domain values.

**Mapping**:
- `UserRole.java` → User roles (CUSTOMER, VERIFIED_ORGANIZER, ADMIN)
- `BookingStatus.java` → Order statuses
- `EventStatus.java` → Event statuses
- `TicketStatus.java` → Ticket statuses
- `PaymentStatus.java` → Payment statuses
- `RefundStatus.java` → Refund statuses
- `SupportTicketStatus.java` → Support ticket statuses

---

### Layer Interaction Flow

```
HTTP Request
    ↓
[Presentation Layer: Controllers]
    ↓ (validates request, injects DTOs)
[Security Layer: Filters & Security]
    ↓ (authenticates, authorizes)
[Business Logic Layer: Services]
    ↓ (implements business rules)
[Data Access Layer: Repositories]
    ↓ (queries database)
[Domain Model Layer: Entities]
    ↓ (maps to database tables)
[PostgreSQL Database]
```

### Summary Table: Architecture to Code Mapping

| Architecture Layer | Package | Key Components | Responsibilities |
|-------------------|---------|----------------|------------------|
| **Presentation** | `controllers/` | REST Controllers | HTTP handling, request/response transformation |
| **Security** | `security/` | Filters, Config, Resolvers | Authentication, authorization, user injection |
| **Business Logic** | `services/` | Service classes | Business rules, orchestration, transactions |
| **Data Access** | `repositories/` | JPA Repositories | Database queries, CRUD operations |
| **Domain Model** | `entities/` | JPA Entities | Domain objects, relationships, business methods |
| **Data Transfer** | `dto/` | DTO classes | Request/response structures, validation |
| **Configuration** | `config/` | Config classes | Spring configuration, CORS, resolvers |
| **Exception Handling** | `exceptions/` | Exception classes | Error handling, consistent responses |
| **Utilities** | `utils/` | Utility classes | Reusable helper methods |
| **Enumerations** | `enums/` | Enum classes | Type-safe constants |

---

## V.2 Map Class Diagram and Interaction Diagram to Code

### Overview

This section demonstrates how the Class Diagram and Interaction Diagrams are implemented in the actual codebase, showcasing the use of various design patterns.

### Design Patterns Identified

1. **Layered Architecture Pattern**
2. **Repository Pattern**
3. **Service Layer Pattern**
4. **DTO (Data Transfer Object) Pattern**
5. **Dependency Injection Pattern**
6. **Template Method Pattern**
7. **Strategy Pattern** (implicit in payment processing)
8. **Factory Pattern** (Spring bean creation)
9. **Filter Pattern** (Authentication)
10. **Argument Resolver Pattern**
11. **Scheduled Task Pattern**
12. **Exception Handler Pattern**

---

### Feature 1: Order Creation and Payment Processing

#### Class Diagram Implementation

**From Class Diagram**:
- `Order` entity with `BookingStatus` enum
- `Order` has relationships with `User`, `Event`, `Ticket`, and `Payment`
- `Order` has business methods: `isExpired()`, `confirm()`, `cancel()`, `expire()`

**Code Implementation**:

```java
// Entity: Order.java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    
    @ManyToOne
    private User user;
    
    @ManyToOne
    private Event event;
    
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;
    
    @OneToMany(mappedBy = "order")
    private Set<Ticket> tickets;
    
    @OneToOne(mappedBy = "order")
    private Payment payment;
    
    // Business methods from class diagram
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(reservationExpiresAt);
    }
    
    public void confirm() {
        this.bookingStatus = BookingStatus.CONFIRMED;
        this.completedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        this.bookingStatus = BookingStatus.CANCELLED;
    }
    
    public void expire() {
        this.bookingStatus = BookingStatus.EXPIRED;
    }
}
```

**Design Patterns Used**:
- **Domain-Driven Design**: Business logic in entity methods
- **Template Method Pattern**: `confirm()`, `cancel()`, `expire()` follow a consistent pattern

---

#### Interaction Diagram Implementation

**Sequence Flow** (from SYSTEM_HIGH_LEVEL_DESIGN.md):

```
User → React Frontend → OrderController → OrderService → 
  EventRepository → TicketTypeRepository → OrderRepository → 
  VNPayService → EmailService → Database
```

**Code Implementation**:

```java
// Controller: OrderController.java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @CurrentUser User user) {
        
        OrderResponse order = orderService.createOrder(user.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(order, "Order created successfully!"));
    }
}
```

```java
// Service: OrderService.java
@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private VNPayService vnPayService;
    
    @Autowired
    private EmailService emailService;
    
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        // 1. Validate user has no pending order (FR5)
        Optional<Order> existingPending = orderRepository
            .findActivePendingOrderByUserId(userId, LocalDateTime.now());
        
        if (existingPending.isPresent()) {
            throw new BusinessRuleViolationException(
                "You have a pending order. Please complete or cancel it first.");
        }
        
        // 2. Get and validate event
        Event event = eventRepository.findById(request.getEventId())
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        // 3. Create order
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Order order = new Order(user, event);
        
        // 4. Calculate totals and reserve tickets
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest item : request.getItems()) {
            TicketType ticketType = ticketTypeRepository.findById(item.getTicketTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));
            
            ticketType.reserve(item.getQuantity());
            totalAmount = totalAmount.add(
                ticketType.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        
        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);
        
        return mapToOrderResponse(order);
    }
    
    @Transactional
    public String initiatePayment(Long userId, PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Validate order
        if (order.isExpired()) {
            cancelExpiredOrder(order);
            throw new BusinessRuleViolationException("Order reservation has expired.");
        }
        
        // Generate payment URL
        String paymentUrl = vnPayService.createPaymentUrl(
            order.getOrderId().toString(),
            order.getTotalAmount(),
            "Order #" + order.getOrderId(),
            request.getReturnUrl()
        );
        
        return paymentUrl;
    }
    
    @Transactional
    public OrderResponse confirmPayment(Long orderId, String transactionId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Confirm order
        order.confirm();
        orderRepository.save(order);
        
        // Generate tickets with QR codes
        List<Ticket> tickets = generateTicketsForOrder(order);
        
        // Send confirmation email
        emailService.sendOrderConfirmation(
            order.getRecipientEmail(),
            order.getOrderId().toString(),
            order.getEvent().getName(),
            order.getTotalAmount(),
            tickets
        );
        
        return mapToOrderResponse(order);
    }
}
```

**Design Patterns Used**:
- **Service Layer Pattern**: Business logic in service class
- **Repository Pattern**: Data access through repositories
- **Dependency Injection**: `@Autowired` for loose coupling
- **Transaction Management**: `@Transactional` for data consistency

---

### Feature 2: Authentication and Session Management

#### Class Diagram Implementation

**From Class Diagram**:
- `User` entity with `UserRole` enum
- `Session` entity with `isValid()` method
- `User` has composition with `Session` (1 to many)

**Code Implementation**:

```java
// Entity: User.java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    
    private String fullName;
    private String contact;
    private String password;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    // Business methods
    public boolean isEmail() {
        return contact != null && contact.contains("@");
    }
    
    public boolean isPhone() {
        return contact != null && !contact.contains("@");
    }
}
```

```java
// Entity: Session.java
@Entity
@Table(name = "sessions")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;
    
    private String sessionToken;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    
    @ManyToOne
    private User user;
    
    // Business method from class diagram
    public boolean isValid() {
        return isActive && LocalDateTime.now().isBefore(expiresAt);
    }
}
```

#### Interaction Diagram Implementation

**Sequence Flow**: User Registration and Login

**Code Implementation**:

```java
// Controller: AuthController.java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        
        AuthResponse response = authService.register(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Registration successful"));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }
}
```

```java
// Service: AuthService.java
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private EmailService emailService;
    
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        // 1. Validate input
        if (!request.hasEmailOrPhone()) {
            throw new IllegalArgumentException("Email or phone number is required");
        }
        
        // 2. Check for duplicates
        String contact = request.getEmail() != null ? request.getEmail() : request.getPhone();
        if (userRepository.existsByContactIgnoreCase(contact)) {
            throw new IllegalArgumentException("This contact is already registered.");
        }
        
        // 3. Create user
        String encodedPassword = PasswordEncoderUtil.encode(request.getPassword());
        User user = new User(request.getFullName(), contact, encodedPassword, UserRole.CUSTOMER);
        user = userRepository.save(user);
        
        // 4. Create session
        String sessionToken = sessionService.createSession(user, httpRequest);
        
        // 5. Send welcome email
        emailService.sendWelcomeEmail(user.getContact(), user.getFullName());
        
        return new AuthResponse(sessionToken, user);
    }
}
```

**Design Patterns Used**:
- **Service Layer Pattern**: Business logic in AuthService
- **Dependency Injection**: Services injected via `@Autowired`
- **Repository Pattern**: UserRepository for data access

---

### Feature 3: Security and Authentication Filter

#### Filter Pattern Implementation

**Code Implementation**:

```java
// Filter: SessionAuthenticationFilter.java
@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private SessionService sessionService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        
        String sessionToken = extractSessionToken(request);
        
        if (sessionToken != null && 
            SecurityContextHolder.getContext().getAuthentication() == null) {
            
            Optional<User> userOpt = sessionService.validateSession(sessionToken);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );
                
                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractSessionToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }
}
```

**Design Patterns Used**:
- **Filter Pattern**: Intercepts all requests for authentication
- **Chain of Responsibility**: Filter chain pattern from Spring Security
- **Template Method Pattern**: `OncePerRequestFilter` provides template method structure

---

### Feature 4: Argument Resolver Pattern

#### Custom Argument Resolver

**Code Implementation**:

```java
// Resolver: CurrentUserArgumentResolver.java
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(CurrentUser.class) != null
            && parameter.getParameterType().equals(User.class);
    }
    
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                 ModelAndViewContainer mavContainer,
                                 NativeWebRequest webRequest,
                                 WebDataBinderFactory binderFactory) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return authentication.getPrincipal();
        }
        
        return null;
    }
}
```

**Usage in Controller**:

```java
@GetMapping("/pending")
public ResponseEntity<ApiResponse<OrderResponse>> getActivePendingOrder(
        @CurrentUser User user) {  // ← Custom resolver injects User here
    
    Optional<OrderResponse> pendingOrder = orderService.getActivePendingOrder(user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(pendingOrder.orElse(null)));
}
```

**Design Patterns Used**:
- **Argument Resolver Pattern**: Custom resolver for dependency injection
- **Strategy Pattern**: Different resolvers for different parameter types

---

### Feature 5: Scheduled Task Pattern

#### Background Job Implementation

**Code Implementation**:

```java
// Service: OrderService.java
@Service
public class OrderService {
    
    /**
     * FR16: Background job to auto-cancel expired orders
     * Runs every minute
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    @Transactional
    public void cleanupExpiredOrders() {
        List<Order> expiredOrders = orderRepository.findExpiredPendingOrders(LocalDateTime.now());
        
        for (Order order : expiredOrders) {
            order.expire();
            orderRepository.save(order);
            releaseTicketsForOrder(order);
        }
    }
}
```

**Design Patterns Used**:
- **Scheduled Task Pattern**: Automated background processing
- **Template Method Pattern**: Spring's `@Scheduled` provides template structure

---

### Feature 6: Exception Handling Pattern

#### Global Exception Handler

**Code Implementation**:

```java
// Handler: GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRuleViolationException(
            BusinessRuleViolationException ex) {
        logger.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(
            UnauthorizedException ex) {
        logger.warn("Unauthorized access: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex) {
        logger.error("Unexpected error occurred", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("An unexpected error occurred. Please try again later."));
    }
}
```

**Design Patterns Used**:
- **Exception Handler Pattern**: Centralized exception handling
- **Strategy Pattern**: Different handlers for different exception types

---

### Feature 7: DTO Pattern

#### Data Transfer Object Implementation

**Code Implementation**:

```java
// DTO: CreateOrderRequest.java
public class CreateOrderRequest {
    @NotNull(message = "Event ID is required")
    private Long eventId;
    
    @NotEmpty(message = "Order items are required")
    @Valid
    private List<OrderItemRequest> items;
    
    @NotNull(message = "Recipient information is required")
    @Valid
    private RecipientInfoRequest recipientInfo;
    
    // Getters and setters
}

// DTO: OrderResponse.java
public class OrderResponse {
    private Long orderId;
    private Long eventId;
    private String eventName;
    private BookingStatus bookingStatus;
    private BigDecimal totalAmount;
    private Integer totalQuantity;
    private LocalDateTime orderDate;
    private LocalDateTime reservationExpiresAt;
    private Long remainingSeconds;
    private List<TicketResponse> tickets;
    
    // Getters and setters
}
```

**Service Layer Mapping**:

```java
// Service: OrderService.java
private OrderResponse mapToOrderResponse(Order order) {
    OrderResponse response = new OrderResponse();
    response.setOrderId(order.getOrderId());
    response.setEventId(order.getEvent().getEventId());
    response.setEventName(order.getEvent().getName());
    response.setBookingStatus(order.getBookingStatus());
    response.setTotalAmount(order.getTotalAmount());
    // ... map other fields
    
    return response;
}
```

**Design Patterns Used**:
- **DTO Pattern**: Separates API contracts from domain models
- **Mapper Pattern**: Transformation between entities and DTOs

---

### Summary: Design Patterns in Implementation

| Design Pattern | Location | Example |
|----------------|----------|---------|
| **Layered Architecture** | Project structure | Controllers → Services → Repositories |
| **Repository Pattern** | `repositories/` | `OrderRepository`, `UserRepository` |
| **Service Layer Pattern** | `services/` | `OrderService`, `AuthService` |
| **DTO Pattern** | `dto/` | `CreateOrderRequest`, `OrderResponse` |
| **Dependency Injection** | Throughout | `@Autowired` annotations |
| **Template Method** | Entities | `Order.confirm()`, `cancel()`, `expire()` |
| **Filter Pattern** | `security/` | `SessionAuthenticationFilter` |
| **Argument Resolver** | `security/` | `CurrentUserArgumentResolver` |
| **Scheduled Task** | Services | `@Scheduled` in `OrderService` |
| **Exception Handler** | `exceptions/` | `GlobalExceptionHandler` |
| **Factory Pattern** | Spring Framework | Bean creation by Spring container |
| **Strategy Pattern** | Implicit | Payment processing (extensible for multiple gateways) |

---

### Conclusion

The implementation successfully maps the Class Diagram and Interaction Diagrams to actual code, demonstrating:

1. **Clear separation of concerns** through layered architecture
2. **Reusable patterns** through service layer and repository pattern
3. **Type safety** through enums and DTOs
4. **Security** through filter and resolver patterns
5. **Maintainability** through exception handling and dependency injection
6. **Scalability** through stateless design and scheduled tasks

All design patterns are implemented following Spring Boot best practices and industry standards.

