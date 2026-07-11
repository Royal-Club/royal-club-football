-- Optional scheduled auction start time. Surfaced publicly in the Match Center
-- as a countdown before the auction goes live. NULL = not scheduled yet.
ALTER TABLE auction_settings
    ADD COLUMN scheduled_start_time DATETIME NULL;
