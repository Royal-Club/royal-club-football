package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Outcome of publishing a line-up, and the state the formation board needs to render afterwards.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LineupPublishResponse {

    /** True once anyone has ever been told about this line-up. */
    private boolean published;

    /** Players placed in the line-up right now. */
    private int placedPlayers;

    /** Told at some point — not necessarily by this call. */
    private int notifiedPlayers;

    /**
     * Placed but never told. Zero after a successful publish, and the number the board shows
     * beside the button so a captain can see there is something to announce.
     */
    private int pendingPlayers;

    /** How many were notified by this call specifically. Zero on a repeated publish. */
    private int notifiedNow;
}
