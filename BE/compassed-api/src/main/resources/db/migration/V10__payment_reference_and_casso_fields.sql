ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS payment_reference VARCHAR(64) NULL,
    ADD COLUMN IF NOT EXISTS last_checked_at DATETIME NULL;

CREATE INDEX IF NOT EXISTS idx_payments_payment_reference ON payments (payment_reference);
