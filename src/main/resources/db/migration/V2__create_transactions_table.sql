CREATE TABLE transactions
(
    id          UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    account_id  UUID           NOT NULL,
    amount      DECIMAL(19, 4) NOT NULL,
    currency    VARCHAR(3)     NOT NULL,
    type        VARCHAR(10)    NOT NULL,
    category    VARCHAR(20)    NOT NULL,
    description VARCHAR(500),
    status      VARCHAR(20)    NOT NULL,
    risk_score  INTEGER        NOT NULL,
    timestamp   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE CASCADE
);

COMMENT ON TABLE transactions IS 'Bank account transactions';
COMMENT ON COLUMN transactions.account_id IS 'Reference to the owning account';
COMMENT ON COLUMN transactions.amount IS 'Transaction amount with 4 decimal places';
COMMENT ON COLUMN transactions.type IS 'DEBIT or CREDIT';
COMMENT ON COLUMN transactions.category IS 'Category (e.g. TRANSFER, DEPOSIT, etc.)';
COMMENT ON COLUMN transactions.risk_score IS 'Risk score calculated by the application (0–100)';
