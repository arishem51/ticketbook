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


