# PayPal-Like Portfolio Project -- Ticket Backlog (OO-Oriented, Tech-Agnostic)

This document converts the feature/architecture blueprint into an
implementation backlog of **tickets**. Tickets are ordered by
**importance** and **dependency** (prerequisites first), so you can work
top-to-bottom.

## Conventions

### Priority & Weight

- **P0 (Must)**: required for a coherent MVP; unblock many other
    tickets.
- **P1 (Should)**: important product value; depends on P0 foundations.
- **P2 (Could)**: nice-to-have; demonstrates polish or advanced
    engineering.
- **P3 (Stretch)**: optional, portfolio "wow" items.

Each ticket includes a **Weight (1--8)**: - 1--2: small - 3--5: medium -
6--8: large / multi-day

### OO Language Assumptions (Tech-Agnostic)

The design assumes: - **Entities** (domain objects with identity) -
**Value Objects** (immutable, no identity) - **Services**
(domain/application services) - **Repositories** (persistence
abstraction) - **Ports/Adapters** (external integration boundaries)

### Ticket Tracking Fields

- **Implemented:** Uses Atlassian workflow terminology. Set to `To Do` when ready for implementation, `In Progress` when work has started, and `Done` once implementation is completed.
- **Reference File:** The file name/path that documents or implements the change. Use `N/A` until one exists.
- **PR URL:** Link to the pull request implementing the ticket. Use `N/A` until opened.

------------------------------------------------------------------------

## Milestone 0 -- Project Skeleton & Guardrails (P0)

### T-0001: Repository Skeleton, Build, and Quality Gates

