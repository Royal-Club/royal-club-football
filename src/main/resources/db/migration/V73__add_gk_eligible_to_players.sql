-- Whether a player takes part in the goalkeeper rotation at all.
--
-- Deliberately separate from players.playing_position: position is the outfield role someone is
-- listed under, which says nothing about whether they are willing or able to keep goal. Mixing the
-- two would either force specialists into the fairness rotation or quietly drop anyone whose
-- position happens to be GOALKEEPER out of it.
--
-- Ineligible players are still shown in the priority queue, but unranked - they neither accrue
-- goalkeeping obligation nor consume a slot in the ordering.
ALTER TABLE players
    ADD COLUMN gk_eligible BOOLEAN NOT NULL DEFAULT TRUE;
