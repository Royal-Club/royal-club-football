-- Taking a player off a team must not take their place off the pitch.
--
-- V62 gave team_formation_slot.team_player_id an ON DELETE CASCADE, which meant
-- deleting a team_player deleted the whole slot row: its position, label and
-- co-ordinates went with the player, and the saved line-up came back a place or
-- two short of its shape (a 2-2-1 rendering as four positions).
--
-- team_player_id is already nullable so a captain can save a partly filled
-- sheet, so SET NULL is what the column was always meant to do: the place stays,
-- and it is simply empty again.
--
-- Sheets already damaged are repaired on read, where the preset can say which
-- slot_index went missing; that cannot be expressed in SQL here.
ALTER TABLE team_formation_slot
    DROP FOREIGN KEY fk_team_formation_slot_team_player;

ALTER TABLE team_formation_slot
    ADD CONSTRAINT fk_team_formation_slot_team_player
        FOREIGN KEY (team_player_id) REFERENCES team_player (id) ON DELETE SET NULL;
