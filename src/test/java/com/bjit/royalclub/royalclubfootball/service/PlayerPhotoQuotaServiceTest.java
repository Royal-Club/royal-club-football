package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.security.UserPrinciple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Covers the rules that decide whether a member may spend an R2 write: the rolling window, the two
 * deliberate exemptions (first photo, moderators), and the ownership check on delete. Each of these
 * fails silently if it regresses - the feature keeps working, it just stops protecting the quota.
 */
class PlayerPhotoQuotaServiceTest {

    private static final String OWN_KEY = "player-7-photo.jpg";

    private PlayerPhotoQuotaService service;

    @BeforeEach
    void setUp() {
        service = new PlayerPhotoQuotaService();
        ReflectionTestUtils.setField(service, "changeWindowDays", 30L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void firstEverPhotoIsAllowed() {
        signIn(playerWith(null, null), "ROLE_USER");

        assertThatCode(() -> service.assertCanChangePhoto()).doesNotThrowAnyException();
    }

    @Test
    void aPhotoNeverYetReplacedIsStillFree() {
        // Has a photo but has never changed it, so the window has not started.
        signIn(playerWith(OWN_KEY, null), "ROLE_USER");

        assertThatCode(() -> service.assertCanChangePhoto()).doesNotThrowAnyException();
    }

    @Test
    void aSecondChangeInsideTheWindowIsRefused() {
        signIn(playerWith(OWN_KEY, LocalDateTime.now().minusDays(3)), "ROLE_USER");

        assertThatThrownBy(() -> service.assertCanChangePhoto())
                .isInstanceOf(PlayerServiceException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void aChangeIsAllowedOnceTheWindowHasPassed() {
        signIn(playerWith(OWN_KEY, LocalDateTime.now().minusDays(31)), "ROLE_USER");

        assertThatCode(() -> service.assertCanChangePhoto()).doesNotThrowAnyException();
    }

    @Test
    void theWindowIsRollingNotCalendarMonth() {
        // Changed 29 days ago: a calendar-month rule would already have refilled, a rolling one has not.
        signIn(playerWith(OWN_KEY, LocalDateTime.now().minusDays(29)), "ROLE_USER");

        assertThatThrownBy(() -> service.assertCanChangePhoto())
                .isInstanceOf(PlayerServiceException.class);
    }

    @Test
    void adminsAreExemptSoModerationIsNeverBlocked() {
        signIn(playerWith(OWN_KEY, LocalDateTime.now().minusMinutes(1)), "ROLE_ADMIN");

        assertThatCode(() -> service.assertCanChangePhoto()).doesNotThrowAnyException();
    }

    @Test
    void superadminsAreExemptToo() {
        signIn(playerWith(OWN_KEY, LocalDateTime.now().minusMinutes(1)), "ROLE_SUPERADMIN");

        assertThatCode(() -> service.assertCanChangePhoto()).doesNotThrowAnyException();
    }

    @Test
    void changeAvailableAtNamesTheDateWhileBlockedAndIsNullOnceFree() {
        Player blocked = playerWith(OWN_KEY, LocalDateTime.now().minusDays(3));
        assertThat(service.changeAvailableAt(blocked)).isNotNull();

        Player free = playerWith(OWN_KEY, LocalDateTime.now().minusDays(31));
        assertThat(service.changeAvailableAt(free)).isNull();

        assertThat(service.changeAvailableAt(playerWith(null, null))).isNull();
    }

    @Test
    void aMemberMayDeleteOnlyTheirOwnPhoto() {
        signIn(playerWith(OWN_KEY, null), "ROLE_USER");

        assertThatCode(() -> service.assertMayDelete(OWN_KEY)).doesNotThrowAnyException();
        assertThatThrownBy(() -> service.assertMayDelete("someone-elses-photo.jpg"))
                .isInstanceOf(PlayerServiceException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.FORBIDDEN);
    }

    @Test
    void adminsMayDeleteAnyPhoto() {
        signIn(playerWith(OWN_KEY, null), "ROLE_ADMIN");

        assertThatCode(() -> service.assertMayDelete("someone-elses-photo.jpg"))
                .doesNotThrowAnyException();
    }

    private Player playerWith(String photoKey, LocalDateTime photoUpdatedAt) {
        Player player = new Player();
        player.setId(7L);
        player.setEmail("player@royalfootball.club");
        player.setPhotoKey(photoKey);
        player.setPhotoUpdatedAt(photoUpdatedAt);
        return player;
    }

    /** Populates a real SecurityContext, which is what SecurityUtil reads. */
    private void signIn(Player player, String authority) {
        UserPrinciple principal = UserPrinciple.builder()
                .id(player.getId())
                .username(player.getEmail())
                .player(player)
                .authorities(List.of(new SimpleGrantedAuthority(authority)))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
