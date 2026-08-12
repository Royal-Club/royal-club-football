package com.bjit.royalclub.royalclubfootball.enums;

/**
 * Outcome of a step in the emailed password-reset flow, reported to the public pages so they can
 * render the right message instead of a generic failure.
 */
public enum PasswordResetStatus {

    /** A link was mailed - also the answer for an address with no account, so the page cannot be used to discover members. */
    SENT,

    /** The monthly quota is spent; the member has to ask an admin. */
    LIMIT_REACHED,

    /** The address exists but the mail server refused the message, so no quota was spent. */
    SEND_FAILED,

    /** The link checks out and the page may show the new-password form. */
    VALID,

    /** The new password was saved. */
    RESET,

    /** Signature, audience or payload is wrong - a forged or mangled link. */
    INVALID,

    /** The link was genuine but is past its lifetime. */
    EXPIRED,

    /** The link was already spent, or superseded by a newer one. */
    ALREADY_USED,

    /** The link is fine but the submitted password does not meet the strength rules. */
    WEAK_PASSWORD
}
