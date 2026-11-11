# PulsePay - System Architecture Overview

## 1. Introduction

PulsePay is a simulated payment processing platform designed to
explore high-throughput transaction processing, financial correctness,
distributed systems and scalability.

The platform uses synthetic accounts and simulated payment providers.
It does not connect to real banking or card networks.

## 2. Architectural Goals

- Support high-volume concurrent payment operations.
- Prevent duplicate financial effects.
- Maintain accurate account balances.
- Guarantee balanced double-entry ledger postings.
- Provide reliable payment lifecycle management.
- Support independent service deployment and scaling.
- Handle failures and retries safely.
- Provide measurable performance and observability.

## 3. High-Level Architecture

Payment Service
       |
       | HTTP
       v
Financial Core
       |
       v
Financial PostgreSQL Database
       |
       +-- Account balances
       +-- Ledger transactions
       +-- Ledger entries
       +-- Financial outbox
       |
       v
Kafka
       |
       +-- Reporting
       +-- Alerts
       +-- Analytics

Gateway Service
       |
       +-- Provider routing
       +-- Provider attempts
       +-- Reconciliation

Gateway Service integrates with simulated external providers
as part of the merchant payment flow.

## 4. Core Services

### Payment Service

Responsible for:
- Payment creation and lifecycle management.
- API-level idempotency.
- Payment orchestration.
- Payment status tracking.
- Publishing payment-domain events.

### Financial Core

Responsible for:
- Account management.
- Available balance management.
- Financial operation idempotency.
- Double-entry ledger.
- Atomic financial posting.
- Financial outbox events.

### Gateway Service

Responsible for:
- Provider configuration.
- Provider selection and routing.
- Provider request attempts.
- Timeout and retry handling.
- Circuit breakers.
- Reconciliation.

## 5. Consistency Model

Strong consistency is required for critical financial operations.

The following changes must happen atomically:
- Financial operation registration.
- Account balance updates.
- Ledger transaction creation.
- Debit and credit entries.
- Financial outbox event creation.

Non-critical downstream consumers use asynchronous
event processing and eventual consistency.

## 6. Communication

Synchronous:
- HTTP between services for operations requiring
  an immediate response.

Asynchronous:
- Kafka for downstream event distribution.
- Transactional outbox for reliable event publication.

Kafka is not part of the synchronous financial posting transaction.

## 7. Technology Stack

- Java 21
- Spring Boot 3
- PostgreSQL 17
- Spring JdbcTemplate
- Flyway
- Maven
- Docker and Docker Compose
- Apache Kafka
- Redis where justified
- JUnit 5 and Testcontainers
- k6
- Micrometer and Prometheus
- Grafana
- OpenTelemetry

## 8. Performance Objectives

| Metric | Target |
|---|---:|
| Baseline throughput | 100 TPS |
| Sustained throughput | 1,000 TPS |
| Stretch throughput | 5,000 TPS |

Track:
- p50 latency
- p95 latency
- p99 latency
- Throughput
- Error rate
- Database contention

## 9. Financial Invariants

- Duplicate financial effects: 0
- Unbalanced ledger transactions: 0
- Negative balances: 0

Correctness takes priority over performance optimisations.