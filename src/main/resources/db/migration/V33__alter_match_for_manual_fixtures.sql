-- =====================================================
-- Migration: Alter Match Table for Manual Fixtures
-- =====================================================
-- This migration adds new columns to the match table to support
-- the manual fixture system (round_id, group_id, etc.)

-- Use stored procedure to handle conditional logic
DELIMITER $$

DROP PROCEDURE IF EXISTS alter_match_table_if_exists$$

CREATE PROCEDURE alter_match_table_if_exists()
BEGIN
    DECLARE table_exists INT DEFAULT 0;
    DECLARE col_round_id_exists INT DEFAULT 0;
    DECLARE col_group_id_exists INT DEFAULT 0;
    DECLARE col_is_placeholder_exists INT DEFAULT 0;
    DECLARE col_match_type_exists INT DEFAULT 0;
    DECLARE col_series_number_exists INT DEFAULT 0;
    DECLARE col_bracket_position_exists INT DEFAULT 0;
    DECLARE fk_round_exists INT DEFAULT 0;
    DECLARE fk_group_exists INT DEFAULT 0;
    DECLARE chk_type_exists INT DEFAULT 0;
    DECLARE idx_round_exists INT DEFAULT 0;
    DECLARE idx_group_exists INT DEFAULT 0;
    DECLARE idx_placeholder_exists INT DEFAULT 0;
    DECLARE idx_type_exists INT DEFAULT 0;

    -- Check if table exists
    SELECT COUNT(*) INTO table_exists
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'match';

    -- Only proceed if table exists
    IF table_exists > 0 THEN
        -- Check column existence
        SELECT COUNT(*) INTO col_round_id_exists
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'match'
        AND COLUMN_NAME = 'round_id';

        SELECT COUNT(*) INTO col_group_id_exists
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'match'
        AND COLUMN_NAME = 'group_id';

        SELECT COUNT(*) INTO col_is_placeholder_exists
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'match'
        AND COLUMN_NAME = 'is_placeholder_match';

        SELECT COUNT(*) INTO col_match_type_exists
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'match'
        AND COLUMN_NAME = 'match_type';

        SELECT COUNT(*) INTO col_series_number_exists
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'match'
        AND COLUMN_NAME = 'series_number';

        SELECT COUNT(*) INTO col_bracket_position_exists
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'match'
        AND COLUMN_NAME = 'bracket_position';

        -- Add columns if they don't exist
        IF col_round_id_exists = 0 THEN
            ALTER TABLE `match` ADD COLUMN `round_id` BIGINT NULL AFTER `tournament_id`;
        END IF;

        IF col_group_id_exists = 0 THEN
            ALTER TABLE `match` ADD COLUMN `group_id` BIGINT NULL AFTER `round_id`;
        END IF;

        IF col_is_placeholder_exists = 0 THEN
            ALTER TABLE `match` ADD COLUMN `is_placeholder_match` BOOLEAN NOT NULL DEFAULT FALSE AFTER `group_name`;
        END IF;

        IF col_match_type_exists = 0 THEN
            ALTER TABLE `match` ADD COLUMN `match_type` VARCHAR(50) NULL AFTER `is_placeholder_match`;
        END IF;

        IF col_series_number_exists = 0 THEN
            ALTER TABLE `match` ADD COLUMN `series_number` INT NULL AFTER `match_type`;
        END IF;

        IF col_bracket_position_exists = 0 THEN
            ALTER TABLE `match` ADD COLUMN `bracket_position` VARCHAR(50) NULL AFTER `series_number`;
        END IF;

        -- Check foreign key existence
        SELECT COUNT(*) INTO fk_round_exists
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'match'
        AND CONSTRAINT_NAME = 'fk_match_round';

        SELECT COUNT(*) INTO fk_group_exists
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'match'
        AND CONSTRAINT_NAME = 'fk_match_group';

        SELECT COUNT(*) INTO chk_type_exists
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
        AND TABLE_NAME = 'match'
        AND CONSTRAINT_NAME = 'chk_match_type';

        -- Add foreign keys if they don't exist
        IF fk_round_exists = 0 THEN
            ALTER TABLE `match` ADD CONSTRAINT `fk_match_round` FOREIGN KEY (`round_id`) REFERENCES `tournament_round` (`id`) ON DELETE CASCADE;
        END IF;

        IF fk_group_exists = 0 THEN
            ALTER TABLE `match` ADD CONSTRAINT `fk_match_group` FOREIGN KEY (`group_id`) REFERENCES `round_group` (`id`) ON DELETE CASCADE;
        END IF;

        IF chk_type_exists = 0 THEN
            ALTER TABLE `match` ADD CONSTRAINT `chk_match_type` CHECK (`match_type` IN ('GROUP_STAGE', 'KNOCKOUT', 'SEMI_FINAL', 'FINAL', 'THIRD_PLACE', 'QUALIFIER'));
        END IF;

        -- Check index existence
        SELECT COUNT(*) INTO idx_round_exists
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'match'
        AND INDEX_NAME = 'idx_match_round_id';

        SELECT COUNT(*) INTO idx_group_exists
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'match'
        AND INDEX_NAME = 'idx_match_group_id';

        SELECT COUNT(*) INTO idx_placeholder_exists
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'match'
        AND INDEX_NAME = 'idx_match_placeholder';

        SELECT COUNT(*) INTO idx_type_exists
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'match'
        AND INDEX_NAME = 'idx_match_type';

        -- Add indexes if they don't exist
        IF idx_round_exists = 0 THEN
            CREATE INDEX `idx_match_round_id` ON `match`(`round_id`);
        END IF;

        IF idx_group_exists = 0 THEN
            CREATE INDEX `idx_match_group_id` ON `match`(`group_id`);
        END IF;

        IF idx_placeholder_exists = 0 THEN
            CREATE INDEX `idx_match_placeholder` ON `match`(`is_placeholder_match`);
        END IF;

        IF idx_type_exists = 0 THEN
            CREATE INDEX `idx_match_type` ON `match`(`match_type`);
        END IF;
    END IF;
END$$

DELIMITER ;

-- Execute the procedure
CALL alter_match_table_if_exists();

-- Drop the procedure
DROP PROCEDURE IF EXISTS alter_match_table_if_exists;

