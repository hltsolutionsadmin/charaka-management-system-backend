-- Align B2B_USER with AuditableModel fields expected by Hibernate
-- Adds: created_at, created_by, updated_at, updated_by

ALTER TABLE B2B_USER
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL;
