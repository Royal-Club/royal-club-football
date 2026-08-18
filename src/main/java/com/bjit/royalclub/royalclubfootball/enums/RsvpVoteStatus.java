package com.bjit.royalclub.royalclubfootball.enums;

/**
 * Outcome of a one-click RSVP link, mapped 1:1 to the states the public /rsvp page renders.
 */
public enum RsvpVoteStatus {
    /** Vote recorded for the first time. */
    RECORDED,
    /** Player had already answered; the vote was overwritten with the new answer. */
    UPDATED,
    /** Signature did not verify, or the link was tampered with. */
    INVALID,
    /** Link is past its expiry (kickoff). */
    EXPIRED,
    /** Tournament was cancelled or deactivated after the email went out. */
    TOURNAMENT_CANCELLED,
    /** Kickoff has passed, so the answer no longer means anything. */
    TOURNAMENT_STARTED,
    /** A coordinator closed the RSVP to pick teams from it; late changes go through them. */
    VOTING_LOCKED
}
