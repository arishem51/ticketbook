# VI. Applying Alternative Architecture Patterns

## VI.1 Applying the Microservices architecture

### 1. Problem Identification

In the current monolithic/layered architecture, modules such as event management, ticket booking, payment, and user management are tightly coupled within a single deployable unit. This creates the following non-functional limitations based on our project’s NFRs:

- **Performance (5,000 concurrent users)** – Single process and shared resources (DB connections, threads, GC) limit horizontal scaling on hot paths (inventory, checkout), causing contention during peak sales.
- **Availability (99.5% uptime)** – Any fault or rollout in one module impacts the whole app; no per-module health isolation or rolling deploys.
- **Reliability (≤0.1% transaction error)** – Cross-module calls are in-process without standardized idempotency/saga handling, increasing risk of partial failures for orders/payments.
- **Security (data protection)** – Coarse-grained trust boundary and secrets make least-privilege and per-module access control difficult; uniform session handling increases blast radius.
- **Scalability (10x spikes via load balancing)** – Entire app must scale together; cannot independently scale Inventory/Order/Payment services during launches.
- **Backup & Recovery (≤2h restore)** – Single large database slows restore and lacks per-domain recovery; limited support for event/outbox replays.
- **Monitoring & Logging (alerts for critical flows)** – Limited module-level visibility and SLOs; difficult to correlate logs and traces across business flows.
- **Notifications & Reporting (scheduled jobs)** – Cron jobs compete with request traffic; long-running jobs affect latency and cannot scale independently.

### 2. Microservice-Based Solution

Redesign the system using a **Microservices Architecture**, where each major function is an independent service communicating over HTTP (REST) via an **API Gateway**. Each service owns its data (database-per-service) and can be scaled and deployed independently.

- **Proposed Services (logical boundaries):**
  - Auth Service (authentication, sessions)
  - Event Service (events, categories)
  - Inventory Service (ticket types, stock reservation/release)
  - Order Service (orders, checkout, idempotency)
  - Payment Service (payment provider integration, webhooks)
  - Refund Service (refund policies and execution)
  - Notification Service (email/SMS/push, reminders)
  - Reporting/Analytics Service (sales reports)
  - Support Service (support tickets)

- **Gateway & Cross-Cutting:**
  - API Gateway for routing, rate limiting, and authentication offloading
  - Service Discovery for dynamic endpoint resolution
  - Observability (centralized logging, metrics, tracing)
  - Resilience (timeouts, retries, circuit breakers)

This solution directly addresses the limitations:
- Reusability: Clean APIs per service enable external/mobile reuse.
- Scalability: Scale hot services (e.g., Inventory/Order/Payment) independently.
- Maintainability: Smaller blast radius, independent deploys, clearer ownership.

### 3. Supporting Diagrams (documentation scope)

- Component Diagram (high-level logical):

  Frontend → API Gateway → { Auth | Event | Inventory | Order | Payment | Refund | Notification | Reporting | Support }

  Each service → its own database.

- Deployment Diagram (conceptual):

  Client → Load Balancer → API Gateway (N instances)

  API Gateway → Services (each N instances) → Databases (per service)

- Sequence (Checkout, conceptual):

  Frontend → Gateway → Order Service → Inventory Service (reserve)

  → Payment Service (initiate/confirm) → Order Service (confirm) → Notification Service

Note: This file documents intent and scope only. No code changes are implied.



#### Mermaid Diagrams (Lean per use case)

```mermaid
%%{init: {"flowchart": {"curve": "linear", "nodeSpacing": 90, "rankSpacing": 160}}}%%
flowchart TD
  %% Clients
  A[Web or Mobile Client] -->|HTTPS + JWT| G[API Gateway]

  %% MVP services per use case
  G --> AU[Auth Service]
  G --> EV[Event Service]
  G --> OR[Order Service]
  G --> PY[Payment Service]
  G --> TK[Ticket Service]
  G --> NF[Notification Service]

  %% Stateless auth and minimal Redis usage
  AU -->|issue/verify JWT| A
  subgraph RS[Redis]
    RBL[JWT blacklist]
    RRT[Refresh tokens]
    RH[15m holds & locks]
  end
  AU -.-> RS
  OR -.-> RH
  G -.-> RRL[Rate limit]
  RRL -.-> RS

  %% Kafka bus (minimal topics)
  subgraph KB[Kafka]
    KT[Topics:<br/>orders.events · payments.events · tickets.events]
  end
  OR <--> KB
  PY <--> KB
  TK <--> KB
  NF <--> KB

  %% Per-service DBs
  EV --- DEV[(Event DB)]
  OR --- DOR[(Order DB)]
  PY --- DPY[(Payment DB)]
  TK --- DTK[(Ticket DB)]

  %% External integrations
  PY --> VNP[VNPAY]
  NF --> EM[Email]
  NF --> SMS[SMS]
  TK --> OBJ[Object Storage<br/>QR images]
```

```mermaid
flowchart LR
  subgraph K8s[Kubernetes]
    IG[Ingress] --> GW[API Gateway]
    GW --> DPL_AU[Deployment: auth]
    GW --> DPL_EV[Deployment: event]
    GW --> DPL_OR[Deployment: order]
    GW --> DPL_PY[Deployment: payment]
    GW --> DPL_TK[Deployment: ticket]
    GW --> DPL_NF[Deployment: notification]

    HPA_OR[HPA: order] --- DPL_OR
    HPA_PY[HPA: payment] --- DPL_PY
    HPA_TK[HPA: ticket] --- DPL_TK

    KF[(Kafka)]
    RD[(Redis)]

    DPL_OR <--> KF
    DPL_PY <--> KF
    DPL_TK <--> KF
    DPL_NF <--> KF

    DPL_AU <--> RD
    DPL_OR <--> RD
  end
```

```mermaid
sequenceDiagram
  autonumber
  participant C as Client
  participant GW as API Gateway
  participant OR as Order Service
  participant KB as Kafka
  participant PY as Payment Service
  participant TK as Ticket Service
  participant NF as Notification Service

  Note over C,NF: Start of checkout saga

  C->>GW: POST /orders
  GW->>OR: createOrder(request, JWT)
  activate OR
  OR->>OR: Local TX reserve tickets
  OR->>OR: Set status PendingPayment
  OR-->>KB: OrderCreated

  PY-->>KB: (consumes) OrderCreated
  activate PY
  PY->>PY: Local TX create payment with VNPAY
  PY-->>KB: PaymentRequested
  deactivate PY
  activate NF
  NF-->>KB: (consumes) PaymentRequested → send link
  deactivate NF

  C->>VNPAY: Complete payment
  PY-->>KB: PaymentSucceeded or PaymentFailed

  OR-->>KB: (consumes) PaymentSucceeded → mark Confirmed
  activate TK
  TK-->>KB: (consumes) PaymentSucceeded → issue tickets + QR
  TK-->>KB: TicketsIssued
  deactivate TK
  activate NF
  NF-->>KB: (consumes) TicketsIssued → deliver QR
  deactivate NF

  OR-->>KB: (consumes) PaymentFailed → release reservation
  OR-->>KB: (consumes) PaymentFailed → mark Cancelled
  activate NF
  NF-->>KB: (consumes) PaymentFailed → notify failure
  deactivate NF

  deactivate OR
  Note over C,NF: End of checkout saga
```