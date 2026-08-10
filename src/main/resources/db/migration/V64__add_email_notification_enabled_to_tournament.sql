-- Per-tournament switch for invitation/reminder emails. Push notifications are unaffected.
-- Existing tournaments backfill as enabled, matching the "on by default" behaviour of the create form.
ALTER TABLE tournament
    ADD COLUMN email_notification_enabled BOOLEAN NOT NULL DEFAULT TRUE;
