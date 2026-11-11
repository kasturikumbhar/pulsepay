# ADR-0004: Database Access Strategy

## Status
Accepted

## Context

PulsePay requires predictable database behaviour, strong financial
consistency, concurrency control and high throughput.

Financial Core performs critical operations involving account locking,
balance updates, ledger posting and idempotency.

## Decision

Use Spring JdbcTemplate with explicit SQL for Financial Core.

This provides direct control over:
- SQL queries and execution plans
- PostgreSQL row-level locking
- Transaction boundaries
- Atomic balance and ledger updates
- Database performance optimisation

JPA/Hibernate is not prohibited. Its use in other services can be
evaluated independently based on their requirements.

## Consequences

Positive:
- Explicit SQL and database behaviour
- Easier visibility into critical queries
- Direct use of PostgreSQL-specific capabilities

Trade-offs:
- More SQL and row-mapping code
- Relationships and persistence behaviour must be managed explicitly
- Developers need stronger SQL knowledge

## Future Considerations

Evaluate alternative persistence approaches using benchmarks and
actual service requirements.

Any change must preserve financial correctness and be supported
by measurable evidence.