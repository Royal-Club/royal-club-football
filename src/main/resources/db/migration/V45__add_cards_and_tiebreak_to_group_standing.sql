-- =====================================================
-- Migration: Add disciplinary (cards), fair-play and manual
-- tiebreak columns to group_standing.
-- =====================================================
-- These support the full group ranking sequence:
--   Points -> Goal Difference -> Goals For -> Head-to-Head
--   -> Fair Play Record -> Penalty Shootout (manual tiebreak).
--
-- Head-to-Head is computed in-memory from matches and needs no column.
-- Fair Play uses the UEFA deduction model (lower is better):
--   yellow = -1, second yellow (=indirect red) = -3,
--   direct red = -4, yellow + direct red = -5.

ALTER TABLE `group_standing`
    ADD COLUMN `yellow_cards` INT NOT NULL DEFAULT 0 AFTER `points`,
    ADD COLUMN `red_cards` INT NOT NULL DEFAULT 0 AFTER `yellow_cards`,
    ADD COLUMN `fair_play_points` INT NOT NULL DEFAULT 0 AFTER `red_cards`,
    ADD COLUMN `tiebreak_rank` INT NULL AFTER `fair_play_points`;

-- Lower tiebreak_rank wins (1 = beats everyone tied). NULL = not set.
ALTER TABLE `group_standing`
    ADD CONSTRAINT `chk_standing_tiebreak_rank_positive`
        CHECK (`tiebreak_rank` IS NULL OR `tiebreak_rank` > 0);
