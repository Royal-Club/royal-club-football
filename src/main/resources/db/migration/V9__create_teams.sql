CREATE TABLE team
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_name     VARCHAR(255) NOT NULL,
    tournament_id BIGINT       NOT NULL,
    created_date  TIMESTAMP    NOT NULL,
    updated_date  DATETIME NULL DEFAULT NULL,
    FOREIGN KEY (tournament_id) REFERENCES tournament (id)
);
