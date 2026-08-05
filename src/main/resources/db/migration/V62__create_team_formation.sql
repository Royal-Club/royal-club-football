-- Line-ups a team captain (or an admin) builds on the pitch view.
--
-- A team has one *default* formation (match_id IS NULL) plus at most one
-- formation per match. The per-match row is created lazily by copying the
-- default the first time someone opens it, so captains only edit what they
-- actually want to change.
--
-- MySQL does not enforce UNIQUE across NULLs, so the default row would not be
-- protected by UNIQUE (team_id, match_id). The generated match_key column
-- collapses NULL to 0 and gives us a real one-row-per-scope guarantee.
CREATE TABLE IF NOT EXISTS team_formation
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id          BIGINT      NOT NULL,
    match_id         BIGINT      NULL,
    match_key        BIGINT AS (IFNULL(match_id, 0)) STORED,
    preset_name      VARCHAR(30) NOT NULL,
    team_size        INT         NOT NULL,
    notes            VARCHAR(500) NULL,
    created_by       BIGINT      NOT NULL,
    created_date     DATETIME    NOT NULL,
    last_modified_by BIGINT      NULL,
    updated_date     DATETIME    NULL DEFAULT NULL,
    CONSTRAINT uc_team_formation_scope UNIQUE (team_id, match_key),
    CONSTRAINT fk_team_formation_team FOREIGN KEY (team_id) REFERENCES team (id) ON DELETE CASCADE,
    -- MySQL 8 forbids CASCADE/SET NULL on a FK column used by a STORED generated column
    CONSTRAINT fk_team_formation_match FOREIGN KEY (match_id) REFERENCES `match` (id) ON DELETE RESTRICT,
    INDEX idx_team_formation_match (match_id)
);

-- One row per pitch slot and per bench place. Starters and bench share a single
-- slot_index sequence (starters first) so ordering is unambiguous.
--
-- x/y are percentages of the pitch box, matching the co-ordinate space the
-- front-end already uses for the "Best XI" pitch: 0/0 is top-left, and the
-- team's own goal is at the bottom. Presets seed them; captains may nudge.
--
-- team_player_id is nullable so a captain can save a partially filled sheet.
CREATE TABLE IF NOT EXISTS team_formation_slot
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    formation_id   BIGINT        NOT NULL,
    team_player_id BIGINT        NULL,
    slot_index     INT           NOT NULL,
    position_group VARCHAR(10)   NOT NULL,
    slot_label     VARCHAR(30)   NULL,
    x              DECIMAL(5, 2) NOT NULL DEFAULT 50.00,
    y              DECIMAL(5, 2) NOT NULL DEFAULT 50.00,
    is_starter     BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT uc_team_formation_slot_index UNIQUE (formation_id, slot_index),
    CONSTRAINT fk_team_formation_slot_formation FOREIGN KEY (formation_id) REFERENCES team_formation (id) ON DELETE CASCADE,
    CONSTRAINT fk_team_formation_slot_team_player FOREIGN KEY (team_player_id) REFERENCES team_player (id) ON DELETE CASCADE,
    INDEX idx_team_formation_slot_team_player (team_player_id)
);
