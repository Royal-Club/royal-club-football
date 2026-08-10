-- Dues reminders now go out on push and email, so the cap and the "already contacted today?"
-- lookup are both scoped per channel. Counting across channels would exhaust a cap of 3 after
-- the first run and a half.
CREATE INDEX idx_dues_reminder_player_month_channel
    ON monthly_dues_reminder_log (player_id, month_of_payment, channel, sent_at);
