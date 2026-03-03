-- Legacy schema compatibility:
-- some local databases still have `active` as NOT NULL without default,
-- while current code writes `is_active`.
-- This causes: Field 'active' doesn't have a default value.

ALTER TABLE subscriptions
    ADD COLUMN IF NOT EXISTS active BIT(1) NOT NULL DEFAULT b'1';

ALTER TABLE subscriptions
    MODIFY COLUMN active BIT(1) NOT NULL DEFAULT b'1';

-- Keep legacy column aligned with current column when possible.
UPDATE subscriptions
SET active = COALESCE(active, is_active, b'1')
WHERE active IS NULL;
