-- =============================================
-- V3: Create transactions table
-- =============================================

CREATE TABLE transactions (
    id               BIGSERIAL     PRIMARY KEY,
    user_id          BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    description      VARCHAR(500)  NOT NULL,
    amount           NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    category         VARCHAR(100),                          -- nullable until AI auto-categorization (Week 5)
    idempotency_key  VARCHAR(255)  UNIQUE,                  -- used in Week 3 for idempotent writes
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
