# Ticket Refinement Rationale

This document explains why the backlog was expanded and restructured after
reviewing implemented code, deferred scope, and current frontend coverage.

## Why This Refinement Was Needed

The original backlog had three gaps:

1. Some implementation-critical problems were not represented as explicit tickets.
2. Deferred scope existed only as high-level bullets, not actionable work items.
3. Implemented backend capabilities did not have corresponding frontend scaffolding tickets.

The resulting changes convert these gaps into concrete, estimable, and
sequenced tickets.

## 1) Added Tickets for Existing Problems

### A. Split registration and login into separate tickets

- Added/refined: `T-0101` (registration only), `T-0104` (login/JWT issuance)
- Reasoning:
  - Registration and authentication are distinct delivery units with different
    risks (credential storage vs token issuance and abuse controls).
  - Current codebase has JWT validation strategies but no user-facing login
    endpoint. Explicitly tracking login prevents it from being implied and missed.
  - Independent ticketing makes dependencies clearer for UI work (`T-UI02`).

### B. Money model consistency across persistence

- Added: `T-0205`
- Reasoning:
  - Current transaction persistence uses `BigDecimal`, while the blueprint and
    ledger rules require integer minor units.
  - This mismatch is a data-integrity and reconciliation risk.
  - A dedicated migration ticket ensures schema, mappers, and tests are updated
    before ledger-heavy features expand.

### C. Append-only financial immutability

- Added: `T-0306`
- Reasoning:
  - Existing delete behavior for transactions conflicts with append-only/audit
    requirements in financial systems.
  - This must be resolved before payments and ledger features are considered
    correct.
  - Ticket defines acceptable alternatives (remove delete, admin-only in
    non-prod, audit logging).

## 2) Refined Deferred Scope into Actionable Tickets

The old blueprint "Optional Advanced Enhancements" section was intentionally
removed and converted into explicit backlog tickets under Milestone 10.

### Converted and expanded deferred scope

- `T-1001` Multi-currency wallet support
- `T-1002` CQRS materialized balance projection
- `T-1003` Event sourcing for payment aggregate
- `T-1004` Scheduled and recurring payments
- `T-1005` Redis balance cache layer
- `T-1006` Async event bus infrastructure (Kafka/RabbitMQ)
- `T-1007` Subscription billing

### Why these are better than the previous bullets

- Each item now has:
  - concrete acceptance criteria
  - dependency wiring
  - bounded scope with implementation intent
  - explicit OO artifacts (entities/services/ports)
- This turns "ideas" into plan-ready work that can be estimated and sequenced.

## 3) Frontend Scaffolding Tickets for Implemented Backend Features

A new Milestone UI was added to align frontend progress with backend reality.

### What was detected in implemented backend

- Transaction CRUD endpoints are implemented.
- Admin status endpoint exists and is authorization-protected.
- Auth infrastructure exists (JWT strategies, dev auth provider), but login/register
  API and UI are not complete.

### Frontend scaffolding tickets added

- `T-UI01` Transaction management UI (marked `Done` as already implemented)
- `T-UI02` Authentication UI scaffold (login + register)
- `T-UI03` Admin panel scaffold
- `T-UI04` Wallet balance and transaction history view
- `T-UI05` P2P send money flow

These tickets prevent backend-only delivery and ensure feature slices are
user-visible.

## 4) Why Deferred Scope Was Removed from Blueprint

The blueprint is now kept as architecture and product intent, while execution
details live in the ticket backlog.

Reasons:

1. Avoid duplicate planning sources that can drift.
2. Keep deferred/advanced work tied to acceptance criteria and dependencies.
3. Preserve a single operational backlog (`paypal_like_portfolio_project_tickets.md`).

## 5) Outcome

The backlog now has:

- explicit tickets for known correctness/security gaps
- a fully ticketed advanced/deferred scope
- frontend scaffolding tickets aligned to backend delivery
- clearer dependency flow from foundational work to advanced capabilities

This should reduce ambiguity during implementation and make progress tracking
much more reliable.
