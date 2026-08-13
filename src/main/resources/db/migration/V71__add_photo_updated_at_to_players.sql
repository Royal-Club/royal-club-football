-- Stamped only when a player REPLACES an existing photo, never on their first one, so the
-- rolling 30-day limit never charges someone for the photo they arrived with. NULL therefore means
-- "has never changed a photo", which is exactly the state that is allowed through.
ALTER TABLE players
    ADD COLUMN photo_updated_at DATETIME NULL DEFAULT NULL;
