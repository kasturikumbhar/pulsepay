# PulsePay - Service Boundaries and Data Ownership

## 1. Overview

PulsePay consists of three independently deployable services.

Each service owns its business logic and database.

Services must not directly query another service's database.

## 2. Payment Service

### Responsibilities
- Payment lifecycle.
- Payment creation.
- API idempotency.
- Payment orchestration.
- Payment status management.

### Database: Payments DB

Tables:
- payments
- idempotency_keys
- outbox_events

### Communication
- HTTP to Financial Core.
- HTTP to Gateway Service where required.
- Kafka for asynchronous payment events.

## 3. Financial Core

### Responsibilities
- Account management.
- Available balances.
- Financial operations.
- Financial idempotency.
- Double-entry ledger.
- Atomic financial posting.

### Database: Financial DB

Tables:
- accounts
- financial_operations
- ledger_transactions
- ledger_entries
- financial_outbox_events

### Communication
- HTTP requests from Payment Service.
- Kafka publication through transactional outbox.

## 4. Gateway Service

### Responsibilities
- Provider management.
- Provider selection.
- Routing.
- Provider attempts.
- Timeout and retry management.
- Circuit breakers.
- Reconciliation.

### Database: Gateway DB

Tables:
- providers
- provider_attempts
- reconciliation_records

### Communication
- HTTP orchestration with Payment Service.
- HTTP integrations with simulated providers.
- Kafka events where asynchronous processing is needed.

## 5. Database Ownership Rules

1. Every service owns its database.
2. Only the owning service can directly access its tables.
3. Cross-service data access must happen through APIs or events.
4. Every service owns its database migrations.
5. Each service independently manages its schema evolution.

## 6. Transactional Boundaries

Financial Core must commit account balance changes,
ledger entries and its financial outbox record in
one PostgreSQL transaction.

Payment Service independently manages its payment
records and payment-domain outbox events.

There is no distributed database transaction across services.

## 7. Local Development

Initially, all logical databases may run inside
one PostgreSQL container.

This is a local development convenience and does not
provide production-level physical isolation.