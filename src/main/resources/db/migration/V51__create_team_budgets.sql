CREATE TABLE team_budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    total_budget INT NOT NULL DEFAULT 10000,
    remaining_budget INT NOT NULL DEFAULT 10000,
    total_spent INT NOT NULL DEFAULT 0,
    players_bought INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by BIGINT,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tournament_id) REFERENCES tournament(id),
    FOREIGN KEY (team_id) REFERENCES team(id),
    FOREIGN KEY (owner_id) REFERENCES players(id),
    UNIQUE KEY uk_auction_tournament_team (tournament_id, team_id)
);
