package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.model.VotingLockResponse;

/**
 * Closing and reopening a tournament's RSVP.
 * <p>
 * Locking is the deliberate line between "we are still collecting answers" and "these are the
 * players we are picking teams from".
 */
public interface TournamentVotingLockService {

    /**
     * Closes the RSVP and stamps every active player who never answered as a No, in one transaction
     * so the flag and the backfill can never disagree.
     */
    VotingLockResponse lock(Long tournamentId);

    /** Reopens the RSVP and removes the No rows the lock wrote. */
    VotingLockResponse unlock(Long tournamentId);

    VotingLockResponse status(Long tournamentId);

    /**
     * Throws when the tournament is locked and the caller is an ordinary member, naming whoever
     * locked it. Managers pass through: editing a single answer on request is the intended escape
     * hatch, and is the reason members are told to contact someone rather than just refused.
     */
    void requireVotingOpen(Tournament tournament);

    /** Display name of whoever locked it, or null when unlocked or the player no longer exists. */
    String lockedByName(Tournament tournament);
}
