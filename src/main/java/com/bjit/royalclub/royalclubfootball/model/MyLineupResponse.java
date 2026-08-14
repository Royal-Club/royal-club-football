package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One player's own line-up for a tournament: the team they were drawn into, the published formation,
 * and the slot they hold in it.
 *
 * Exists so a client does not have to fetch every team in the tournament and work out which one it
 * belongs to. That answer is a domain rule and belongs here - a client deciding it means the rule
 * cannot be changed without shipping a new app, and older installs keep answering it the old way.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyLineupResponse {

    private Long tournamentId;
    private TournamentTeamResponse team;
    private TeamFormationResponse formation;

    /** The slot this player holds, or null if the captain has not placed them. */
    private TeamFormationSlotResponse mySlot;

    /** Slots with a player in them, and the total the formation has. */
    private int filledSlots;
    private int totalSlots;
}
