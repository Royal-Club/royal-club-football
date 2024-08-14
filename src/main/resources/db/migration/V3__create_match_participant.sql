CREATE TABLE match_participant
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_schedule_id    BIGINT NOT NULL,
    player_id            BIGINT NOT NULL,
    participation_status BOOLEAN NOT NULL DEFAULT FALSE,
    is_active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_date         TIMESTAMP NOT NULL,
    updated_date         DATETIME NULL DEFAULT NULL,
    FOREIGN KEY (match_schedule_id) REFERENCES match_schedule(id),
    FOREIGN KEY (player_id) REFERENCES players(id),
    CONSTRAINT uq_match_participant UNIQUE (match_schedule_id, player_id)
);
