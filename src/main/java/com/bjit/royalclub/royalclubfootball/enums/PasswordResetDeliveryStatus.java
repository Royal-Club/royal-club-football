package com.bjit.royalclub.royalclubfootball.enums;

/**
 * What became of one reset link's email.
 * <p>
 * Exists so the quota can tell "we mailed you" apart from "we tried and failed", and so a process
 * that dies between the insert and the send leaves a row that can be reconciled later rather than
 * one that silently costs the member a slot.
 */
public enum PasswordResetDeliveryStatus {

    /** Row written, send not yet resolved. Anything left here is orphaned and gets reaped. */
    PENDING,

    /** The mail server accepted the message. Counts against the monthly quota. */
    SENT,

    /** The send failed, or a stale PENDING row was reaped. Never counts against the quota. */
    FAILED
}
