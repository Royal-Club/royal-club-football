CREATE TABLE monthly_collections
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id     BIGINT    NOT NULL,
    amount        DOUBLE    NOT NULL,
    payment_month DATE      NOT NULL,
    description   VARCHAR(255),
    created_date  TIMESTAMP NOT NULL,
    updated_date  DATETIME NULL DEFAULT NULL,
    FOREIGN KEY (player_id) REFERENCES players (id),
    INDEX         idx_player_payment_month (player_id, payment_month)
);
