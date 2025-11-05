-- Index on account_id for searching for a specific account’s transactions
CREATE INDEX idx_transactions_account_id
    ON transactions(account_id);

-- Index on status (filtering transactions such as FLAGGED, PENDING, etc.)
CREATE INDEX idx_transactions_status
    ON transactions(status);

-- Index on risk_score (sorting by descending risk)
CREATE INDEX idx_transactions_risk_score
    ON transactions(risk_score DESC);

-- Index on timestamp (global chronological sorting)
CREATE INDEX idx_transactions_timestamp
    ON transactions(timestamp DESC);

-- Composite index for monitoring flagged transactions
CREATE INDEX idx_transactions_flagged
    ON transactions(status, risk_score DESC)
    WHERE status = 'FLAGGED';

-- Index on account_number for fast lookup
CREATE INDEX idx_accounts_account_number
    ON accounts(account_number);

-- Index on status for filtering
CREATE INDEX idx_accounts_status
    ON accounts(status);
