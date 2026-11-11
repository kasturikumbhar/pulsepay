# PulsePay

**A high-throughput, distributed payment processing platform focused on financial correctness, idempotency and scalability.**

PulsePay is an educational payment platform built to explore the engineering challenges behind modern payment systems.

It simulates customer wallet transfers and merchant payment processing using synthetic accounts and simulated payment providers.

The project focuses on building a reliable financial system that can handle concurrent transactions, duplicate requests, failures and increasing workloads without compromising financial correctness.

> **Core principle:** A payment may be delayed, retried or temporarily unavailable, but it must never silently produce an incorrect financial result.

---

## 1. Project Goals

- Build a distributed payment processing platform.
- Support concurrent financial operations.
- Prevent duplicate payment creation and financial postings.
- Maintain accurate account balances.
- Implement double-entry ledger accounting.
- Explore reliable asynchronous event processing.
- Handle provider failures, timeouts and reconciliation.
- Measure and optimise throughput and latency.

## 2. Architecture

PulsePay is designed around three independently deployable services.

| Service | Responsibility |
|---|---|
| Payment Service | Payment lifecycle, orchestration and API idempotency |
| Financial Core | Accounts, balances, ledger and financial correctness |
| Gateway Service | Provider routing, attempts and reconciliation |

### High-Level Architecture

```text
                 Customer / Merchant
                         |
                         v
                  Payment Service
                         |
                       HTTP
                         |
                         v
                   Financial Core
                         |
                         v
                 Financial PostgreSQL
                    /           \
               Balances        Ledger
                    \           /
                     Transaction
                          |
                       Outbox
                          |
                          v
                        Kafka
                     /    |    \
                    v     v     v
                Reporting Alerts Analytics


                  Gateway Service
                         |
                         v
                 Simulated Providers
```

Kafka is used for asynchronous downstream processing. It is not part of the synchronous financial posting transaction.

## 3. Financial Correctness

Financial correctness is the most important design requirement.

PulsePay follows these principles:

- Money is represented using integer minor units (paise).
- Financial operations use database transactions.
- Account balances cannot become negative.
- Every posted transfer produces balanced debit and credit entries.
- Ledger entries are immutable.
- Reversals use compensating entries.
- Idempotency is enforced at both the API and financial posting boundaries.
- Financial balance updates, ledger entries and outbox records commit atomically.

### Idempotency

Two independent idempotency boundaries are planned:

1. Payment Service prevents duplicate API payment creation.
2. Financial Core prevents duplicate financial postings.

### Concurrency Control

The initial Financial Core implementation uses PostgreSQL pessimistic row-level locking (`SELECT ... FOR UPDATE`) with deterministic account lock ordering.

Optimistic locking will also be evaluated through performance benchmarks.

## 4. Database Ownership

Each service owns its database.

| Database | Owner | Tables |
|---|---|---|
| Payments DB | Payment Service | payments, idempotency_keys, outbox_events |
| Financial DB | Financial Core | accounts, financial_operations, ledger_transactions, ledger_entries, financial_outbox_events |
| Gateway DB | Gateway Service | providers, provider_attempts, reconciliation_records |

Services must not directly query another service's database. Communication happens through APIs or events.

## 5. Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Application development |
| Spring Boot 3 | Application framework |
| PostgreSQL 17 | Relational database |
| Spring JdbcTemplate | Explicit SQL-based database access |
| Flyway | Database migrations |
| Maven | Build and dependency management |
| Docker / Docker Compose | Local infrastructure |
| Apache Kafka | Asynchronous event processing |
| JUnit 5 | Automated testing |
| Testcontainers | Database integration testing |
| k6 | Load testing |
| Micrometer / Prometheus | Metrics |
| Grafana | Monitoring |
| OpenTelemetry | Distributed tracing |

Technologies will be introduced progressively as the architecture evolves.

## 6. Performance Objectives

| Metric | Target |
|---|---:|
| Baseline throughput | 100 TPS |
| Sustained throughput | 1,000 TPS |
| Stretch throughput | 5,000 TPS |

Performance will be evaluated using:

- p50 latency
- p95 latency
- p99 latency
- Throughput
- Error rate
- Database contention

### Correctness Targets

| Invariant | Target |
|---|---:|
| Duplicate financial effects | 0 |
| Unbalanced ledger transactions | 0 |
| Negative balances | 0 |

Performance improvements must never compromise these invariants.

## 7. Project Structure

The intended repository structure is:

```text
pulsepay/
├── services/
│   ├── payment-service/
│   ├── financial-core/
│   └── gateway-service/
│
├── infrastructure/
│   ├── docker/
│   └── kafka/
│
├── docs/
│   ├── architecture/
│   │   ├── system-overview.md
│   │   ├── service-boundaries.md
│   │   └── payment-flows.md
│   │
│   └── decisions/
│       ├── ADR-0001-service-architecture.md
│       ├── ADR-0002-financial-consistency.md
│       ├── ADR-0003-data-ownership.md
│       └── ADR-0004-database-access-strategy.md
│
├── docker-compose.yml
├── pom.xml
└── README.md
```

This is the target structure. The current implementation is being progressively migrated into this structure.

## 8. Current Implementation

### Implemented

- Spring Boot application foundation.
- PostgreSQL integration using Docker.
- Flyway database migrations.
- Account creation and retrieval APIs.
- Internal wallet transfer API.
- Financial operation idempotency.
- Atomic balance updates.
- Double-entry ledger posting.
- Insufficient balance validation.
- Automated PostgreSQL integration tests using Testcontainers.

### Planned

- Maven multi-module restructuring.
- Independent Payment Service.
- Payment API idempotency.
- Independent service databases.
- Transactional outbox.
- Kafka event processing.
- Gateway Service.
- Simulated provider integrations.
- Retry and circuit breaker mechanisms.
- Reconciliation.
- Load testing and observability.

## 9. Running Locally

### Prerequisites

- Java 21
- Docker Desktop
- Git

### Start PostgreSQL

```bash
docker compose up -d
```

### Build the application

```bash
./mvnw clean package
```

### Run automated tests

```bash
./mvnw clean test
```

### Start Spring Boot

```bash
./mvnw spring-boot:run
```

The application is configured to use:

```text
jdbc:postgresql://localhost:5432/pulsepay
```

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

## 10. API Overview

Initial APIs:

| Method | Endpoint | Purpose |
|---|---|---|
| POST | /api/v1/accounts | Create an account |
| GET | /api/v1/accounts/{accountId} | Retrieve an account |
| POST | /api/v1/transfers | Transfer funds between accounts |

Transfers require an `Idempotency-Key` request header.

Amounts are specified in minor currency units. For INR, `50000` represents ₹500.

## 11. Architecture Decision Records

Key architectural decisions are documented in `docs/decisions/`.

- ADR-0001: Service Architecture
- ADR-0002: Financial Consistency
- ADR-0003: Database Ownership
- ADR-0004: Database Access Strategy

These documents explain the reasoning and trade-offs behind the architecture.

## 12. Project Philosophy

PulsePay is being developed incrementally, with an emphasis on understanding the engineering decisions behind payment systems.

The goal is not simply to add technologies such as Kafka, Redis or microservices.

The goal is to understand when they are useful, what problems they solve, what trade-offs they introduce and how to validate their impact.

**Correctness first. Performance through measurement. Scalability through deliberate design.**

---

*PulsePay is a simulated educational project. It does not process real payments or connect to real financial networks.*