-- Keep only one source of truth for subscription active flag: `active`.
ALTER TABLE subscriptions
    ADD COLUMN IF NOT EXISTS active BIT(1) NOT NULL DEFAULT b'1';

ALTER TABLE subscriptions
    MODIFY COLUMN active BIT(1) NOT NULL DEFAULT b'1';

-- Backfill before dropping legacy column.
UPDATE subscriptions
SET active = COALESCE(active, is_active, b'1')
WHERE active IS NULL;

-- MySQL compatibility: emulate DROP COLUMN IF EXISTS.
SET @has_is_active := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'subscriptions'
      AND COLUMN_NAME = 'is_active'
);
SET @drop_sql := IF(@has_is_active > 0, 'ALTER TABLE subscriptions DROP COLUMN is_active', 'SELECT 1');
PREPARE stmt FROM @drop_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
