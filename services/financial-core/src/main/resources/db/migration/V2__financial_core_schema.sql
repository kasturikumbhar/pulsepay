-- ==========================================
-- ACCOUNTS
-- ==========================================

CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    owner_id UUID NOT NULL,

    currency CHAR(3) NOT NULL,

    available_balance BIGINT NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL
        CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED')),

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_non_negative_balance
        CHECK (available_balance >= 0)
);


-- ==========================================
-- FINANCIAL OPERATIONS
-- ==========================================

CREATE TABLE financial_operations (
    operation_id UUID PRIMARY KEY,

    request_hash VARCHAR(64) NOT NULL,

    source_account_id UUID NOT NULL
        REFERENCES accounts(id),

    destination_account_id UUID NOT NULL
        REFERENCES accounts(id),

    amount BIGINT NOT NULL
        CHECK (amount > 0),

    currency CHAR(3) NOT NULL,

    status VARCHAR(20) NOT NULL
        CHECK (status IN ('SUCCEEDED', 'DECLINED')),

    result_code VARCHAR(50),

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    completed_at TIMESTAMPTZ,

    CONSTRAINT chk_different_accounts
        CHECK (source_account_id <> destination_account_id)
);


-- ==========================================
-- LEDGER TRANSACTIONS
-- ==========================================

CREATE TABLE ledger_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    operation_id UUID NOT NULL UNIQUE
        REFERENCES financial_operations(operation_id),

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- ==========================================
-- LEDGER ENTRIES
-- ==========================================

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


-- ==========================================
-- INDEXES
-- ==========================================

CREATE INDEX idx_accounts_owner
    ON accounts(owner_id);

CREATE INDEX idx_financial_operations_source
    ON financial_operations(source_account_id, created_at DESC);

CREATE INDEX idx_ledger_entries_account
    ON ledger_entries(account_id, created_at DESC);

CREATE INDEX idx_ledger_entries_transaction
    ON ledger_entries(ledger_transaction_id);