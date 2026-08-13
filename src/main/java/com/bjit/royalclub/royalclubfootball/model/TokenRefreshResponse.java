package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Answer to a refresh. Carries a new refresh token as well as a new access token, because tokens
 * rotate on every exchange - the one the caller sent is dead by the time this is returned, so a
 * client that fails to store the replacement has ended its own session.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenRefreshResponse {
    private String token;
    private String refreshToken;
    /** Access-token lifetime in seconds, so a client can renew ahead of expiry instead of on failure. */
    private long expiresIn;
}
