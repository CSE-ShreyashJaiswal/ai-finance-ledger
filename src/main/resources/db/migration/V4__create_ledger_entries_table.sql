-- =============================================
-- V4: Create ledger entries table (double-entry core)
-- =============================================
-- Every transaction produces at least two ledger entries
-- whose debits and credits balance to zero.
-- This is the heart of the double-entry system.

CREATE TABLE ledger_entries (
    id              BIGSERIAL     PRIMARY KEY,
    transaction_id  BIGINT        NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    account_id      BIGINT        NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    entry_type      VARCHAR(10)   NOT NULL,                -- DEBIT or CREDIT
    amount          NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ledger_entries_transaction_id ON ledger_entries(transaction_id);
CREATE INDEX idx_ledger_entries_account_id ON ledger_entries(account_id);
