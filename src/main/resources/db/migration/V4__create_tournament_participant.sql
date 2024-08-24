CREATE TABLE tournament_participant
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id        BIGINT    NOT NULL,
    player_id            BIGINT    NOT NULL,
    participation_status BOOLEAN   NOT NULL DEFAULT FALSE,
    created_date         TIMESTAMP NOT NULL,
    updated_date         DATETIME NULL DEFAULT NULL,
    FOREIGN KEY (tournament_id) REFERENCES tournament (id),
    FOREIGN KEY (player_id) REFERENCES players (id),
    CONSTRAINT uq_match_participant UNIQUE (tournament_id, player_id)
);