package com.bjit.royalclub.royalclubfootball.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Tuning knobs for the goalkeeper priority queue.
 *
 * @see com.bjit.royalclub.royalclubfootball.service.PlayerServiceImpl#getGoalKeeperPriorityQueue(Long)
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "goalkeeper-queue")
public class GoalKeeperQueueProperties {

    /**
     * How many of the most recent tournaments count as a rest period. Anyone who kept goal within
     * this many tournaments drops below everyone who did not, however much they are owed.
     * <p>
     * 1 reproduces the old "not two in a row" rule. 2 is the default because it is the smallest
     * value that stops a player being asked again the week after next.
     */
    private int cooldownTournaments = 2;

    /**
     * Goalkeeper slots assumed for a tournament with no goalkeeping history recorded against it.
     * <p>
     * Only consulted when {@link #estimateMissingSlots} is true. A tournament with genuinely no
     * recorded keeper is indistinguishable from one whose keepers were never entered, so this is a
     * guess either way - the response reports how many tournaments needed it.
     */
    private int defaultSlotsPerTournament = 2;

    /**
     * Whether to fall back to {@link #defaultSlotsPerTournament} for tournaments with no recorded
     * keepers. False treats them as creating no obligation at all, which is the safer reading if
     * your early history is patchy in a way that would otherwise invent debt for long-serving
     * members.
     */
    private boolean estimateMissingSlots = true;
}
