# PulsePay - Payment Flows

## 1. Internal Wallet Transfer

### Objective

Transfer funds between two PulsePay customer accounts.

### Flow

1. Customer submits a transfer request.
2. Payment Service receives the request.
3. Payment Service validates the request and checks
   its API idempotency key.
4. Payment Service creates or retrieves the payment record.
5. Payment Service calls Financial Core through HTTP.
6. Financial Core validates the financial operation.
7. Financial Core checks financial idempotency.
8. Financial Core locks the required accounts.
9. Financial Core validates account status, currency
   and available balance.
10. Financial Core updates both account balances.
11. Financial Core writes balanced debit and credit entries.
12. Financial Core writes an outbox event.
13. All financial changes commit atomically.
14. Payment Service updates its payment status.
15. Financial events are published asynchronously through Kafka.

## 2. Merchant Payment Flow

### Objective

Allow a merchant to submit a payment request.

### Flow

1. Merchant submits a payment request.
2. Payment Service validates and registers the request.
3. Payment Service checks API idempotency.
4. Payment Service selects the payment processing path.
5. Gateway Service selects an appropriate provider.
6. Gateway Service sends a request to a simulated provider.
7. Provider returns an approval, decline or ambiguous result.
8. Payment Service manages the payment lifecycle.
9. Financial Core performs the appropriate financial posting
   when the operation is ready to be posted.
10. Payment and financial events are published asynchronously.

## 3. Failure Handling

### Duplicate Requests

Repeated requests with the same idempotency key must
not create duplicate payments or financial postings.

### Financial Failure

A failed financial transaction must not leave
partially updated balances or ledger entries.

### Provider Timeout

A timeout does not necessarily mean the provider declined
the payment.

The system must support pending outcomes, provider
status inquiries and reconciliation.

Blindly retrying against another provider may create
duplicate external payment effects.

### Event Publication Failure

Financial database changes and the outbox event are
committed atomically.

If Kafka is unavailable, the outbox publisher retries
without repeating the financial posting.

## 4. Payment Lifecycle

Initial payment states:

- CREATED
- PROCESSING
- APPROVED
- DECLINED
- PENDING
- CANCELLED

Transitions must be validated and illegal transitions
must be rejected.

## 5. Important Design Principle

A payment may be delayed, retried or temporarily unavailable.

It must never silently produce an incorrect financial result.