- **Priority:** P0\
- **Weight:** 3\
- **Implemented:** Implemented\\
- **Reference File:** N/A\\
- **PR URL:** [T-0001: Repository skeleton, build, and quality gates](https://github.com/JacobKurtzbergsNickname/feathered-oof-bird/pull/8/changes#diff-b803fcb7f17ed9235f1e5cb1fcd2f5d3b2838429d4368ae4c57ce4436577f03f)
 Establish a consistent project structure and quality
    gates.
- **Implementation Notes (OO):**
  - Create bounded modules/packages: `Identity`, `Ledger`,
        `Payments`, `Notifications`, `Admin`, `Risk`.
  - Introduce `Domain`, `Application`, `Infrastructure` layers
        (names can vary).
- **Acceptance Criteria:**
  - Project builds locally with one command.
  - Linting + formatting configured.
  - Tests can run in CI.
  - Pre-commit hooks (optional) or CI checks enforce baseline
        quality.

### T-0002: Runtime Configuration & Secret Handling

- **Priority:** P0\
- **Weight:** 2\
- **Implemented:** Done\\
- **Reference File:** backend/src/main/java/com/paypalclone/featheredoofbird/shared/config/AppConfig.java, shared/config/AppConfigurationModule.java, shared/config/EnvironmentSecretProvider.java, shared/secrets/ISecretProvider.java, shared/secrets/SecretNotFoundException.java\\
- **PR URL:** N/A\\
 Standardize configuration for local/dev/prod.
- **Implementation Notes (OO):**
  - `Config` object (immutable) injected where needed.
  - Define `ISecretProvider` (or equivalent) for future secret
        stores.
- **Acceptance Criteria:**
  - Configuration values come from environment variables / config
        files.
  - No secrets committed to repo.
  - App can boot with sane defaults for local dev.

### T-0003: Observability Baseline (Logging, Correlation, Error Model)

- **Priority:** P0\
- **Weight:** 3\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Ensure every request can be traced and errors are
    consistent.
- **Implementation Notes (OO):**
  - Define `CorrelationId` value object.
  - Define `ApiError` contract (code, message, traceId).
- **Acceptance Criteria:**
  - All API responses include a correlation/trace identifier.
  - Errors returned in a consistent structure.
  - Server logs include correlation id for each request.

------------------------------------------------------------------------

## Milestone 1 -- Identity & Access Control (P0)

### T-0101: User Registration and Authentication

- **Priority:** P0\
- **Weight:** 5\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Users can sign up and sign in.
- **OO Objects:**
  - Entity: `User`
  - Value Objects: `EmailAddress`, `PasswordHash`
  - Services: `AuthService`, `PasswordHasher`
  - Repository: `UserRepository`
- **Acceptance Criteria:**
  - Register endpoint creates a user with hashed password.
  - Login endpoint returns session/JWT.
  - Rate limit login attempts (basic).
  - Password hashing uses a modern algorithm (argon2/bcrypt).

### T-0102: Email Verification (Lightweight)

- **Priority:** P0\
- **Weight:** 3\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Verify primary email to reduce fraud and enable
    notifications.
- **OO Objects:**
  - Entity: `EmailVerificationToken`
  - Service: `VerificationService`
- **Acceptance Criteria:**
  - Register generates verification token.
  - Verify endpoint marks user email as verified.
  - Token expires and cannot be reused.

### T-0103: Roles and Authorization Guards

- **Priority:** P0\
- **Weight:** 3\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Protect endpoints based on roles and ownership.
- **OO Objects:**
  - Value Object: `Role`
  - Service: `AuthorizationService`
- **Acceptance Criteria:**
  - Role types: `PERSONAL`, `BUSINESS`, `ADMIN` (minimum).
  - Ownership checks exist for wallet/transactions.
  - Admin endpoints require admin role.

------------------------------------------------------------------------

## Milestone 2 -- Ledger Foundation (P0, highest dependency hub)

### T-0201: Money & Currency Value Objects

- **Priority:** P0\
- **Weight:** 3\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Prevent floating point and currency mistakes.
- **OO Objects:**
  - Value Object: `Money(amountMinor: int, currency: Currency)`
  - Value Object: `Currency(code: string)`
- **Acceptance Criteria:**
  - All amounts stored and passed as minor units (e.g., cents).
  - Currency is explicit everywhere.
  - Money supports safe arithmetic (add/subtract same currency).

### T-0202: Ledger Domain Model (Double-Entry)

- **Priority:** P0\
- **Weight:** 8\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Create an append-only double-entry ledger.
- **OO Objects:**
  - Entities: `LedgerAccount`, `LedgerEntry`, `LedgerTransaction`
  - Services: `LedgerService`
  - Repositories: `LedgerAccountRepository`,
        `LedgerEntryRepository`, `LedgerTransactionRepository`
- **Acceptance Criteria:**
  - Writing a transaction creates entries whose sum is **exactly
        zero** per currency.
  - Entries are append-only; no updates/deletes (except hard admin
        maintenance in dev).
  - Atomicity: transaction + entries persist in one DB transaction.
  - Concurrency: ledger write is safe under parallel requests.

### T-0203: Wallet Accounts Provisioning

- **Priority:** P0\
- **Weight:** 5\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Each user gets a wallet ledger account (and supporting
    internal accounts).
- **OO Objects:**
  - Service: `WalletProvisioningService`
- **Acceptance Criteria:**
  - On user creation (or first login), create:
    - user wallet account
    - platform clearing account
    - platform fee account
    - optional hold account
  - Accounts are per-currency (start with 1 currency for MVP).

### T-0204: Balance Query (Projection v1)

- **Priority:** P0\
- **Weight:** 4\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Provide wallet balance and transaction history views.
- **OO Objects:**
  - Query Service: `BalanceQueryService`
  - DTOs/View Models: `BalanceView`, `TransactionListView`
- **Acceptance Criteria:**
  - Endpoint returns current balance computed from ledger entries.
  - Endpoint returns paginated transaction history.
  - Filters: date range, type, status (basic).

------------------------------------------------------------------------

## Milestone 3 -- Payments Core (P0)

### T-0301: Payment State Machine (Core Types + Transitions)

- **Priority:** P0\
- **Weight:** 5\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Model payments as a state machine with safe transitions.
- **OO Objects:**
  - Entity: `Payment`
  - Value Object: `PaymentStatus`
  - Service: `PaymentStateMachine`
- **Acceptance Criteria:**
  - States: `CREATED`, `AUTHORIZED`, `CAPTURED`, `COMPLETED`,
        `FAILED`, `REVERSED` (minimum).
  - Transition rules enforced centrally (not scattered in
        controllers).
  - Invalid transitions are rejected with meaningful errors.

### T-0302: Idempotency Keys for Write APIs

- **Priority:** P0\
- **Weight:** 5\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Ensure retries don't double-charge/double-write.
- **OO Objects:**
  - Entity: `IdempotencyRecord`
  - Service: `IdempotencyService`
  - Repository: `IdempotencyRepository`
- **Acceptance Criteria:**
  - Write endpoints accept `Idempotency-Key` header.
  - If key reused with same request hash → return stored response.
  - If key reused with different request hash → reject (conflict).

### T-0303: P2P Send Money (Happy Path)

- **Priority:** P0\
- **Weight:** 6\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Send money from one user wallet to another with ledger
    correctness.
- **OO Objects:**
  - Service: `P2PPaymentService`
  - Policies: `SufficientFundsPolicy`
- **Acceptance Criteria:**
  - Sender selects recipient (by email/username).
  - Ledger entries move funds: sender wallet → recipient wallet (or
        via clearing).
  - Payment ends `COMPLETED` and appears in both histories.
  - Insufficient funds returns a domain error (no partial writes).

### T-0304: Money Requests (Create & Settle)

- **Priority:** P0\
- **Weight:** 5\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Allow a user to request money and the other to pay it.
- **OO Objects:**
  - Entity: `MoneyRequest`
  - Service: `RequestService`
- **Acceptance Criteria:**
  - Create request (amount, note, payer identity).
  - Payer can accept and triggers standard P2P flow.
  - Request statuses: `OPEN`, `PAID`, `CANCELED`, `EXPIRED`.

### T-0305: Claimable Payments (Recipient Not Registered)

- **Priority:** P1\
- **Weight:** 5\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Send to an email that doesn't exist yet; recipient can
    claim later.
- **OO Objects:**
  - Entity: `ClaimablePayment`
  - Service: `ClaimService`
- **Acceptance Criteria:**
  - Payment created with "claim token" linked to email.
  - Funds placed into a holding/clearing account until claimed or
        expired.
  - Claim transfers funds to recipient wallet after
        signup/verification.

------------------------------------------------------------------------

## Milestone 4 -- Merchant Checkout (P1, showcases platform capability)

### T-0401: Merchant Account Type & API Credentials

- **Priority:** P1\
- **Weight:** 5\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Allow "business" users to act as merchants.
- **OO Objects:**
  - Entity: `Merchant`
  - Value Object: `ApiKey`, `ApiSecret` (or token)
  - Service: `CredentialService`
- **Acceptance Criteria:**
  - Business user can create merchant profile.
  - Merchant receives API credentials (rotatable).
  - Requests authenticate merchants via credentials.

### T-0402: Orders API (Create)

- **Priority:** P1\
- **Weight:** 5\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Create checkout orders.
- **OO Objects:**
  - Entity: `Order`
  - Value Objects: `LineItem`, `OrderTotal`
  - Service: `OrderService`
- **Acceptance Criteria:**
  - `POST /orders` creates order with items and total.
  - Currency validated.
  - Order status: `CREATED`.

### T-0403: Approve & Capture API

- **Priority:** P1\
- **Weight:** 6\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Mimic PayPal's approve→capture flow.
- **OO Objects:**
  - Service: `CheckoutService`
- **Acceptance Criteria:**
  - Approve requires authenticated payer.
  - Capture performs ledger movement: payer wallet → merchant wallet
        (+ fees).
  - Order transitions: `CREATED` → `APPROVED` → `CAPTURED` →
        `COMPLETED`.
  - Failure leaves no partial ledger writes.

### T-0404: Return URLs and Receipt View

- **Priority:** P2\
- **Weight:** 3\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Basic UI endpoints for success/cancel and receipt display.
- **Acceptance Criteria:**
  - User sees receipt with order/payment details.
  - Receipt can be retrieved by id (auth protected).

------------------------------------------------------------------------

## Milestone 5 -- Fees, Holds, and Pending (P1)

### T-0501: Notifications Foundation (Email + In-App Stub)

- **Priority:** P1\
- **Weight:** 4\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Provide a notification port to use across flows.
- **OO Objects:**
  - Port: `INotificationSender`
  - Adapters: `ConsoleEmailSender` (dev), `RealEmailSender`
        (optional)
- **Acceptance Criteria:**
  - Events can trigger notifications.
  - Templates are centralized and testable.

### T-0502: Fee Engine (Deterministic)

- **Priority:** P1\
- **Weight:** 4\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Compute and apply platform fees.
- **OO Objects:**
  - Value Object: `Fee`
  - Service: `FeeCalculator`
  - Policy: `FeePolicy`
- **Acceptance Criteria:**
  - Fee calculation is pure and deterministic.
  - Checkout capture books fee to platform fee account via ledger
        entries.
  - Fee breakdown appears in receipt/transaction metadata.

### T-0503: Holds & Release Jobs

- **Priority:** P2\
- **Weight:** 6\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Introduce pending/held balances for risk or delayed
    settlement.
- **OO Objects:**
  - Service: `HoldService`
  - Job: `HoldReleaseJob`
- **Acceptance Criteria:**
  - A payment can be held → moves funds to hold account.
  - Release job settles to final destination after delay or
        approval.
  - Held funds show up as "held" in balance view.

------------------------------------------------------------------------

## Milestone 6 -- Refunds & Disputes (P1/P2)

### T-0601: Refunds (Full & Partial)

- **Priority:** P1\
- **Weight:** 6\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Reverse payments safely via ledger.
- **OO Objects:**
  - Entity: `Refund`
  - Service: `RefundService`
- **Acceptance Criteria:**
  - Refund references original payment/order.
  - Ledger entries reverse net/gross correctly (including fees
        policy decision).
  - Refund appears in transaction history.
  - Partial refunds supported (sum of partials ≤ original).

### T-0602: Dispute Case (CRUD + Status Flow)

- **Priority:** P2\
- **Weight:** 6\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Allow buyers to open disputes on merchant payments.
- **OO Objects:**
  - Entity: `DisputeCase`
  - Value Object: `DisputeStatus`
  - Service: `DisputeService`
- **Acceptance Criteria:**
  - Create dispute with reason and message.
  - Status transitions: `OPEN` → `UNDER_REVIEW` → `RESOLVED`
        (minimum).
  - Dispute updates trigger notifications.
  - Audit log records all changes.

------------------------------------------------------------------------

## Milestone 7 -- Events, Webhooks, and Integration Patterns (P1/P2)

### T-0701: Domain Events (In-Process) + Outbox Pattern

- **Priority:** P1\
- **Weight:** 6\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Emit reliable events for side effects (notifications,
    projections, webhooks).
- **OO Objects:**
  - Base: `DomainEvent`
  - Publisher: `EventBus`
  - Storage: `OutboxMessage`
- **Acceptance Criteria:**
  - Payments emit events (e.g., `PaymentCompleted`).
  - Outbox persists events in same DB transaction as ledger writes.
  - Background dispatcher publishes events to handlers.

### T-0702: Webhooks (HMAC Signing + Retry)

- **Priority:** P2\
- **Weight:** 6\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Allow merchants to receive signed event notifications.
- **OO Objects:**
  - Entity: `WebhookSubscription`
  - Service: `WebhookDispatcher`
  - Value Object: `WebhookSignature`
- **Acceptance Criteria:**
  - Merchant registers webhook URL + secret.
  - Events are POSTed with signature header.
  - Retry with backoff on non-2xx.
  - Webhook deliveries recorded for audit/debug.

------------------------------------------------------------------------

## Milestone 8 -- Risk & Abuse Controls (P2)

### T-0801: Rate Limiting & Anti-Abuse Policies

- **Priority:** P2\
- **Weight:** 4\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Prevent basic abuse and demonstrate security maturity.
- **OO Objects:**
  - Service: `RateLimitService`
  - Policy: `VelocityPolicy`
- **Acceptance Criteria:**
  - Rate limit login and payment creation endpoints.
  - Velocity rule: too many transfers in a window triggers
        hold/reject.

### T-0802: Risk Engine v1 (Allow/Hold/Reject)

- **Priority:** P2\
- **Weight:** 5\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Centralize risk decisions and keep them auditable.
- **OO Objects:**
  - Service: `RiskEngine`
  - Entity: `RiskDecisionRecord`
- **Acceptance Criteria:**
  - RiskEngine returns decision and reasons.
  - Decisions are stored and traceable to payment id.
  - Holds triggered via HoldService when needed.

------------------------------------------------------------------------

## Milestone 9 -- Admin & Reporting (P2)

### T-0901: Admin Read Views (Users, Payments, Ledger)

- **Priority:** P2\
- **Weight:** 5\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Provide an admin dashboard-like API for inspection.
- **OO Objects:**
  - Query Services + DTOs
- **Acceptance Criteria:**
  - Admin can list users and see statuses.
  - Admin can inspect a payment and its ledger entries.
  - Admin can search by ids, email, date range.

### T-0902: Export (CSV) for Transactions

- **Priority:** P3\
- **Weight:** 3\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Provide simple reporting export capability.
- **Acceptance Criteria:**
  - CSV export supports date range and filters.
  - Output includes columns for gross, fee, net, status, timestamps.

------------------------------------------------------------------------

## Milestone 10 -- Optional "Wow" Enhancements (P3)

### T-1001: Multi-Currency Support

- **Priority:** P3\
- **Weight:** 8\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Support multiple currencies per wallet and transaction
    constraints.
- **Acceptance Criteria:**
  - Wallet supports multiple ledger accounts (one per currency).
  - Cross-currency transfers are disallowed or use a mocked FX
        engine.

### T-1002: CQRS Read Model (Materialized Balances)

- **Priority:** P3\
- **Weight:** 6\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Faster balance queries via projections.
- **Acceptance Criteria:**
  - `BalanceProjection` table updated from events.
  - Projection is rebuildable from ledger history.

### T-1003: Event Sourcing Mode (Advanced)

- **Priority:** P3\
- **Weight:** 8\
- **Implemented:** To Do (ready for implementation)\\
- **Reference File:** N/A\\
- **PR URL:** N/A\\
 Replace parts of persistence with event streams for
    learning.
- **Acceptance Criteria:**
  - Payment aggregate reconstructed from event history.
  - Snapshots optional; deterministic replay supported.

------------------------------------------------------------------------

## Dependency Summary (High-Level)

- Identity (T-0101) + Money (T-0201) → Ledger (T-0202) → Wallet &
    Balance (T-0203/0204)\
- Ledger → Payment State Machine (T-0301) → Idempotency (T-0302) → P2P
    (T-0303) → Requests (T-0304)\
- Merchant (T-0401) → Orders (T-0402) → Approve/Capture (T-0403) →
    Refunds/Disputes (T-0601/0602)\
- Outbox/Events (T-0701) unlocks webhooks, better notifications,
    projections, and risk automation.

------------------------------------------------------------------------

## Suggested Execution Order (If You Want a Straight Line)

1. **T-0001 → T-0002 → T-0003**\
2. **T-0101 → T-0103 → T-0201 → T-0202 → T-0203 → T-0204**\
3. **T-0301 → T-0302 → T-0303 → T-0304**\
4. **T-0401 → T-0402 → T-0403**\
5. **T-0502 → T-0601**\
6. **T-0701 → (T-0501, T-0702) → (T-0503, T-0802)**\
7. **Admin & extras**
