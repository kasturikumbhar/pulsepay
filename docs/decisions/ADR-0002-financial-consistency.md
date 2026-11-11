# ADR-0002: Financial Consistency and Concurrency

## Status
Accepted

## Context

Financial operations must preserve correctness under
concurrent requests, retries and failures.

A payment may be delayed or retried, but it must never
silently produce an incorrect financial result.

## Decision

Use strong transactional consistency for critical
financial operations.

Financial Core will use PostgreSQL transactions to
atomically perform:

- Financial operation registration
- Account balance updates
- Ledger transaction creation
- Debit and credit entry creation
- Financial operation completion
- Financial outbox event creation

All these changes must commit or roll back together.

## Idempotency

Implement idempotency at two boundaries:

1. Payment Service prevents duplicate API payment creation.
2. Financial Core prevents duplicate financial postings.

Use durable operation identifiers, request hashes and
database uniqueness constraints.

The same operation identifier with a different request
must be rejected.

## Ledger Integrity

- Ledger entries are immutable.
- Reversals use compensating entries.
- Debit and credit totals must balance.
- Account balances must never become negative.
- Money is represented using integer minor units.

## Initial Concurrency Strategy

Use PostgreSQL pessimistic row-level locking
(SELECT ... FOR UPDATE) for wallet transfers.

Acquire account locks in deterministic account-ID order
to reduce deadlock risk.

Keep transactions short and avoid external calls while
account locks are held.

## Alternatives Considered

### Optimistic Locking

Uses version checks to detect concurrent modifications.

Advantages:
- Can perform well under low contention.
- Avoids waiting for a lock before attempting an update.

Trade-offs:
- Conflicts require retries.
- High contention can produce retry storms.
- Multi-account operations require careful coordination.

### Decision Rationale

Pessimistic locking provides a straightforward initial
correctness model for transfers involving two accounts.

This is an initial implementation choice, not a claim
that pessimistic locking is always faster.

Both approaches must be benchmarked under representative
workloads before making production performance decisions.

## Asynchronous Processing

Use eventual consistency for non-critical downstream
consumers such as reporting, alerts and analytics.

Use a transactional outbox to reliably publish events
after the financial database transaction commits.

Kafka is not part of the synchronous financial posting
transaction.

## Consequences

Financial correctness takes priority over throughput.

Performance optimisations must preserve all financial
invariants.