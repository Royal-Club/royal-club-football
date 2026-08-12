package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.PasswordResetToken;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.enums.PasswordResetStatus;
import com.bjit.royalclub.royalclubfootball.exception.PasswordResetTokenException;
import com.bjit.royalclub.royalclubfootball.model.PasswordResetConfirmRequest;
import com.bjit.royalclub.royalclubfootball.model.PasswordResetResponse;
import com.bjit.royalclub.royalclubfootball.repository.PasswordResetTokenRepository;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.service.notification.PasswordResetEmailService;
import com.bjit.royalclub.royalclubfootball.util.PasswordResetTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the rules that would otherwise fail silently: the quota, single use, and the
 * lastPasswordChangeDate stamp that keeps a reset member out of the forced-change loop.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PasswordResetServiceImplTest {

    private static final String EMAIL = "player@royalfootball.club";
    private static final String TOKEN = "signed.reset.token";

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PasswordResetTokenUtil passwordResetTokenUtil;
    @Mock
    private PasswordResetEmailService passwordResetEmailService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private Player player;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "maxPerWindow", 3);
        ReflectionTestUtils.setField(passwordResetService, "windowDays", 30);

        player = Player.builder().id(7L).name("Rakib").email(EMAIL).isActive(true).build();

        when(passwordResetTokenUtil.generate(anyLong())).thenReturn(
                new PasswordResetTokenUtil.GeneratedToken(TOKEN, LocalDateTime.now().plusHours(1)));
        when(passwordResetTokenRepository.saveAndFlush(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> {
                    PasswordResetToken saved = invocation.getArgument(0);
                    saved.setId(99L);
                    return saved;
                });
        when(passwordResetEmailService.sendResetLink(any(), anyString(), any())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("ENCODED");
    }

    @Test
    void unknownAddressGetsTheSameAnswerAsARealOne() {
        when(playerRepository.findByEmailAndIsActiveTrue(EMAIL)).thenReturn(Optional.empty());

        PasswordResetResponse response = passwordResetService.requestReset(EMAIL);

        assertThat(response.getStatus()).isEqualTo(PasswordResetStatus.SENT);
        verify(passwordResetEmailService, never()).sendResetLink(any(), anyString(), any());
        verify(passwordResetTokenRepository, never()).saveAndFlush(any());
    }

    @Test
    void aDeliveredLinkIsLoggedAndRetiresTheOlderOnes() {
        when(playerRepository.findByEmailAndIsActiveTrue(EMAIL)).thenReturn(Optional.of(player));
        when(passwordResetTokenRepository.countByPlayerIdAndSentAtAfter(eq(7L), any())).thenReturn(1);

        PasswordResetResponse response = passwordResetService.requestReset(EMAIL);

        assertThat(response.getStatus()).isEqualTo(PasswordResetStatus.SENT);
        verify(passwordResetTokenRepository).saveAndFlush(any(PasswordResetToken.class));
        verify(passwordResetTokenRepository).supersedeOtherLinks(eq(7L), eq(99L), any());
    }

    @Test
    void theFourthLinkInTheWindowIsRefused() {
        when(playerRepository.findByEmailAndIsActiveTrue(EMAIL)).thenReturn(Optional.of(player));
        when(passwordResetTokenRepository.countByPlayerIdAndSentAtAfter(eq(7L), any())).thenReturn(3);

        PasswordResetResponse response = passwordResetService.requestReset(EMAIL);

        assertThat(response.getStatus()).isEqualTo(PasswordResetStatus.LIMIT_REACHED);
        verify(passwordResetEmailService, never()).sendResetLink(any(), anyString(), any());
        verify(passwordResetTokenRepository, never()).saveAndFlush(any());
    }

    @Test
    void mailThatNeverLeftTheBuildingDoesNotCostAQuotaSlot() {
        when(playerRepository.findByEmailAndIsActiveTrue(EMAIL)).thenReturn(Optional.of(player));
        when(passwordResetTokenRepository.countByPlayerIdAndSentAtAfter(eq(7L), any())).thenReturn(0);
        when(passwordResetEmailService.sendResetLink(any(), anyString(), any())).thenReturn(false);

        PasswordResetResponse response = passwordResetService.requestReset(EMAIL);

        assertThat(response.getStatus()).isEqualTo(PasswordResetStatus.SEND_FAILED);
        verify(passwordResetTokenRepository).delete(any(PasswordResetToken.class));
    }

    @Test
    void confirmingStampsThePasswordDateSoLoginDoesNotDemandTheOldPassword() {
        PasswordResetToken row = liveLink();
        when(passwordResetTokenUtil.parse(TOKEN)).thenReturn(7L);
        when(passwordResetTokenRepository.findByTokenHash(PasswordResetTokenUtil.hash(TOKEN)))
                .thenReturn(Optional.of(row));

        PasswordResetResponse response = passwordResetService.confirm(request("Str0ngPass"));

        assertThat(response.getStatus()).isEqualTo(PasswordResetStatus.RESET);
        assertThat(player.getPassword()).isEqualTo("ENCODED");
        assertThat(player.getLastPasswordChangeDate()).isNotNull();
        assertThat(row.getUsedAt()).isNotNull();
        verify(playerRepository).save(player);
    }

    @Test
    void aSpentLinkCannotBeUsedTwice() {
        PasswordResetToken row = liveLink();
        row.setUsedAt(LocalDateTime.now().minusMinutes(5));
        when(passwordResetTokenUtil.parse(TOKEN)).thenReturn(7L);
        when(passwordResetTokenRepository.findByTokenHash(PasswordResetTokenUtil.hash(TOKEN)))
                .thenReturn(Optional.of(row));

        assertThatThrownBy(() -> passwordResetService.confirm(request("Str0ngPass")))
                .isInstanceOf(PasswordResetTokenException.class)
                .extracting(e -> ((PasswordResetTokenException) e).getStatus())
                .isEqualTo(PasswordResetStatus.ALREADY_USED);
    }

    @Test
    void anExpiredRowIsRefusedEvenIfTheSignatureStillParses() {
        PasswordResetToken row = liveLink();
        row.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(passwordResetTokenUtil.parse(TOKEN)).thenReturn(7L);
        when(passwordResetTokenRepository.findByTokenHash(PasswordResetTokenUtil.hash(TOKEN)))
                .thenReturn(Optional.of(row));

        assertThatThrownBy(() -> passwordResetService.confirm(request("Str0ngPass")))
                .isInstanceOf(PasswordResetTokenException.class)
                .extracting(e -> ((PasswordResetTokenException) e).getStatus())
                .isEqualTo(PasswordResetStatus.EXPIRED);
    }

    @Test
    void aWeakPasswordLeavesTheLinkUsable() {
        PasswordResetToken row = liveLink();
        when(passwordResetTokenUtil.parse(TOKEN)).thenReturn(7L);
        when(passwordResetTokenRepository.findByTokenHash(PasswordResetTokenUtil.hash(TOKEN)))
                .thenReturn(Optional.of(row));

        PasswordResetResponse response = passwordResetService.confirm(request("weak"));

        assertThat(response.getStatus()).isEqualTo(PasswordResetStatus.WEAK_PASSWORD);
        assertThat(row.getUsedAt()).isNull();
        verify(playerRepository, never()).save(any());
    }

    private PasswordResetToken liveLink() {
        return PasswordResetToken.builder()
                .id(99L)
                .player(player)
                .tokenHash(PasswordResetTokenUtil.hash(TOKEN))
                .sentAt(LocalDateTime.now().minusMinutes(2))
                .expiresAt(LocalDateTime.now().plusMinutes(58))
                .build();
    }

    private PasswordResetConfirmRequest request(String newPassword) {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        request.setToken(TOKEN);
        request.setNewPassword(newPassword);
        return request;
    }
}
