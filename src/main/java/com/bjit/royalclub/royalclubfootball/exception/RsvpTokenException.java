package com.bjit.royalclub.royalclubfootball.exception;

import com.bjit.royalclub.royalclubfootball.enums.RsvpVoteStatus;
import lombok.Getter;

/**
 * Thrown when an RSVP link cannot be trusted. Carries the status so the public endpoint can tell
 * an expired link apart from a forged one instead of collapsing both into a generic error.
 */
@Getter
public class RsvpTokenException extends RuntimeException {

    private final RsvpVoteStatus status;

    public RsvpTokenException(RsvpVoteStatus status, String message) {
        super(message);
        this.status = status;
    }
}
