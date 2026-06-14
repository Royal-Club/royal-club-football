-- Add self-referencing parent group relationship to support nested sub-groups
ALTER TABLE `round_group`
  ADD COLUMN `parent_group_id` BIGINT NULL AFTER `round_id`,
  ADD INDEX `idx_round_group_parent` (`parent_group_id`),
  ADD CONSTRAINT `fk_round_group_parent` FOREIGN KEY (`parent_group_id`)
      REFERENCES `round_group` (`id`) ON DELETE CASCADE;
