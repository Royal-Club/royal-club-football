package com.bjit.royalclub.royalclubfootball.util;

import com.bjit.royalclub.royalclubfootball.enums.PasswordResetStatus;
import com.bjit.royalclub.royalclubfootball.exception.PasswordResetTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HexFormat;

/**
 * Mints and verifies the signed tokens behind password-reset links.
 * <p>
 * Keyed on its own secret rather than {@code jwt.secret} for the same reason as
 * {@link RsvpTokenUtil}: a reset link sits in a mailbox and passes through mail scanners, so it
 * must never be replayable as a login token. The distinct key plus the {@code password-reset}
 * audience means a token from here cannot satisfy the auth filter, and rotating one secret does not
 * invalidate the other.
 * <p>
 * The signature alone is not sufficient authority to change a password - it only proves the link
 * was minted here and has not expired. Single use is enforced against
 * {@link com.bjit.royalclub.royalclubfootball.entity.PasswordResetToken}, which is why {@link #hash}
 * exists.
 */
@Component
public class PasswordResetTokenUtil {

    private static final String AUDIENCE = "password-reset";

    private final Key key;
    private final long linkTtlMinutes;

    public PasswordResetTokenUtil(@Value("${password-reset.token.secret}") String secret,
                                  @Value("${password-reset.link-ttl-minutes:60}") long linkTtlMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.linkTtlMinutes = linkTtlMinutes;
    }

    /**
     * Short-lived by design: a reset link is a standing invitation to take over an account, so it
     * should stop working long before an old mailbox is ever browsed again.
     */
    public GeneratedToken generate(Long playerId) {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(linkTtlMinutes);
        String token = Jwts.builder()
                .setAudience(AUDIENCE)
                .setSubject(String.valueOf(playerId))
                .setIssuedAt(new Date())
                // The expiry above is host wall-clock time, so it converts back through the host's
                // own zone - unlike the RSVP token, whose expiry comes from a UTC-stored column.
                .setExpiration(Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        return new GeneratedToken(token, expiresAt);
    }

    /**
     * @return the player the link was minted for.
     * @throws PasswordResetTokenException carrying EXPIRED or INVALID, so callers can render the
     *                                     right page.
     */
    public Long parse(String token) {
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .requireAudience(AUDIENCE)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new PasswordResetTokenException(PasswordResetStatus.EXPIRED,
                    "This password reset link has expired.");
        } catch (Exception e) {
            throw new PasswordResetTokenException(PasswordResetStatus.INVALID,
                    "This password reset link is not valid.");
        }

        if (claims.getSubject() == null) {
            throw new PasswordResetTokenException(PasswordResetStatus.INVALID,
                    "This password reset link is not valid.");
        }

        try {
            return Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new PasswordResetTokenException(PasswordResetStatus.INVALID,
                    "This password reset link is not valid.");
        }
    }

    /** What gets stored, so the database never holds a working link. */
    public static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by the JRE spec; unreachable on any supported runtime.
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    /** The token to mail out, paired with the expiry the log row has to record. */
    public record GeneratedToken(String token, LocalDateTime expiresAt) {
    }
}
