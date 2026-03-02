-- Ensure legacy local DB has all columns required by current Subscription entity.
ALTER TABLE subscriptions
    ADD COLUMN IF NOT EXISTS package_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS payment_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS start_date DATETIME NULL,
    ADD COLUMN IF NOT EXISTS end_date DATETIME NULL,
    ADD COLUMN IF NOT EXISTS is_active BIT(1) NULL,
    ADD COLUMN IF NOT EXISTS placement_unlocked BIT(1) NULL,
    ADD COLUMN IF NOT EXISTS created_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME NULL;

-- Backfill with safe defaults for local legacy rows.
UPDATE subscriptions
SET
    is_active = COALESCE(is_active, b'1'),
    start_date = COALESCE(start_date, NOW()),
    created_at = COALESCE(created_at, NOW()),
    placement_unlocked = COALESCE(placement_unlocked, b'0')
WHERE is_active IS NULL
   OR start_date IS NULL
   OR created_at IS NULL
   OR placement_unlocked IS NULL;

-- Set safe defaults for package/payment/end_date when missing.
UPDATE subscriptions
SET
    package_id = COALESCE(package_id, 0),
    payment_id = COALESCE(payment_id, 0),
    end_date = COALESCE(end_date, DATE_ADD(start_date, INTERVAL 1 YEAR))
WHERE package_id IS NULL
   OR payment_id IS NULL
   OR end_date IS NULL;
