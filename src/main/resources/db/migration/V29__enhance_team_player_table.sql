ALTER TABLE `team_player`
ADD COLUMN `team_player_role` VARCHAR(50) NOT NULL DEFAULT 'PLAYER' AFTER `playing_position`,
ADD COLUMN `is_captain` TINYINT(1) NOT NULL DEFAULT 0 AFTER `team_player_role`,
ADD COLUMN `jersey_number` INT NULL AFTER `is_captain`;

CREATE INDEX `idx_team_player_is_captain` ON `team_player`(`is_captain`);
CREATE INDEX `idx_team_player_role` ON `team_player`(`team_player_role`);
