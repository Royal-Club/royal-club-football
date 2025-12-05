ALTER TABLE `tournament`
ADD COLUMN `tournament_type` VARCHAR(50) NOT NULL DEFAULT 'ROUND_ROBIN' AFTER `sport_type`,
ADD COLUMN `group_count` INT NULL AFTER `tournament_type`;
