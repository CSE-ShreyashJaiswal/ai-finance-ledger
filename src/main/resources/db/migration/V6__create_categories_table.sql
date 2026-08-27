-- =============================================
-- V6: Create categories table + seed defaults
-- =============================================

CREATE TABLE categories (
    id    BIGSERIAL    PRIMARY KEY,
    name  VARCHAR(100) NOT NULL UNIQUE,
    type  VARCHAR(50)  NOT NULL    -- EXPENSE, INCOME, TRANSFER
);

-- Seed default categories (used by AI auto-categorization in Week 5)
INSERT INTO categories (name, type) VALUES
    ('FOOD',          'EXPENSE'),
    ('GROCERIES',     'EXPENSE'),
    ('TRANSPORT',     'EXPENSE'),
    ('FUEL',          'EXPENSE'),
    ('UTILITIES',     'EXPENSE'),
    ('RENT',          'EXPENSE'),
    ('ENTERTAINMENT', 'EXPENSE'),
    ('SHOPPING',      'EXPENSE'),
    ('HEALTHCARE',    'EXPENSE'),
    ('EDUCATION',     'EXPENSE'),
    ('INSURANCE',     'EXPENSE'),
    ('SUBSCRIPTIONS', 'EXPENSE'),
    ('TRAVEL',        'EXPENSE'),
    ('PERSONAL_CARE', 'EXPENSE'),
    ('GIFTS',         'EXPENSE'),
    ('CHARITY',       'EXPENSE'),
    ('MISCELLANEOUS', 'EXPENSE'),
    ('SALARY',        'INCOME'),
    ('FREELANCE',     'INCOME'),
    ('INVESTMENT',    'INCOME'),
    ('REFUND',        'INCOME'),
    ('INTEREST',      'INCOME'),
    ('TRANSFER',      'TRANSFER');
