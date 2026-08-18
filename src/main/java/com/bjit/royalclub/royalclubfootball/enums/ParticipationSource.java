package com.bjit.royalclub.royalclubfootball.enums;

/**
 * How a Yes/No answer came to be recorded.
 * <p>
 * Exists mainly so the auto-No written when voting is locked stays distinguishable from a
 * deliberate No. Without it, locking would silently erase the club's record of who actually
 * ignored the RSVP, and unlocking could not tell which rows it was safe to remove.
 */
public enum ParticipationSource {
    /** The player answered in the app. */
    SELF_APP,
    /** The player answered through a one-click link in an RSVP email. */
    SELF_EMAIL,
    /** A coordinator or admin recorded the answer on the player's behalf. */
    ADMIN,
    /** Nobody answered; the row was written by the voting lock as a No. */
    AUTO_LOCK
}
