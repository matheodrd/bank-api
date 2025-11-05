CREATE TABLE accounts
(
    id             UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    account_number VARCHAR(34)    NOT NULL UNIQUE,
    account_holder VARCHAR(100)   NOT NULL,
    balance        DECIMAL(19, 4) NOT NULL DEFAULT 0.0,
    currency       VARCHAR(3)     NOT NULL,
    status         VARCHAR(20)    NOT NULL,
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE accounts IS 'Bank Accounts';
COMMENT ON COLUMN accounts.account_number IS 'Unique account number (IBAN format)';
COMMENT ON COLUMN accounts.balance IS 'Account balance with 4 decimal places';
COMMENT ON COLUMN accounts.currency IS 'ISO 4217 currency codes (e.g. GBP, EUR, USD)';
COMMENT ON COLUMN accounts.status IS 'Account status (e.g. ACTIVE, CLOSED, etc.)';
