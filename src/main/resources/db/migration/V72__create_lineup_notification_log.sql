-- One row per player per formation, written when that player is told they are in the line-up.
--
-- Tracked per player rather than per formation so publishing is idempotent and incremental: a
-- captain who publishes, swaps someone in, and publishes again notifies only the replacement.
-- A formation-level "notified" flag could not express that, and would either spam everyone on the
-- second publish or silence the new player entirely.
--
-- Follows the same send-log-as-ledger shape as tournament_reminder_log and
-- monthly_dues_reminder_log.
CREATE TABLE IF NOT EXISTS lineup_notification_log
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    formation_id     BIGINT   NOT NULL,
    player_id        BIGINT   NOT NULL,
    notified_at      DATETIME NOT NULL,
    created_by       BIGINT   NOT NULL,
    created_date     DATETIME NOT NULL,
    last_modified_by BIGINT   NULL,
    updated_date     DATETIME NULL DEFAULT NULL,
    -- The uniqueness that makes republishing safe: a second publish cannot insert a duplicate,
    -- so "who still needs telling" is answered by the database rather than by timing.
    UNIQUE KEY uk_lineup_notification_formation_player (formation_id, player_id),
    FOREIGN KEY (formation_id) REFERENCES team_formation (id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players (id)
);
