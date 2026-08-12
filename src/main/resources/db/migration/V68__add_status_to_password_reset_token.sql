-- Records what happened to each reset link, so a process that dies mid-send leaves evidence
-- instead of a row that silently charges the member a quota slot for mail they never got.
--
-- Existing rows default to SENT: under the previous code a row only survived when the mail server
-- had already accepted the message, so that is the truthful backfill.
ALTER TABLE password_reset_token
    ADD COLUMN status VARCHAR(10) NOT NULL DEFAULT 'SENT' AFTER token_hash;

-- Drop the default now the backfill is done, so the application must state the outcome explicitly
-- rather than silently inheriting SENT for a link it never managed to deliver.
ALTER TABLE password_reset_token
    ALTER COLUMN status DROP DEFAULT;
