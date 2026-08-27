-- =============================================
-- V5: Create idempotency records table
-- =============================================
-- Prevents duplicate transaction creation from network retries.
-- Uses INSERT ... ON CONFLICT in a REQUIRES_NEW transaction to
-- atomically claim the key (see IdempotencyService).

CREATE TABLE idempotency_records (
    id               BIGSERIAL    PRIMARY KEY,
    idempotency_key  VARCHAR(255) NOT NULL UNIQUE,
    user_id          BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PROCESSING',  -- PROCESSING, COMPLETED, FAILED
    transaction_id   BIGINT       REFERENCES transactions(id),
    response_body    TEXT,            -- Cached JSON response for replay
    response_status  INTEGER,        -- HTTP status code for replay
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_idempotency_user_id ON idempotency_records(user_id);
