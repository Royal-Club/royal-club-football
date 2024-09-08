-- Create table for AcCollection
CREATE TABLE IF NOT EXISTS ac_collections
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id   VARCHAR(30) UNIQUE NOT NULL,
    amount           DOUBLE             NOT NULL,
    total_amount     DOUBLE             NOT NULL,
    month_of_payment DATE               NOT NULL,
    description      TEXT,
    is_paid          BOOLEAN            NULL,
    created_by       BIGINT             NULL,
    created_date     DATETIME           NOT NULL default current_timestamp,
    last_modified_by BIGINT             NULL,
    updated_date     DATETIME           NULL     DEFAULT current_timestamp
);

-- Create join table for AcCollection and Player
CREATE TABLE IF NOT EXISTS collection_players
(
    collection_id BIGINT NOT NULL,
    player_id     BIGINT NOT NULL,
    PRIMARY KEY (collection_id, player_id),
    FOREIGN KEY (collection_id) REFERENCES ac_collections (id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players (id) ON DELETE CASCADE
);

-- Add indexes to frequently queried columns
CREATE INDEX idx_ac_collections_transaction_id ON ac_collections (transaction_id);
CREATE INDEX idx_collection_players_collection_id ON collection_players (collection_id);
CREATE INDEX idx_collection_players_player_id ON collection_players (player_id);