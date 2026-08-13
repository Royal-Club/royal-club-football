package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.RefreshToken;
import com.bjit.royalclub.royalclubfootball.exception.SecurityException;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the rules that decide whether a session survives: single use, expiry, reuse detection, and
 * the re-read that stops a deactivated member renewing. All of these fail silently if they regress -
 * the app keeps working, it just stops being safe.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RefreshTokenServiceImplTest {

    private static final String EMAIL = "player@royalfootball.club";
    private static final Long PLAYER_ID = 7L;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private Player player;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationDays", 60L);
        player = new Player();
        player.setId(PLAYER_ID);
        player.setEmail(EMAIL);
        when(playerRepository.findByEmailAndIsActiveTrueWithRoles(EMAIL)).thenReturn(Optional.of(player));
    }

    @Test
    void issueStoresOnlyTheHashAndReturnsTheRawToken() {
        String rawToken = refreshTokenService.issue(player);

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(saved.capture());

        assertThat(rawToken).isNotBlank();
        // A database dump must not yield a working session.
        assertThat(saved.getValue().getTokenHash()).isNotEqualTo(rawToken).hasSize(64);
        assertThat(saved.getValue().getExpiresAt()).isAfter(LocalDateTime.now().plusDays(59));
    }

    @Test
    void issueGivesEveryLoginADistinctToken() {
        assertThat(refreshTokenService.issue(player)).isNotEqualTo(refreshTokenService.issue(player));
    }

    @Test
    void consumeRevokesTheTokenAndReturnsAFreshlyReadPlayer() {
        String rawToken = issueAndStub(liveToken());

        Player consumed = refreshTokenService.consume(rawToken);

        assertThat(consumed).isSameAs(player);
        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(saved.capture());
        // Single use: the row is dead the moment it is spent.
        assertThat(saved.getValue().getRevokedAt()).isNotNull();
    }

    @Test
    void consumeRejectsAnUnknownToken() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.consume("never-issued"))
                .isInstanceOf(SecurityException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void consumeRejectsAnExpiredToken() {
        RefreshToken expired = liveToken();
        expired.setExpiresAt(LocalDateTime.now().minusDays(1));
        String rawToken = issueAndStub(expired);

        assertThatThrownBy(() -> refreshTokenService.consume(rawToken))
                .isInstanceOf(SecurityException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.UNAUTHORIZED);
        verify(refreshTokenRepository, never()).revokeAllForPlayer(anyLong(), any());
    }

    @Test
    void consumeOfAnAlreadySpentTokenEndsEverySessionTheMemberHas() {
        RefreshToken spent = liveToken();
        spent.setRevokedAt(LocalDateTime.now().minusMinutes(5));
        String rawToken = issueAndStub(spent);

        assertThatThrownBy(() -> refreshTokenService.consume(rawToken))
                .isInstanceOf(SecurityException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.UNAUTHORIZED);

        // Replay means one of two holders is a thief and we cannot tell which, so both lose.
        verify(refreshTokenRepository).revokeAllForPlayer(eq(PLAYER_ID), any());
    }

    @Test
    void consumeRejectsAMemberDeactivatedSinceTheLastRenewal() {
        String rawToken = issueAndStub(liveToken());
        when(playerRepository.findByEmailAndIsActiveTrueWithRoles(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.consume(rawToken))
                .isInstanceOf(SecurityException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void revokeIsSilentOnATokenThatIsAlreadyDead() {
        RefreshToken spent = liveToken();
        spent.setRevokedAt(LocalDateTime.now().minusMinutes(5));
        String rawToken = issueAndStub(spent);

        refreshTokenService.revoke(rawToken);

        // Sign-out is idempotent: nothing to write, and certainly nothing to throw.
        verify(refreshTokenRepository, never()).save(any());
    }

    private RefreshToken liveToken() {
        return RefreshToken.builder()
                .id(1L)
                .player(player)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    /**
     * Issues a real token so the test exercises the service's own hashing rather than a hard-coded
     * hash, then wires the repository to answer that hash with the row under test.
     * <p>
     * The mock is reset afterwards so the interactions each test asserts on are only the ones from
     * the {@code consume} or {@code revoke} under examination, not the setup issue.
     */
    private String issueAndStub(RefreshToken stored) {
        String rawToken = refreshTokenService.issue(player);
        ArgumentCaptor<RefreshToken> issued = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(issued.capture());
        stored.setTokenHash(issued.getValue().getTokenHash());

        reset(refreshTokenRepository);
        when(refreshTokenRepository.findByTokenHash(stored.getTokenHash())).thenReturn(Optional.of(stored));
        return rawToken;
    }
}
