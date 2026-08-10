package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.RsvpVoteStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * What the public /rsvp page renders, both when previewing a link and after confirming a vote.
 */
@Builder
@Data
public class RsvpVoteResponse {

    private RsvpVoteStatus status;
    private String playerName;
    private String tournamentName;
    private LocalDateTime tournamentDate;
    private String venueName;

    /** The answer the link carries (preview) or the answer now recorded (after confirming). */
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private boolean attending;

    /** Human-readable line the page can show verbatim. */
    private String message;
}
