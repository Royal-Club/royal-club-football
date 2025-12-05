-- =====================================================
-- Migration: Match Related Tables
-- =====================================================
-- This migration creates match, match_event, and match_statistics tables

-- Create match table if it doesn't exist
CREATE TABLE IF NOT EXISTS `match` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tournament_id` BIGINT NOT NULL,
    `home_team_id` BIGINT NOT NULL,
    `away_team_id` BIGINT NOT NULL,
    `venue_id` BIGINT,
    `match_date` DATETIME NOT NULL,
    `match_status` VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    `match_order` INT,
    `round` INT,
    `group_name` VARCHAR(100),
    `home_team_score` INT NOT NULL DEFAULT 0,
    `away_team_score` INT NOT NULL DEFAULT 0,
    `match_duration_minutes` INT,
    `elapsed_time_seconds` INT NOT NULL DEFAULT 0,
    `started_at` DATETIME,
    `completed_at` DATETIME,
    `created_by` BIGINT NOT NULL,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_modified_by` BIGINT,
    `updated_date` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_tournament_id` (`tournament_id` ASC),
    INDEX `idx_match_status` (`match_status` ASC),
    INDEX `idx_match_date` (`match_date` ASC),
    INDEX `idx_home_team_id` (`home_team_id` ASC),
    INDEX `idx_away_team_id` (`away_team_id` ASC),
    INDEX `idx_tournament_round` (`tournament_id` ASC, `round` ASC),
    INDEX `idx_tournament_group` (`tournament_id` ASC, `group_name` ASC),
    CONSTRAINT `fk_match_tournament` FOREIGN KEY (`tournament_id`) REFERENCES `tournament` (`id`),
    CONSTRAINT `fk_match_home_team` FOREIGN KEY (`home_team_id`) REFERENCES `team` (`id`),
    CONSTRAINT `fk_match_away_team` FOREIGN KEY (`away_team_id`) REFERENCES `team` (`id`),
    CONSTRAINT `fk_match_venue` FOREIGN KEY (`venue_id`) REFERENCES `venues` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create match_event table if it doesn't exist
CREATE TABLE IF NOT EXISTS `match_event` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `match_id` BIGINT NOT NULL,
    `event_type` VARCHAR(50) NOT NULL,
    `player_id` BIGINT NOT NULL,
    `team_id` BIGINT NOT NULL,
    `event_time` INT NOT NULL,
    `description` VARCHAR(255),
    `related_player_id` BIGINT,
    `details` JSON,
    `created_by` BIGINT NOT NULL,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_modified_by` BIGINT,
    `updated_date` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_match_event_match` (`match_id`),
    INDEX `idx_match_event_player` (`player_id`),
    INDEX `idx_match_event_team` (`team_id`),
    INDEX `idx_match_event_type` (`event_type`),
    CONSTRAINT `fk_match_event_match` FOREIGN KEY (`match_id`) REFERENCES `match` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_match_event_player` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`),
    CONSTRAINT `fk_match_event_team` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`),
    CONSTRAINT `fk_match_event_related_player` FOREIGN KEY (`related_player_id`) REFERENCES `players` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Create match_statistics table if it doesn't exist
CREATE TABLE IF NOT EXISTS `match_statistics` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `match_id` BIGINT NOT NULL,
    `player_id` BIGINT NOT NULL,
    `team_id` BIGINT NOT NULL,
    `goals_scored` INT NOT NULL DEFAULT 0,
    `assists` INT NOT NULL DEFAULT 0,
    `red_cards` INT NOT NULL DEFAULT 0,
    `yellow_cards` INT NOT NULL DEFAULT 0,
    `substitution_in` INT NOT NULL DEFAULT 0,
    `substitution_out` INT NOT NULL DEFAULT 0,
    `minutes_played` INT NOT NULL DEFAULT 0,
    `created_by` BIGINT NOT NULL,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_modified_by` BIGINT,
    `updated_date` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_match_statistics_match` (`match_id`),
    INDEX `idx_match_statistics_player` (`player_id`),
    INDEX `idx_match_statistics_team` (`team_id`),
    INDEX `idx_match_player` (`match_id`, `player_id`),
    UNIQUE KEY `unique_match_player` (`match_id`, `player_id`),
    CONSTRAINT `fk_match_statistics_match` FOREIGN KEY (`match_id`) REFERENCES `match` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_match_statistics_player` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`),
    CONSTRAINT `fk_match_statistics_team` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

