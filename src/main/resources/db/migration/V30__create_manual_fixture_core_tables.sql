-- =====================================================
-- Consolidated Migration: Manual Fixture Core Tables
-- =====================================================
-- This migration creates all core tables for the manual fixture system
-- Includes: tournament_round, round_group, round_group_team, round_team, group_standing

-- Create tournament_round table for manual fixture system
CREATE TABLE IF NOT EXISTS `tournament_round` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tournament_id` BIGINT NOT NULL,
    `round_number` INT NOT NULL,
    `round_name` VARCHAR(100) NOT NULL,
    `round_type` VARCHAR(50) NOT NULL,
    `round_format` VARCHAR(50),
    `advancement_rule` TEXT,
    `status` VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    `sequence_order` INT NOT NULL,
    `start_date` DATETIME,
    `end_date` DATETIME,

    -- Audit fields
    `created_by` BIGINT,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_modified_by` BIGINT,
    `updated_date` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_tournament_round_number` (`tournament_id`, `round_number`),
    UNIQUE KEY `uq_tournament_sequence_order` (`tournament_id`, `sequence_order`),
    INDEX `idx_tournament_round_tournament` (`tournament_id`),
    INDEX `idx_tournament_round_sequence` (`tournament_id`, `sequence_order`),
    INDEX `idx_tournament_round_status` (`status`),

    CONSTRAINT `fk_tournament_round_tournament` FOREIGN KEY (`tournament_id`) REFERENCES `tournament` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_round_type` CHECK (`round_type` IN ('GROUP_BASED', 'DIRECT_KNOCKOUT')),
    CONSTRAINT `chk_round_format` CHECK (`round_format` IN ('ROUND_ROBIN', 'SINGLE_ELIMINATION', 'DOUBLE_ELIMINATION', 'SWISS_SYSTEM', 'CUSTOM')),
    CONSTRAINT `chk_round_status` CHECK (`status` IN ('NOT_STARTED', 'ONGOING', 'COMPLETED')),
    CONSTRAINT `chk_round_number_positive` CHECK (`round_number` > 0),
    CONSTRAINT `chk_sequence_order_positive` CHECK (`sequence_order` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Stores rounds for multi-round tournaments (e.g., Group Stage, Quarter Finals, Semi Finals, Final)';

-- Create round_group table for group-based rounds
CREATE TABLE IF NOT EXISTS `round_group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `round_id` BIGINT NOT NULL,
    `group_name` VARCHAR(100) NOT NULL,
    `group_format` VARCHAR(50),
    `advancement_rule` TEXT,
    `max_teams` INT,
    `status` VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',

    -- Audit fields
    `created_by` BIGINT,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_modified_by` BIGINT,
    `updated_date` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_round_group_name` (`round_id`, `group_name`),
    INDEX `idx_round_group_round` (`round_id`),
    INDEX `idx_round_group_status` (`status`),

    CONSTRAINT `fk_round_group_round` FOREIGN KEY (`round_id`) REFERENCES `tournament_round` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_group_format` CHECK (`group_format` IN ('MANUAL', 'ROUND_ROBIN_SINGLE', 'ROUND_ROBIN_DOUBLE', 'CUSTOM_MULTIPLE')),
    CONSTRAINT `chk_group_status` CHECK (`status` IN ('NOT_STARTED', 'ONGOING', 'COMPLETED')),
    CONSTRAINT `chk_max_teams_positive` CHECK (`max_teams` IS NULL OR `max_teams` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Stores groups within a round (e.g., Group A, Group B in Group Stage)';

-- Create round_group_team junction table for teams in groups
CREATE TABLE IF NOT EXISTS `round_group_team` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `group_id` BIGINT NOT NULL,
    `team_id` BIGINT,
    `assignment_type` VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    `source_rule` TEXT,
    `is_placeholder` BOOLEAN NOT NULL DEFAULT FALSE,
    `placeholder_name` VARCHAR(255),
    `assigned_at` DATETIME,

    -- Audit fields
    `created_by` BIGINT,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_modified_by` BIGINT,
    `updated_date` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    INDEX `idx_round_group_team_group` (`group_id`),
    INDEX `idx_round_group_team_team` (`team_id`),
    INDEX `idx_round_group_team_placeholder` (`is_placeholder`),

    CONSTRAINT `fk_round_group_team_group` FOREIGN KEY (`group_id`) REFERENCES `round_group` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_round_group_team_team` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_group_team_assignment_type` CHECK (`assignment_type` IN ('MANUAL', 'RULE_BASED', 'PLACEHOLDER')),
    -- Ensure placeholder logic consistency: if placeholder, team_id must be NULL and placeholder_name must be set
    CONSTRAINT `chk_group_team_placeholder_logic` CHECK (
        (`is_placeholder` = TRUE AND `team_id` IS NULL AND `placeholder_name` IS NOT NULL) OR
        (`is_placeholder` = FALSE AND `team_id` IS NOT NULL)
    ),
    -- Ensure unique team per group (handles NULL team_id for placeholders differently)
    CONSTRAINT `uq_group_team_actual` UNIQUE (`group_id`, `team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Junction table linking teams to groups. Supports both actual teams and placeholder teams (TBD)';

-- Create round_team table for teams in direct knockout rounds (non-group rounds)
CREATE TABLE IF NOT EXISTS `round_team` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `round_id` BIGINT NOT NULL,
    `team_id` BIGINT,
    `assignment_type` VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    `source_rule` TEXT,
    `is_placeholder` BOOLEAN NOT NULL DEFAULT FALSE,
    `placeholder_name` VARCHAR(255),
    `seed_position` INT,
    `assigned_at` DATETIME,

    -- Audit fields
    `created_by` BIGINT,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_modified_by` BIGINT,
    `updated_date` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    INDEX `idx_round_team_round` (`round_id`),
    INDEX `idx_round_team_team` (`team_id`),
    INDEX `idx_round_team_placeholder` (`is_placeholder`),
    INDEX `idx_round_team_seed` (`round_id`, `seed_position`),

    CONSTRAINT `fk_round_team_round` FOREIGN KEY (`round_id`) REFERENCES `tournament_round` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_round_team_team` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_round_team_assignment_type` CHECK (`assignment_type` IN ('MANUAL', 'RULE_BASED', 'PLACEHOLDER')),
    CONSTRAINT `chk_seed_position_positive` CHECK (`seed_position` IS NULL OR `seed_position` > 0),
    -- Ensure placeholder logic consistency: if placeholder, team_id must be NULL and placeholder_name must be set
    CONSTRAINT `chk_round_team_placeholder_logic` CHECK (
        (`is_placeholder` = TRUE AND `team_id` IS NULL AND `placeholder_name` IS NOT NULL) OR
        (`is_placeholder` = FALSE AND `team_id` IS NOT NULL)
    ),
    -- Ensure unique team per round (handles NULL team_id for placeholders differently)
    CONSTRAINT `uq_round_team_actual` UNIQUE (`round_id`, `team_id`),
    -- Ensure unique seed position per round (only for non-NULL seed positions)
    CONSTRAINT `uq_round_seed_position` UNIQUE (`round_id`, `seed_position`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Teams in direct knockout rounds (not in groups). Supports seeding and placeholder teams';

-- Create group_standing table for tracking group standings
CREATE TABLE IF NOT EXISTS `group_standing` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `group_id` BIGINT NOT NULL,
    `team_id` BIGINT NOT NULL,
    `matches_played` INT NOT NULL DEFAULT 0,
    `wins` INT NOT NULL DEFAULT 0,
    `draws` INT NOT NULL DEFAULT 0,
    `losses` INT NOT NULL DEFAULT 0,
    `goals_for` INT NOT NULL DEFAULT 0,
    `goals_against` INT NOT NULL DEFAULT 0,
    `goal_difference` INT NOT NULL DEFAULT 0,
    `points` INT NOT NULL DEFAULT 0,
    `position` INT,
    `is_advanced` BOOLEAN DEFAULT FALSE,

    -- Audit fields
    `created_by` BIGINT,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_modified_by` BIGINT,
    `updated_date` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_group_standing_team` (`group_id`, `team_id`),
    INDEX `idx_group_standing_group` (`group_id`),
    INDEX `idx_group_standing_team` (`team_id`),
    INDEX `idx_group_standing_position` (`group_id`, `position`),
    INDEX `idx_group_standing_points` (`group_id`, `points` DESC),

    CONSTRAINT `fk_group_standing_group` FOREIGN KEY (`group_id`) REFERENCES `round_group` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_group_standing_team` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_standing_non_negative` CHECK (
        `matches_played` >= 0 AND
        `wins` >= 0 AND
        `draws` >= 0 AND
        `losses` >= 0 AND
        `goals_for` >= 0 AND
        `goals_against` >= 0 AND
        `points` >= 0
    ),
    CONSTRAINT `chk_standing_position_positive` CHECK (`position` IS NULL OR `position` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Calculated standings for teams in a group (auto-updated after each match)';

