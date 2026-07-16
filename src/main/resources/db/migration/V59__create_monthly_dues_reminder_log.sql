CREATE TABLE IF NOT EXISTS monthly_dues_reminder_log
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id        BIGINT      NOT NULL,
    month_of_payment DATE        NOT NULL,
    channel          VARCHAR(20) NOT NULL,
    sent_at          DATETIME    NOT NULL,
    created_by       BIGINT      NOT NULL,
    created_date     DATETIME    NOT NULL,
    last_modified_by BIGINT      NULL,
    updated_date     DATETIME    NULL DEFAULT NULL,
    FOREIGN KEY (player_id) REFERENCES players (id),
    INDEX idx_dues_reminder_player_month (player_id, month_of_payment)
);
