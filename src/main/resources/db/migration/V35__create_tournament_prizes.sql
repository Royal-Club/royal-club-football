-- Create tournament_prize table for both team and player prizes
CREATE TABLE IF NOT EXISTS `tournament_prize`
(
    `id`                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    `tournament_id`       BIGINT       NOT NULL,
    `prize_type`          VARCHAR(20)  NOT NULL COMMENT 'TEAM or PLAYER',

    -- Recipient (one will be null based on prize_type)
    `team_id`             BIGINT       NULL,
    `player_id`           BIGINT       NULL,

    -- Prize details
    `position_rank`       INT          NOT NULL COMMENT '1 for champion, 2 for runner-up, etc.',
    `prize_amount`        DECIMAL(10,2) NULL COMMENT 'Monetary value',
    `prize_category`      VARCHAR(50)  NOT NULL COMMENT 'CHAMPION, RUNNER_UP, TOP_SCORER, etc.',
    `description`         TEXT         NULL,
    `image_links`         JSON         NULL COMMENT 'Array of image URLs',

    -- Standard audit fields
    `created_by`          BIGINT       NOT NULL,
    `created_date`        TIMESTAMP    NOT NULL,
    `last_modified_by`    BIGINT       NULL,
    `updated_date`        DATETIME     NULL DEFAULT NULL,

    -- Foreign keys with CASCADE delete
    CONSTRAINT `fk_tournament_prize_tournament`
        FOREIGN KEY (`tournament_id`) REFERENCES `tournament` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_tournament_prize_team`
        FOREIGN KEY (`team_id`) REFERENCES `team` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_tournament_prize_player`
        FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE,

    -- Business logic constraints
    CONSTRAINT `chk_prize_type` CHECK (`prize_type` IN ('TEAM', 'PLAYER')),
    CONSTRAINT `chk_team_or_player` CHECK (
        (`prize_type` = 'TEAM' AND `team_id` IS NOT NULL AND `player_id` IS NULL) OR
        (`prize_type` = 'PLAYER' AND `player_id` IS NOT NULL AND `team_id` IS NULL)
    ),

    -- Prevent duplicates: one prize category per team/player per tournament
    CONSTRAINT `uk_tournament_team_category`
        UNIQUE (`tournament_id`, `team_id`, `prize_category`),
    CONSTRAINT `uk_tournament_player_category`
        UNIQUE (`tournament_id`, `player_id`, `prize_category`)
);

-- Create indexes for performance
CREATE INDEX `idx_tournament_prize_tournament_id` ON `tournament_prize` (`tournament_id`);
CREATE INDEX `idx_tournament_prize_team_id` ON `tournament_prize` (`team_id`);
CREATE INDEX `idx_tournament_prize_player_id` ON `tournament_prize` (`player_id`);
CREATE INDEX `idx_tournament_prize_type` ON `tournament_prize` (`prize_type`);
CREATE INDEX `idx_tournament_prize_category` ON `tournament_prize` (`prize_category`);
