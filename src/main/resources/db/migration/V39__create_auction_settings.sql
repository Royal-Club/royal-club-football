CREATE TABLE auction_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL UNIQUE,
    team_budget INT NOT NULL DEFAULT 10000,
    min_squad_size INT NOT NULL DEFAULT 10,
    max_squad_size INT NOT NULL DEFAULT 15,
    auction_timer_seconds INT NOT NULL DEFAULT 180,
    bid_increment INT NOT NULL DEFAULT 100,
    unsold_reauction_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    timer_extension_seconds INT NOT NULL DEFAULT 15,
    extend_if_bid_within_last_seconds INT NOT NULL DEFAULT 15,
    min_role_requirements JSON,
    auction_status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
    created_by BIGINT,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tournament_id) REFERENCES tournament(id)
);
