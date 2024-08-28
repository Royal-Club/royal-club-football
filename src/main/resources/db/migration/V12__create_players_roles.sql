CREATE TABLE players_roles
(
    player_id BIGINT,
    role_id   BIGINT,
    PRIMARY KEY (player_id, role_id),
    FOREIGN KEY (player_id) REFERENCES players (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);
