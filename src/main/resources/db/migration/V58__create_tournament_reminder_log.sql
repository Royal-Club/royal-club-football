CREATE TABLE IF NOT EXISTS tournament_reminder_log
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id    BIGINT      NOT NULL,
    player_id        BIGINT      NOT NULL,
    channel          VARCHAR(20) NOT NULL,
    sent_at          DATETIME    NOT NULL,
    created_by       BIGINT      NOT NULL,
    created_date     DATETIME    NOT NULL,
    last_modified_by BIGINT      NULL,
    updated_date     DATETIME    NULL DEFAULT NULL,
    FOREIGN KEY (tournament_id) REFERENCES tournament (id),
    FOREIGN KEY (player_id) REFERENCES players (id),
    INDEX idx_reminder_tournament_player (tournament_id, player_id)
);
