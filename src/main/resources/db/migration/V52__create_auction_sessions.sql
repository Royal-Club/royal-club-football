CREATE TABLE auction_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    current_auction_player_id BIGINT,
    round_number INT NOT NULL DEFAULT 1,
    started_at DATETIME,
    paused_at DATETIME,
    completed_at DATETIME,
    current_timer_ends_at DATETIME,
    created_by BIGINT,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tournament_id) REFERENCES tournament(id),
    FOREIGN KEY (current_auction_player_id) REFERENCES auction_players(id)
);
