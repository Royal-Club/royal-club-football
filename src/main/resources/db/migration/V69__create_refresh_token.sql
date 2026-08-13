CREATE TABLE IF NOT EXISTS refresh_token
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id        BIGINT      NOT NULL,
    token_hash       VARCHAR(64) NOT NULL,
    expires_at       DATETIME    NOT NULL,
    revoked_at       DATETIME    NULL DEFAULT NULL,
    created_by       BIGINT      NOT NULL,
    created_date     DATETIME    NOT NULL,
    last_modified_by BIGINT      NULL,
    updated_date     DATETIME    NULL DEFAULT NULL,
    UNIQUE KEY uk_refresh_token_hash (token_hash),
    FOREIGN KEY (player_id) REFERENCES players (id),
    -- Reuse detection revokes every live token a member holds, so that pair is the hot lookup.
    INDEX idx_refresh_token_player_revoked (player_id, revoked_at)
);
