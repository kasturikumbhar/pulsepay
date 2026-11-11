# ADR-0003: Database Ownership

## Status
Accepted

## Context

PulsePay consists of independently deployable services.

Direct database access across service boundaries would
create tight coupling and undermine independent ownership.

## Decision

Each service owns its database and is responsible for
its schema, migrations and data integrity.

### Payments DB

Owner: Payment Service

Tables:
- payments
- idempotency_keys
- outbox_events

### Financial DB

Owner: Financial Core

Tables:
- accounts
- financial_operations
- ledger_transactions
- ledger_entries
- financial_outbox_events

### Gateway DB

Owner: Gateway Service

Tables:
- providers
- provider_attempts
- reconciliation_records

## Data Access Rules

- A service accesses its own database directly.
- Other services access its data through APIs or events.
- Services must not query another service's database directly.
- Each service owns its Flyway migrations.
- Database schemas evolve independently.

## Outbox Ownership

An outbox record must be stored in the same database
transaction as the business data that produced the event.

Financial Core owns financial_outbox_events because
financial events must be committed atomically with
financial postings.

Payment Service owns outbox_events for payment-domain
events committed with payment records.

## Local Development

The initial local environment may use one PostgreSQL
container with separate logical databases.

This does not provide complete physical resource isolation.

## Consequences

Positive:
- Clear data ownership
- Reduced service coupling
- Independent schema evolution

Trade-offs:
- Cross-service queries require APIs or events.
- Reporting requires read models or data pipelines.
- Distributed workflows require explicit coordination.