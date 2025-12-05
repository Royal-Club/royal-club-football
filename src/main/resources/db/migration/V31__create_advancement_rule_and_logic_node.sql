-- =====================================================
-- Migration: Advancement Rules and Logic Nodes
-- =====================================================
-- This migration creates advancement_rule and logic_node tables
-- for automated team advancement in tournaments

-- Create advancement_rule table for defining team progression between rounds
CREATE TABLE IF NOT EXISTS `advancement_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `source_round_id` BIGINT NOT NULL,
    `source_group_id` BIGINT,
    `target_round_id` BIGINT NOT NULL,
    `target_group_id` BIGINT,
    `rule_type` VARCHAR(50) NOT NULL,
    `rule_config` TEXT NOT NULL,
    `priority_order` INT,

    -- Audit fields
    `created_by` BIGINT,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_modified_by` BIGINT,
    `updated_date` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    INDEX `idx_advancement_rule_source_round` (`source_round_id`),
    INDEX `idx_advancement_rule_source_group` (`source_group_id`),
    INDEX `idx_advancement_rule_target_round` (`target_round_id`),
    INDEX `idx_advancement_rule_target_group` (`target_group_id`),
    INDEX `idx_advancement_rule_type` (`rule_type`),

    CONSTRAINT `fk_advancement_rule_source_round` FOREIGN KEY (`source_round_id`) REFERENCES `tournament_round` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_advancement_rule_source_group` FOREIGN KEY (`source_group_id`) REFERENCES `round_group` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_advancement_rule_target_round` FOREIGN KEY (`target_round_id`) REFERENCES `tournament_round` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_advancement_rule_target_group` FOREIGN KEY (`target_group_id`) REFERENCES `round_group` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_advancement_rule_type` CHECK (`rule_type` IN ('TOP_N', 'WINNER', 'LOSER', 'BEST_THIRD_PLACE', 'ALL_TEAMS')),
    CONSTRAINT `chk_advancement_priority` CHECK (`priority_order` IS NULL OR `priority_order` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Defines how teams advance from one round to the next (e.g., top 2 from each group)';

-- Create logic_node table for automated team advancement rules
CREATE TABLE IF NOT EXISTS `logic_node` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tournament_id` BIGINT NOT NULL,
    `node_name` VARCHAR(100) NOT NULL,
    `node_type` VARCHAR(50) NOT NULL,
    `source_round_id` BIGINT,
    `source_group_id` BIGINT,
    `target_round_id` BIGINT NOT NULL,
    `rule_config` TEXT,
    `priority_order` INT,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `auto_execute` BOOLEAN NOT NULL DEFAULT TRUE,
    `execution_count` INT NOT NULL DEFAULT 0,
    `last_executed_at` DATETIME,

    -- Audit fields
    `created_by` BIGINT,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_modified_by` BIGINT,
    `updated_date` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    INDEX `idx_logic_node_tournament` (`tournament_id`),
    INDEX `idx_logic_node_source_round` (`source_round_id`),
    INDEX `idx_logic_node_source_group` (`source_group_id`),
    INDEX `idx_logic_node_target_round` (`target_round_id`),
    INDEX `idx_logic_node_active` (`is_active`),
    INDEX `idx_logic_node_auto_execute` (`auto_execute`),

    CONSTRAINT `fk_logic_node_tournament` FOREIGN KEY (`tournament_id`) REFERENCES `tournament` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_logic_node_source_round` FOREIGN KEY (`source_round_id`) REFERENCES `tournament_round` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_logic_node_source_group` FOREIGN KEY (`source_group_id`) REFERENCES `round_group` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_logic_node_target_round` FOREIGN KEY (`target_round_id`) REFERENCES `tournament_round` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_logic_node_type` CHECK (`node_type` IN ('ADVANCEMENT', 'FILTER', 'CUSTOM')),
    CONSTRAINT `chk_logic_node_priority` CHECK (`priority_order` IS NULL OR `priority_order` > 0),
    CONSTRAINT `chk_logic_node_execution_count` CHECK (`execution_count` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Logic nodes for automated team advancement based on configurable rules';

