package com.bjit.royalclub.royalclubfootball.enums;

import lombok.Getter;

/**
 * Which band of the goalkeeper priority queue a player falls in.
 * <p>
 * Bands are applied before any ranking, so no amount of accrued obligation lifts a player out of
 * one. Keeping "who is owed a turn" (the ledger) apart from "who can reasonably take it now" (the
 * band) is the point: folding both into a single ordering is what let one quietly override the
 * other in the previous implementation.
 */
@Getter
public enum GoalKeeperQueueTier {

    /** In the running, ranked by how much goalkeeping they owe. */
    ELIGIBLE("Eligible", "Ranked by goalkeeping owed - most due first"),

    /** Kept goal recently, so held back regardless of what they are owed. Keeps accruing. */
    COOLDOWN("Resting", "Kept goal recently - held back, but still accruing"),

    /** Opted out of the rotation, so neither ranked nor accruing. Shown for transparency. */
    EXEMPT("Not in rotation", "Opted out of goalkeeping - not ranked");

    private final String label;
    private final String hint;

    GoalKeeperQueueTier(String label, String hint) {
        this.label = label;
        this.hint = hint;
    }
}
