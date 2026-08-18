package com.bjit.royalclub.royalclubfootball.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * State of a tournament's RSVP lock, plus the tallies a coordinator needs to decide whether to
 * pull the trigger - chiefly how many players are still silent and would be stamped as No.
 */
@Data
@Builder
public class VotingLockResponse {

    private Long tournamentId;

    // NON_EMPTY would drop a false and the edit form has to see it explicitly.
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private boolean votingLocked;

    private Long lockedById;
    /** Display name of the person to contact about a late change. Null while unlocked. */
    private String lockedByName;
    private LocalDateTime lockedAt;

    /** Answered Yes. */
    private int confirmedCount;
    /** Answered No, including anyone the lock stamped. */
    private int declinedCount;
    /** Active players with no answer on record. Zero immediately after a lock. */
    private int pendingCount;
    /** Rows this lock created (on a lock) or removed (on an unlock). Zero for a plain status read. */
    private int autoMarkedCount;
}
