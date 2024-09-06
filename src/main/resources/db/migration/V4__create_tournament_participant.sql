CREATE TABLE IF NOT EXISTS tournament_participant
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id        BIGINT       NOT NULL,
    player_id            BIGINT       NOT NULL,
    participation_status BOOLEAN      NOT NULL DEFAULT FALSE,
    comments             VARCHAR(255) NULL,
    created_by           BIGINT       NOT NULL,
    created_date         TIMESTAMP    NOT NULL,
    last_modified_by     BIGINT       NULL,
    updated_date         DATETIME     NULL     DEFAULT NULL,
    FOREIGN KEY (tournament_id) REFERENCES tournament (id),
    FOREIGN KEY (player_id) REFERENCES players (id)
);

