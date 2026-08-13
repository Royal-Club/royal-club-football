package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.RefreshToken;
import com.bjit.royalclub.royalclubfootball.exception.SecurityException;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.SESSION_EXPIRED;

/**
 * Issues, rotates and revokes the long-lived half of a login.
 *
 * @see com.bjit.royalclub.royalclubfootball.entity.RefreshToken for why tokens are single-use and
 * stored only as hashes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    /** 256 bits of entropy - the token is a bearer credential, so guessing must be hopeless. */
    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final PlayerRepository playerRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.refresh-expiration-days}")
    private long refreshExpirationDays;

    @Override
    @Transactional
    public String issue(Player player) {
        String rawToken = randomToken();
        refreshTokenRepository.save(RefreshToken.builder()
                .player(player)
                .tokenHash(hash(rawToken))
                .expiresAt(LocalDateTime.now().plusDays(refreshExpirationDays))
                .build());
        return rawToken;
    }

    @Override
    @Transactional
    public Player consume(String rawToken) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new SecurityException(SESSION_EXPIRED, HttpStatus.UNAUTHORIZED));

        LocalDateTime now = LocalDateTime.now();
        if (stored.getRevokedAt() != null) {
            // Someone is replaying a token that was already spent. Either the device is retrying a
            // rotation whose response it never saw, or a stolen token is in play - and we cannot
            // tell which. Ending every session for the member is the safe reading: the honest owner
            // signs in again, the thief is locked out.
            int ended = refreshTokenRepository.revokeAllForPlayer(stored.getPlayer().getId(), now);
            log.warn("Refresh token reuse detected for player {}; revoked {} live session(s)",
                    stored.getPlayer().getId(), ended);
            throw new SecurityException(SESSION_EXPIRED, HttpStatus.UNAUTHORIZED);
        }
        if (!stored.isSpendable(now)) {
            throw new SecurityException(SESSION_EXPIRED, HttpStatus.UNAUTHORIZED);
        }

        stored.setRevokedAt(now);
        refreshTokenRepository.save(stored);

        // Re-read rather than trusting the row's player: a member deactivated since the last refresh
        // must not be able to renew their way into a session that outlives the deactivation.
        return playerRepository.findByEmailAndIsActiveTrueWithRoles(stored.getPlayer().getEmail())
                .orElseThrow(() -> new SecurityException(SESSION_EXPIRED, HttpStatus.UNAUTHORIZED));
    }

    @Override
    @Transactional
    public void revoke(String rawToken) {
        Optional<RefreshToken> stored = refreshTokenRepository.findByTokenHash(hash(rawToken));
        stored.filter(token -> token.getRevokedAt() == null).ifPresent(token -> {
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
        });
    }

    private String randomToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandatory on every JVM; if it is missing the process is unusable anyway.
            throw new IllegalStateException(e);
        }
    }
}
