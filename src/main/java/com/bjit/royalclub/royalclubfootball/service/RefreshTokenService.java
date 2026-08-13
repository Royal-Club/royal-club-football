package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;

public interface RefreshTokenService {

    /**
     * Mints a token for a fresh login.
     *
     * @return the raw token - the only moment it exists in plaintext, so it must reach the caller.
     */
    String issue(Player player);

    /**
     * Spends a token and returns its owner, so the caller can mint the next access token.
     * <p>
     * The token is revoked before this returns: it is single-use by design.
     *
     * @throws com.bjit.royalclub.royalclubfootball.exception.SecurityException with 401 when the
     *                                                                         token is unknown, expired, already spent, or belongs to a member who is no longer active.
     */
    Player consume(String rawToken);

    /** Ends one session. Silent when the token is already unknown or dead - sign-out is idempotent. */
    void revoke(String rawToken);
}
