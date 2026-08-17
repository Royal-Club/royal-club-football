package com.bjit.royalclub.royalclubfootball.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GoalKeeperPriorityDto {
    private Integer priority;
    private String category;              // ELIGIBLE | COOLDOWN | EXEMPT - see GoalKeeperQueueTier
    private Long playerId;
    private String playerName;
    private String employeeId;
    private List<String> playAsGkDates;  // Format: dd-MM-yy (e.g., "15-11-25")
    private Integer totalTournamentParticipations;        // How many tournaments player participated
    private Integer activeTournamentCount;                // Total active tournaments in system
    private Double participationFrequency;                // %: (totalParticipations / activeTournamentCount) * 100
    private Integer totalGoalKeeperTournaments;           // Distinct tournaments played as goalkeeper
    private LocalDateTime lastGoalKeeperDate;             // Last date played as goalkeeper
    private String lastPlayedTournamentDate;              // Last tournament participation date (excluding current) - Format: dd-MM-yy

    // === Goalkeeping ledger ===
    // The ranking is debt DESC within a tier. The three parts are all returned so the number can be
    // explained to the player it ranks: "you have turned up 12 times, your share of that was 1.4
    // turns, you have served 2 - you are 0.6 ahead."

    /**
     * Turns in goal this player's attendance record asks of them: summed, for every tournament they
     * actually attended, of that tournament's keeper slots divided by its head count.
     */
    private Double accruedObligation;

    /**
     * Turns in goal actually served, counting shifts rather than tournament days, and excluding the
     * tournament being ranked. This is the honest total; {@code totalGoalKeeperTournaments} counts
     * distinct tournaments and so under-reports anyone who kept goal twice in a day.
     */
    private Integer goalKeeperStints;

    /**
     * {@code accruedObligation - goalKeeperStints}. Positive means owed a turn, negative means
     * already ahead of their share. Ranks the queue.
     */
    private Double goalKeeperDebt;

    /** Past tournaments attended - the number of appearances the obligation was accrued over. */
    private Integer attendedTournaments;

    /**
     * For a resting player, how many more tournaments before they rejoin the ranking. Null for
     * everyone else.
     */
    private Integer cooldownRemaining;
}
