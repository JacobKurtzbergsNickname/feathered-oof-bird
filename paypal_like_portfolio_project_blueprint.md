# PayPal-Like Portfolio Project -- Feature & Architecture Blueprint

## 1. Executive Overview

This document outlines the core features, architectural patterns, domain
modeling decisions, and implementation guardrails for building a
PayPal-like payment platform as a portfolio project.

The goal is not to replicate real-world banking infrastructure, but to
demonstrate:

-   Strong domain modeling
-   Financial correctness principles
-   Secure coding practices
-   Event-driven architecture
-   Idempotent APIs
-   Clean separation of concerns
-   Auditability and traceability

------------------------------------------------------------------------

# 2. Core Functional Domains

## 2.1 Identity & Accounts

### Functional Scope

-   User registration & login
-   Email verification
-   Optional MFA
-   Profile management
-   Personal vs Business account types
-   Account status flags (active, restricted, suspended)

### Data Model

User: - id (UUID) - email - password_hash - role (PERSONAL \| BUSINESS
\| ADMIN) - status (ACTIVE \| RESTRICTED \| SUSPENDED) - created_at -
updated_at

Account: - id (UUID) - user_id (FK) - type (WALLET \| FEE \| CLEARING) -
currency - created_at

### Engineering Considerations

-   Separate identity from financial accounts
-   Store password hashes using Argon2 or bcrypt
-   Use JWT or session cookies (httpOnly + secure)
-   Add audit logging for profile changes
-   Add rate limiting to login endpoints

------------------------------------------------------------------------

## 2.2 Ledger System (The Core of Financial Integrity)

This is the most important subsystem.

Never store balance as a mutable number.

### Principles

-   Double-entry bookkeeping
-   Append-only ledger entries
-   Balances are projections
-   Idempotent write operations

### Ledger Tables

LedgerAccount: - id - owner_id - account_type - currency

LedgerEntry: - id - ledger_account_id - transaction_id - amount_minor
(integer) - currency - created_at

Transaction: - id - type (P2P, MERCHANT, REFUND, FEE) - status (CREATED,
AUTHORIZED, CAPTURED, COMPLETED, FAILED, REVERSED) - idempotency_key -
metadata_json - created_at

### Critical Rules

-   Sum of entries per transaction must equal zero.
-   All money is stored as integers in minor units (e.g., cents).
-   Currency must always be explicitly stored.

### Why This Matters

This design demonstrates that you understand: - Financial correctness -
Race conditions - Data integrity under concurrency

------------------------------------------------------------------------

## 2.3 Wallet & Balance Projection

Balances are calculated as:

SUM(ledger_entries.amount_minor) GROUP BY ledger_account_id

Optionally maintain a read-model projection for performance.

Balance Types: - available - pending - held

Projection can be updated via event subscribers.

------------------------------------------------------------------------

## 2.4 Funding Sources

Simulated for portfolio.

FundingSource: - id - user_id - type (BANK \| CARD) - last4 - brand -
token (simulated) - verified

Provider Interface Example:

interface PaymentProvider { authorize(amount, currency)
capture(transaction_id) refund(transaction_id) }

Implement: - FakeBankProvider - FakeCardProvider

This abstraction shows strong architectural design.

------------------------------------------------------------------------

## 2.5 P2P Payments

Flow: 1. Create payment 2. Authorize 3. Capture 4. Complete

Edge Cases: - Recipient not registered - Insufficient funds - Duplicate
requests

Use a state machine for transitions.

State Machine: CREATED → AUTHORIZED → CAPTURED → COMPLETED\
CREATED → FAILED\
CAPTURED → REFUNDED

------------------------------------------------------------------------

## 2.6 Merchant Checkout

API Design:

POST /api/orders\
POST /api/orders/{id}/approve\
POST /api/orders/{id}/capture

Order: - id - merchant_id - total_minor - currency - status

Add Webhooks: - order.approved - payment.captured - payment.failed

Sign webhooks using HMAC signatures.

------------------------------------------------------------------------

## 2.7 Fees & Holds

FeePolicy: - percentage - flat_fee - region - payment_type

Fee calculation should be deterministic and pure.

Holds: - Transfer to HOLD ledger account - Release via background job

------------------------------------------------------------------------

## 2.8 Refunds & Disputes

Refund: - Create reversing ledger entries - Reference original
transaction

Dispute: - id - transaction_id - status - messages -
evidence_storage_path

Demonstrates: - Reversibility - Audit trail - State transitions

------------------------------------------------------------------------

## 2.9 Notifications

Event-driven notifications.

PaymentCompleted → send receipt\
RefundIssued → notify user\
DisputeCreated → notify merchant

Use a NotificationService abstraction.

------------------------------------------------------------------------

## 2.10 Risk Engine

Simple heuristic checks: - Velocity limits - Amount thresholds - Geo
inconsistencies (mocked)

RiskDecision: - ALLOW - HOLD - REJECT

All decisions logged for auditability.

------------------------------------------------------------------------

# 3. Cross-Cutting Engineering Concerns

## Idempotency

Require Idempotency-Key header for write operations.

Store: - idempotency_key - request_hash - response_snapshot

If duplicate request detected → return stored response.

------------------------------------------------------------------------

## Concurrency

Use: - DB transactions - Row-level locking - Optimistic concurrency
control

Ledger writes must be atomic.

------------------------------------------------------------------------

## Audit Logging

AuditLog: - actor_id - action - entity_type - entity_id - timestamp -
metadata

Never mutate audit logs.

------------------------------------------------------------------------

## Security

-   HTTPS only
-   CSRF protection
-   XSS protection
-   Input validation
-   Rate limiting
-   Strict CORS policy

------------------------------------------------------------------------

# 4. Suggested Architecture

Monolith (Modular):

/modules /identity /ledger /payments /notifications /risk /admin

OR

Microservices:

-   Identity Service
-   Ledger Service
-   Payment Service
-   Notification Service
-   Admin/Reporting Service

Communication: - REST internally - Event bus for async flows

------------------------------------------------------------------------

# 5. Minimal Viable Portfolio Scope

1.  User registration & wallet creation
2.  Double-entry ledger
3.  P2P payments
4.  Merchant checkout flow
5.  Refund support
6.  Webhooks
7.  Basic admin dashboard

------------------------------------------------------------------------

# 6. Optional Advanced Enhancements

-   Multi-currency support
-   Scheduled payments
-   Subscription billing
-   Fraud scoring engine
-   Event sourcing
-   CQRS separation
-   Redis caching for balance projection
-   Kafka/RabbitMQ for event transport

------------------------------------------------------------------------

# 7. What This Project Demonstrates

-   Domain-driven design principles
-   Financial data integrity awareness
-   State machine modeling
-   Event-driven architecture
-   Secure API design
-   Production-ready backend patterns
-   Clean separation of business logic and infrastructure

------------------------------------------------------------------------

# 8. Conclusion

A PayPal-like system is not about payment gateways. It is about:

-   Ledger correctness
-   Idempotency
-   Auditability
-   Clear domain boundaries
-   Secure and reliable state transitions

If implemented correctly, this project strongly signals senior-level
backend maturity.
