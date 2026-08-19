-- Team chat: a private room per team, opened when the line-up is published and destroyed when the
-- tournament concludes.
--
-- There is deliberately no `team_chat_room` table. A room is exactly one team, and a team already
-- belongs to exactly one tournament, so "every tournament gets fresh rooms" is a property of the
-- existing data rather than something this feature has to maintain. All a room needs beyond that is
-- the moment it opened, which lives on `team`.
--
-- chat_opened_at NULL means no room: either the line-up was never published, or the room has been
-- purged. Both are the same thing to a caller - there is nothing to show - so one nullable column
-- carries the whole lifecycle.
ALTER TABLE team
    ADD COLUMN chat_opened_at DATETIME NULL;

-- Messages are hard-deleted at purge time, not soft-deleted, so this table has no status column and
-- no deleted_at. A row existing means the message is live and readable by that team.
CREATE TABLE IF NOT EXISTS team_chat_message
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id          BIGINT   NOT NULL,
    sender_player_id BIGINT   NOT NULL,
    -- Nullable so an attachment can be posted with no words alongside it.
    body             TEXT     NULL,
    created_by       BIGINT   NOT NULL,
    created_date     DATETIME NOT NULL,
    last_modified_by BIGINT   NULL,
    updated_date     DATETIME NULL DEFAULT NULL,
    -- The only read pattern: one room's messages in order. Paging walks backwards from the newest.
    KEY idx_team_chat_message_team_id (team_id, id),
    -- Purging a team's room, and deleting a team, both remove its messages without extra code.
    FOREIGN KEY (team_id) REFERENCES team (id) ON DELETE CASCADE,
    FOREIGN KEY (sender_player_id) REFERENCES players (id)
);

-- Files shared into a room. storage_key points at the same object store the resource library uses;
-- the purge deletes the stored object before deleting the row, because a row is the only record
-- that the object exists and losing it first would orphan the file forever.
CREATE TABLE IF NOT EXISTS team_chat_attachment
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id       BIGINT       NOT NULL,
    storage_key      VARCHAR(255) NOT NULL,
    file_name        VARCHAR(255) NOT NULL,
    content_type     VARCHAR(100) NOT NULL,
    size_bytes       BIGINT       NOT NULL,
    created_by       BIGINT       NOT NULL,
    created_date     DATETIME     NOT NULL,
    last_modified_by BIGINT       NULL,
    updated_date     DATETIME     NULL DEFAULT NULL,
    -- One key is one upload. Stops a replayed post from attaching another message's file.
    UNIQUE KEY uk_team_chat_attachment_storage_key (storage_key),
    KEY idx_team_chat_attachment_message_id (message_id),
    FOREIGN KEY (message_id) REFERENCES team_chat_message (id) ON DELETE CASCADE
);
