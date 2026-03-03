-- Fix legacy schema where `subscriptions.active` exists and is NOT NULL without default.
-- Current application writes `is_active`, so legacy `active` can break inserts.

SET @has_active := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'subscriptions'
      AND COLUMN_NAME = 'active'
);

SET @alter_active_sql := IF(
    @has_active > 0,
    'ALTER TABLE subscriptions MODIFY COLUMN active TINYINT(1) NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE stmt_alter_active FROM @alter_active_sql;
EXECUTE stmt_alter_active;
DEALLOCATE PREPARE stmt_alter_active;

SET @sync_active_sql := IF(
    @has_active > 0,
    'UPDATE subscriptions SET active = COALESCE(active, 1) WHERE active IS NULL',
    'SELECT 1'
);
PREPARE stmt_sync_active FROM @sync_active_sql;
EXECUTE stmt_sync_active;
DEALLOCATE PREPARE stmt_sync_active;
