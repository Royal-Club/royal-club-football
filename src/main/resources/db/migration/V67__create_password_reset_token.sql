CREATE TABLE IF NOT EXISTS password_reset_token
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id        BIGINT      NOT NULL,
    token_hash       VARCHAR(64) NOT NULL,
    sent_at          DATETIME    NOT NULL,
    expires_at       DATETIME    NOT NULL,
    used_at          DATETIME    NULL DEFAULT NULL,
    created_by       BIGINT      NOT NULL,
    created_date     DATETIME    NOT NULL,
    last_modified_by BIGINT      NULL,
    updated_date     DATETIME    NULL DEFAULT NULL,
    UNIQUE KEY uk_password_reset_token_hash (token_hash),
    FOREIGN KEY (player_id) REFERENCES players (id),
    -- The monthly quota is a count over this pair, so it is the one index that must exist.
    INDEX idx_password_reset_player_sent (player_id, sent_at)
);
