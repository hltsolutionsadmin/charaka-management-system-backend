-- Align b2b_user with AuditableModel fields expected by Hibernate
-- Adds: created_by, created_at, updated_by, updated_at

-- created_by
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'b2b_user'
      AND COLUMN_NAME = 'created_by'
);
SET @ddl := IF(@col_exists = 0,
               'ALTER TABLE b2b_user ADD COLUMN created_by VARCHAR(255) NULL',
               'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- created_at
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'b2b_user'
      AND COLUMN_NAME = 'created_at'
);
SET @ddl := IF(@col_exists = 0,
               'ALTER TABLE b2b_user ADD COLUMN created_at TIMESTAMP NULL',
               'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- updated_by
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'b2b_user'
      AND COLUMN_NAME = 'updated_by'
);
SET @ddl := IF(@col_exists = 0,
               'ALTER TABLE b2b_user ADD COLUMN updated_by VARCHAR(255) NULL',
               'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- updated_at
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'b2b_user'
      AND COLUMN_NAME = 'updated_at'
);
SET @ddl := IF(@col_exists = 0,
               'ALTER TABLE b2b_user ADD COLUMN updated_at TIMESTAMP NULL',
               'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
