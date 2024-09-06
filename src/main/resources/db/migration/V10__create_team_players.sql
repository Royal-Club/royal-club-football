CREATE TABLE IF NOT EXISTS team_player
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id          BIGINT      NOT NULL,
    player_id        BIGINT      NOT NULL,
    playing_position VARCHAR(50) NOT NULL,
    created_by       BIGINT      NOT NULL,
    created_date     DATETIME    NOT NULL,
    last_modified_by BIGINT      NULL,
    updated_date     DATETIME    NULL DEFAULT NULL,
    FOREIGN KEY (team_id) REFERENCES team (id),
    FOREIGN KEY (player_id) REFERENCES players (id)
);
