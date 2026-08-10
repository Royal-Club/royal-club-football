package com.bjit.royalclub.royalclubfootball.model;

/**
 * Verified contents of an RSVP link. The answer is part of the signed payload, so editing the
 * URL to flip Yes into No invalidates the signature rather than changing the vote.
 */
public record RsvpTokenPayload(Long tournamentId, Long playerId, boolean attending) {
}
