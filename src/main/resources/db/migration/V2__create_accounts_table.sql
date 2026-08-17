-- =============================================
-- V2: Create accounts table
-- =============================================

CREATE TABLE accounts (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name       VARCHAR(255) NOT NULL,
    type       VARCHAR(50)  NOT NULL,   -- ASSET or LIABILITY
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Index for fast lookup of all accounts belonging to a user
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
