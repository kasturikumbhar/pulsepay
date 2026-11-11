# ADR-0001: Service Architecture

## Status
Accepted

## Context

PulsePay is an educational payment platform designed to explore
high-throughput payment processing, financial correctness,
distributed systems and horizontal scalability.

The platform needs clear service responsibilities and independent
deployment boundaries.

## Decision

Adopt a microservices architecture with three core services:

### Payment Service
Owns:
- Payment lifecycle and orchestration
- Payment records
- API-level idempotency
- Payment status management

### Financial Core
Owns:
- Customer accounts
- Available balances
- Financial operations
- Double-entry ledger
- Financial posting idempotency

### Gateway Service
Owns:
- Provider integrations
- Provider routing
- Provider attempts
- Reconciliation

Services communicate through synchronous HTTP APIs
and asynchronous events where appropriate.

## Initial Implementation Strategy

Use a Maven multi-module monorepo.

Each service will be independently buildable and deployable.

The initial implementation may prioritise Financial Core
before implementing the other services.

## Consequences

Positive:
- Clear ownership and responsibilities
- Independent deployment and scaling
- Better isolation of business capabilities

Trade-offs:
- Network failures and distributed latency
- More complex debugging and observability
- Distributed consistency challenges
- Additional infrastructure requirements

## Future Considerations

Introduce distributed tracing, resilience patterns,
centralised observability and independent deployment pipelines.