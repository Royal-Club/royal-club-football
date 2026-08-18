package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.ParticipationSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class LatestTournamentWithUserParticipantsResponse {
    private TournamentResponse tournament;
    private int totalParticipant;
    private int remainParticipant;
    private int totalPlayer;
    private Boolean isUserParticipated;
    private Long tournamentParticipantId;

    /**
     * Who to contact about a late change, once voting is locked. Null while it is open.
     * <p>
     * Resolved here rather than on the nested tournament because this endpoint returns exactly one
     * tournament, so naming the person costs a single lookup.
     */
    private String votingLockedByName;

    /**
     * How the caller's own answer got recorded — chiefly so a client can tell AUTO_LOCK from a
     * deliberate No. Showing "Not playing" to someone the lock decided for, as though they had
     * chosen it, is the wrong story and exactly the kind of surprise this feature exists to avoid.
     * <p>
     * Null when they have not answered, or on rows written before the source was tracked.
     */
    private ParticipationSource participationSource;
}