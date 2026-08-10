package com.bjit.royalclub.royalclubfootball.util;

import com.bjit.royalclub.royalclubfootball.enums.RsvpVoteStatus;
import com.bjit.royalclub.royalclubfootball.exception.RsvpTokenException;
import com.bjit.royalclub.royalclubfootball.model.RsvpTokenPayload;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * Mints and verifies the signed tokens behind the Yes/No links in RSVP emails.
 * <p>
 * Deliberately keyed on its own secret rather than {@code jwt.secret}: an RSVP link travels through
 * mailboxes and mail scanners, so it must never be replayable as a login token. The distinct key
 * plus the {@code rsvp-vote} audience means a token from here cannot satisfy the auth filter, and
 * rotating one secret does not invalidate the other.
 */
@Component
public class RsvpTokenUtil {

    private static final String AUDIENCE = "rsvp-vote";
    private static final String CLAIM_TOURNAMENT_ID = "tid";
    private static final String CLAIM_ATTENDING = "att";

    private final Key key;

    public RsvpTokenUtil(@Value("${rsvp.token.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * @param expiresAt normally kickoff, so a link stays usable (and re-usable, for a change of
     *                  mind) right up to the moment the answer stops mattering.
     */
    public String generate(Long tournamentId, Long playerId, boolean attending, LocalDateTime expiresAt) {
        return Jwts.builder()
                .setAudience(AUDIENCE)
                .setSubject(String.valueOf(playerId))
                .claim(CLAIM_TOURNAMENT_ID, tournamentId)
                .claim(CLAIM_ATTENDING, attending)
                .setIssuedAt(new Date())
                // Stored tournament dates are UTC; say so rather than trusting the host's default zone.
                .setExpiration(Date.from(expiresAt.toInstant(ZoneOffset.UTC)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * @throws RsvpTokenException carrying EXPIRED or INVALID, so callers can render the right page.
     */
    public RsvpTokenPayload parse(String token) {
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .requireAudience(AUDIENCE)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RsvpTokenException(RsvpVoteStatus.EXPIRED, "This RSVP link has expired.");
        } catch (Exception e) {
            throw new RsvpTokenException(RsvpVoteStatus.INVALID, "This RSVP link is not valid.");
        }

        Long tournamentId = claims.get(CLAIM_TOURNAMENT_ID, Number.class) == null
                ? null
                : claims.get(CLAIM_TOURNAMENT_ID, Number.class).longValue();
        Boolean attending = claims.get(CLAIM_ATTENDING, Boolean.class);

        if (tournamentId == null || attending == null || claims.getSubject() == null) {
            throw new RsvpTokenException(RsvpVoteStatus.INVALID, "This RSVP link is not valid.");
        }

        try {
            return new RsvpTokenPayload(tournamentId, Long.valueOf(claims.getSubject()), attending);
        } catch (NumberFormatException e) {
            throw new RsvpTokenException(RsvpVoteStatus.INVALID, "This RSVP link is not valid.");
        }
    }
}
