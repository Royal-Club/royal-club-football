ALTER TABLE `tournament`
ADD COLUMN `season` VARCHAR(100) NULL AFTER `name`,
ADD COLUMN `description` TEXT NULL AFTER `season`;
