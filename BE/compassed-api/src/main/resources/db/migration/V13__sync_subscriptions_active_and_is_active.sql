-- Ensure both legacy/new columns are safe and consistent across environments.
ALTER TABLE subscriptions
    ADD COLUMN IF NOT EXISTS is_active BIT(1) NOT NULL DEFAULT b'1';

ALTER TABLE subscriptions
    MODIFY COLUMN is_active BIT(1) NOT NULL DEFAULT b'1';

ALTER TABLE subscriptions
    ADD COLUMN IF NOT EXISTS active BIT(1) NOT NULL DEFAULT b'1';

ALTER TABLE subscriptions
    MODIFY COLUMN active BIT(1) NOT NULL DEFAULT b'1';

UPDATE subscriptions
SET
    is_active = COALESCE(is_active, active, b'1'),
    active = COALESCE(active, is_active, b'1')
WHERE is_active IS NULL
   OR active IS NULL;
