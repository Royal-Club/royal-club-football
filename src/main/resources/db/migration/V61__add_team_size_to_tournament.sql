-- Players per side for this tournament (5, 6, 7 or 11). Drives how many
-- starting slots a team formation has, so it must be known before a captain
-- can build a line-up. Existing rows default to 6 — the club's futsal format.
ALTER TABLE tournament
    ADD COLUMN team_size INT NOT NULL DEFAULT 6;
