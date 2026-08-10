-- Separates the one-off creation invite from the D-2/D-1/match-day nudges.
-- An INVITE occupies its calendar day (so it suppresses that day's reminder) but does not
-- count against reminders.max-per-player, giving at most 1 invite + 3 reminders per player.
ALTER TABLE tournament_reminder_log
    ADD COLUMN reminder_type VARCHAR(20) NOT NULL DEFAULT 'REMINDER';

-- Backs the "already contacted this player on this channel today?" lookup.
CREATE INDEX idx_reminder_tournament_player_channel_sent
    ON tournament_reminder_log (tournament_id, player_id, channel, sent_at);
