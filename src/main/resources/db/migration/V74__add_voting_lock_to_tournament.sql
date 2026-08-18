-- Voting lock: the coordinator closes the RSVP, then builds the teams.
--
-- Without it a squad is assembled from a list that can still move underneath it - someone flips
-- their answer after the teams are drawn and the coordinator finds out at the pitch. Locking freezes
-- the list; players who ask to change afterwards are pointed at whoever locked it.
--
-- voting_locked_by is the contact named in that message. It is deliberately not last_modified_by,
-- which any unrelated edit to the tournament would overwrite.
ALTER TABLE tournament
    ADD COLUMN voting_locked    BOOLEAN  NOT NULL DEFAULT FALSE,
    ADD COLUMN voting_locked_by BIGINT   NULL,
    ADD COLUMN voting_locked_at DATETIME NULL;

-- How a response came to be recorded.
--
-- Locking stamps every active player who never answered as a No, so selection works from a settled
-- list instead of "Yes plus a pile of silence". That insert would otherwise destroy two things
-- permanently: the difference between a deliberate No and silence, and "who never responded" -
-- which the whole app derives from the *absence* of a tournament_participant row.
--
-- Unlocking deletes exactly the AUTO_LOCK rows, which restores true pending state. A row an admin
-- has since edited carries ADMIN instead and therefore survives the undo.
--
-- NULL on historic rows: their origin is genuinely unknown, and guessing would be worse than saying so.
ALTER TABLE tournament_participant
    ADD COLUMN participation_source VARCHAR(20) NULL;
