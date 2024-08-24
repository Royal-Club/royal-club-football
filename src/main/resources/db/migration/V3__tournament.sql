CREATE TABLE tournament
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    tournament_date TIMESTAMP    NOT NULL,
    venue_id        BIGINT       NOT NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT true,
    created_date    TIMESTAMP    NOT NULL,
    updated_date    DATETIME NULL DEFAULT NULL,
    FOREIGN KEY (venue_id) REFERENCES venues (id)
);