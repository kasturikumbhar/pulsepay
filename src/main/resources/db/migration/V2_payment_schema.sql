CREATE TABLE merchants(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK(status IN  ('ACTIVE', 'INACTIVE')
    create_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE TABLE accounts(
    id UUID PRIMARY KEY DEFAULT GEN_RANDOM_UUID(),
    owner_id UUID NOT Null,
    currency CHAR(3) NOT NULL,
    available_balance BIGINT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL CHECK(status IN ('ACTIVE', 'FROZEN', 'CLOSED')),
    create_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT check_balance_non_negative CHECK(available_balance >= 0)
);

CREATE TABLE payments(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL References merchants(id),
    source_account_id UUID NOT NULL References accounts(id),
    destination_account_id UUID NOT NULL References accounts(id),
    amount BIGINT NOT NULL CHECK(amount > 0),
    currency CHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK(status IN ('CREATED', 'PROCESSING', 'APPROVED', 'DECLINED', 'CANCELLED', 'PENDING')), 
    create_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT check_source_destination_accounts CHECK(source_account_id <> destination_account_id) 
);

CREATE TABLE ledger_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL
        REFERENCES payments(id),
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('POSTED', 'REVERSED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ledger_transaction_id UUID NOT NULL
        REFERENCES ledger_transactions(id),
    account_id UUID NOT NULL
        REFERENCES accounts(id),
    entry_type VARCHAR(10) NOT NULL
        CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    amount BIGINT NOT NULL
        CHECK (amount > 0),
    currency CHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE idempotency_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    merchant_id UUID NOT NULL
        REFERENCES merchants(id),

    idempotency_key VARCHAR(255) NOT NULL,

    request_hash VARCHAR(64) NOT NULL,

    payment_id UUID
        REFERENCES payments(id),

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_merchant_idempotency
        UNIQUE (merchant_id, idempotency_key)
);

CREATE INDEX idx_payments_merchant_created
ON payments (merchant_id, created_at DESC);

CREATE INDEX idx_payments_source_account
ON payments (source_account_id);

CREATE INDEX idx_ledger_account_created
ON ledger_entries (account_id, created_at DESC);

CREATE INDEX idx_ledger_transaction
ON ledger_entries (ledger_transaction_id);