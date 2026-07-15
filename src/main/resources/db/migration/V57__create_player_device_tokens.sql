CREATE TABLE IF NOT EXISTS player_device_token
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id        BIGINT       NOT NULL,
    token            VARCHAR(512) NOT NULL,
    platform         VARCHAR(20)  NOT NULL,
    created_by       BIGINT       NOT NULL,
    created_date     DATETIME     NOT NULL,
    last_modified_by BIGINT       NULL,
    updated_date     DATETIME     NULL DEFAULT NULL,
    CONSTRAINT uc_player_device_token_token UNIQUE (token),
    FOREIGN KEY (player_id) REFERENCES players (id)
);
