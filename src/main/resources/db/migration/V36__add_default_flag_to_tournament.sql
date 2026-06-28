ALTER TABLE `tournament`
ADD COLUMN `is_default` BOOLEAN NOT NULL DEFAULT false AFTER `is_active`;
