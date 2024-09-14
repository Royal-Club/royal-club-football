CREATE TABLE player_goalkeeping_history
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    player_id        BIGINT   NOT NULL,
    round_number     INT      NOT NULL,
    played_date      DATETIME NOT NULL,
    created_by       BIGINT   NOT NULL,
    created_date     DATETIME NOT NULL,
    last_modified_by BIGINT,
    updated_date     DATETIME DEFAULT NULL,
    FOREIGN KEY (player_id) REFERENCES players (id),
    INDEX idx_player_round (player_id, round_number)
);