package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.PasswordResetToken;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.enums.PasswordResetDeliveryStatus;
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
 * Covers the rules that would otherwise fail silently: the quota, single use, the delivery outcome
 * recorded against each link, and the lastPasswordChangeDate stamp that keeps a reset member out of
 * the forced-change loop.
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

    /** The row handed to saveAndFlush, so a test can assert the outcome written against it. */
    private PasswordResetToken savedRow;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "maxPerWindow", 3);
        ReflectionTestUtils.setField(passwordResetService, "windowDays", 30);
        ReflectionTestUtils.setField(passwordResetService, "stalePendingMinutes", 15);

        player = Player.builder().id(7L).name("Rakib").email(EMAIL).isActive(true).build();

        when(passwordResetTokenUtil.generate(anyLong())).thenReturn(
                new PasswordResetTokenUtil.GeneratedToken(TOKEN, LocalDateTime.now().plusHours(1)));
        when(passwordResetTokenRepository.saveAndFlush(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> {
                    savedRow = invocation.getArgument(0);
                    savedRow.setId(99L);
                    return savedRow;
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
    void aDeliveredLinkIsRecordedAsSentAndRetiresTheOlderOnes() {
        givenPlayerWithLinksUsed(1);

        PasswordResetResponse response = passwordResetService.requestReset(EMAIL);

        assertThat(response.getStatus()).isEqualTo(PasswordResetStatus.SENT);
        assertThat(savedRow.getStatus()).isEqualTo(PasswordResetDeliveryStatus.SENT);
        verify(passwordResetTokenRepository).supersedeOtherLinks(eq(7L), eq(99L), any());
    }

    @Test
    void theRowIsWrittenAsPendingBeforeTheSendIsAttempted() {
        givenPlayerWithLinksUsed(0);
        // Captures the status at the moment of the send - the window a crash would leave behind.
        when(passwordResetEmailService.sendResetLink(any(), anyString(), any()))
                .thenAnswer(invocation -> {
                    assertThat(savedRow.getStatus()).isEqualTo(PasswordResetDeliveryStatus.PENDING);
                    return true;
                });

        passwordResetService.requestReset(EMAIL);

        assertThat(savedRow.getStatus()).isEqualTo(PasswordResetDeliveryStatus.SENT);
    }

    @Test
    void theFourthLinkInTheWindowIsRefused() {
        givenPlayerWithLinksUsed(3);

        PasswordResetResponse response = passwordResetService.requestReset(EMAIL);

        assertThat(response.getStatus()).isEqualTo(PasswordResetStatus.LIMIT_REACHED);
        verify(passwordResetEmailService, never()).sendResetLink(any(), anyString(), any());
        verify(passwordResetTokenRepository, never()).saveAndFlush(any());
    }

    @Test
    void mailThatNeverLeftTheBuildingIsRecordedAsFailedRatherThanErased() {
        givenPlayerWithLinksUsed(0);
        when(passwordResetEmailService.sendResetLink(any(), anyString(), any())).thenReturn(false);

        PasswordResetResponse response = passwordResetService.requestReset(EMAIL);

        assertThat(response.getStatus()).isEqualTo(PasswordResetStatus.SEND_FAILED);
        // FAILED rows are excluded from the quota, so the attempt is kept for audit without costing a slot.
        assertThat(savedRow.getStatus()).isEqualTo(PasswordResetDeliveryStatus.FAILED);
        verify(passwordResetTokenRepository, never()).delete(any());
    }

    @Test
    void abandonedLinksAreReapedSoTheirQuotaSlotsComeBack() {
        when(passwordResetTokenRepository.failStalePendingLinks(any())).thenReturn(2);

        assertThat(passwordResetService.reconcileStalePendingLinks()).isEqualTo(2);
    }

    @Test
    void confirmingStampsThePasswordDateSoLoginDoesNotDemandTheOldPassword() {
        PasswordResetToken row = givenLiveLink();

        PasswordResetResponse response = passwordResetService.confirm(request("Str0ngPass"));

        assertThat(response.getStatus()).isEqualTo(PasswordResetStatus.RESET);
        assertThat(player.getPassword()).isEqualTo("ENCODED");
        assertThat(player.getLastPasswordChangeDate()).isNotNull();
        assertThat(row.getUsedAt()).isNotNull();
        verify(playerRepository).save(player);
    }

    @Test
    void aSpentLinkCannotBeUsedTwice() {
        givenLiveLink().setUsedAt(LocalDateTime.now().minusMinutes(5));

        assertThatThrownBy(() -> passwordResetService.confirm(request("Str0ngPass")))
                .isInstanceOf(PasswordResetTokenException.class)
                .extracting(e -> ((PasswordResetTokenException) e).getStatus())
                .isEqualTo(PasswordResetStatus.ALREADY_USED);
    }

    @Test
    void anExpiredRowIsRefusedEvenIfTheSignatureStillParses() {
        givenLiveLink().setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertThatThrownBy(() -> passwordResetService.confirm(request("Str0ngPass")))
                .isInstanceOf(PasswordResetTokenException.class)
                .extracting(e -> ((PasswordResetTokenException) e).getStatus())
                .isEqualTo(PasswordResetStatus.EXPIRED);
    }

    /**
     * A link reaped as FAILED may still have reached a mailbox, so validity is governed by usedAt
     * and expiresAt rather than by the delivery status.
     */
    @Test
    void aLinkReapedAsFailedStillWorksIfItDidReachTheMember() {
        givenLiveLink().setStatus(PasswordResetDeliveryStatus.FAILED);

        PasswordResetResponse response = passwordResetService.confirm(request("Str0ngPass"));

        assertThat(response.getStatus()).isEqualTo(PasswordResetStatus.RESET);
    }

    @Test
    void aWeakPasswordLeavesTheLinkUsable() {
        PasswordResetToken row = givenLiveLink();

        PasswordResetResponse response = passwordResetService.confirm(request("weak"));

        assertThat(response.getStatus()).isEqualTo(PasswordResetStatus.WEAK_PASSWORD);
        assertThat(row.getUsedAt()).isNull();
        verify(playerRepository, never()).save(any());
    }

    private void givenPlayerWithLinksUsed(int used) {
        when(playerRepository.findByEmailAndIsActiveTrue(EMAIL)).thenReturn(Optional.of(player));
        when(passwordResetTokenRepository.countByPlayerIdAndSentAtAfterAndStatusNot(
                eq(7L), any(), eq(PasswordResetDeliveryStatus.FAILED))).thenReturn(used);
    }

    private PasswordResetToken givenLiveLink() {
        PasswordResetToken row = PasswordResetToken.builder()
                .id(99L)
                .player(player)
                .tokenHash(PasswordResetTokenUtil.hash(TOKEN))
                .status(PasswordResetDeliveryStatus.SENT)
                .sentAt(LocalDateTime.now().minusMinutes(2))
                .expiresAt(LocalDateTime.now().plusMinutes(58))
                .build();
        when(passwordResetTokenUtil.parse(TOKEN)).thenReturn(7L);
        when(passwordResetTokenRepository.findByTokenHash(PasswordResetTokenUtil.hash(TOKEN)))
                .thenReturn(Optional.of(row));
        return row;
    }

    private PasswordResetConfirmRequest request(String newPassword) {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        request.setToken(TOKEN);
        request.setNewPassword(newPassword);
        return request;
    }
}
