package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * What the client needs to decide whether to show a room at all, and what to say when it cannot.
 *
 * <p>{@code closedReason} exists so the UI never has to reconstruct why the room is unavailable from
 * a combination of nulls. "The line-up has not been published yet" and "this tournament is over and
 * the chat was deleted" look identical in the data and mean completely different things to a player.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamChatRoomResponse {

    private Long teamId;
    private String teamName;
    private Long tournamentId;
    private String tournamentName;

    /** True only when the caller may read and post right now. */
    private boolean open;

    /** Null while the room is open. */
    private String closedReason;

    private LocalDateTime openedAt;

    /** Members of this team, so the room can show who is in it. */
    private List<TeamPlayerResponse> members;

    private long messageCount;

    /** Bytes of shared files this room already holds. */
    private long storageUsedBytes;

    /**
     * The room's total file budget, shared by its members.
     *
     * <p>Sent rather than hard-coded in the client so the two can never disagree about what the
     * limit is - a UI promising more space than the server will accept is worse than no indicator.
     */
    private long storageLimitBytes;

    /** Largest single file this room will accept. */
    private long maxFileBytes;
}
