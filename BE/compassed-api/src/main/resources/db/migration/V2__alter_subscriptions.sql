-- Align subscriptions table with entity fields
ALTER TABLE subscriptions
    ADD COLUMN IF NOT EXISTS package_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS payment_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS start_date DATETIME NULL,
    ADD COLUMN IF NOT EXISTS end_date DATETIME NULL,
    ADD COLUMN IF NOT EXISTS is_active BIT(1) NULL,
    ADD COLUMN IF NOT EXISTS placement_unlocked BIT(1) NULL,
    ADD COLUMN IF NOT EXISTS created_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME NULL;

-- Backfill from legacy columns when present
UPDATE subscriptions
SET
    is_active = COALESCE(is_active, active),
    start_date = COALESCE(start_date, activated_at),
    created_at = COALESCE(created_at, activated_at),
    placement_unlocked = COALESCE(placement_unlocked, 0)
WHERE is_active IS NULL
   OR start_date IS NULL
   OR created_at IS NULL
   OR placement_unlocked IS NULL;
