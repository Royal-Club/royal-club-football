package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.constant.AuthConstants;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Issues and redeems the reset links behind the "Forgot password" flow.
 * <p>
 * Two rules shape everything here. An address with no account gets the same answer as one with an
 * account, so the login page cannot be used to discover who is a member. And a member gets a fixed
 * number of links per rolling window, counted on delivery, so the feature cannot be leaned on
 * repeatedly - every link also spends from the same daily mail allowance the RSVP and dues
 * reminders draw on.
 * <p>
 * A member who exhausts the window is not stranded: an admin can still set a password directly
 * through {@link AuthService#resetPassword}, which is not mailed and does not touch this quota.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Pattern STRONG_PASSWORD = Pattern.compile(AuthConstants.PASSWORD_STRENGTH_PATTERN);

    private static final String SENT_MESSAGE =
            "If that email belongs to a club account, a reset link is on its way. "
                    + "Check your inbox, and your spam folder.";
    private static final String SEND_FAILED_MESSAGE =
            "We could not send the email just now. Please try again in a few minutes.";
    private static final String INVALID_MESSAGE = "This password reset link is not valid.";
    private static final String ALREADY_USED_MESSAGE =
            "This link has already been used. Request a new one from the login page.";
    private static final String EXPIRED_MESSAGE =
            "This password reset link has expired. Request a new one from the login page.";
    private static final String WEAK_PASSWORD_MESSAGE =
            "Password must be at least 8 characters and contain uppercase, lowercase and numbers.";
    private static final String RESET_MESSAGE = "Your password has been updated. You can sign in with it now.";

    private final PlayerRepository playerRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetTokenUtil passwordResetTokenUtil;
    private final PasswordResetEmailService passwordResetEmailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${password-reset.max-per-window:3}")
    private int maxPerWindow;

    @Value("${password-reset.window-days:30}")
    private int windowDays;

    /**
     * Deliberately NOT {@code @Transactional}: this method makes an SMTP call, and a transaction
     * here would hold a pooled database connection for the whole round trip - up to the 10s mail
     * timeout - while the member waits. Each repository call below commits on its own instead.
     * <p>
     * Nothing is lost by that. The quota count and the row insert were never atomic against a
     * concurrent duplicate request anyway (a plain count takes no lock), and deleting the row after
     * a failed send is a compensating action that works just as well outside a transaction.
     */
    @Override
    public PasswordResetResponse requestReset(String email) {
        Player player = playerRepository.findByEmailAndIsActiveTrue(normalise(email)).orElse(null);
        if (player == null) {
            // Deliberately indistinguishable from a successful send: the response must not confirm
            // whether an address belongs to a member.
            log.info("Password reset requested for an address with no active account; answering generically.");
            return status(PasswordResetStatus.SENT, SENT_MESSAGE);
        }
        return issueLink(player);
    }

    @Override
    @Transactional(readOnly = true)
    public PasswordResetResponse validate(String token) {
        PasswordResetToken row = requireUsableLink(token);
        return PasswordResetResponse.builder()
                .status(PasswordResetStatus.VALID)
                .playerName(row.getPlayer().getName())
                .build();
    }

    @Override
    @Transactional
    public PasswordResetResponse confirm(PasswordResetConfirmRequest request) {
        PasswordResetToken row = requireUsableLink(request.getToken());

        if (!STRONG_PASSWORD.matcher(request.getNewPassword()).matches()) {
            // Not an exception: the link is still good, so the page should keep the form open.
            return PasswordResetResponse.builder()
                    .status(PasswordResetStatus.WEAK_PASSWORD)
                    .playerName(row.getPlayer().getName())
                    .message(WEAK_PASSWORD_MESSAGE)
                    .build();
        }

        Player player = row.getPlayer();
        player.setPassword(passwordEncoder.encode(request.getNewPassword()));
        // Must be stamped, not left null: a null date makes login demand a reset that asks for the
        // old password, which is the very thing this member does not have.
        player.setLastPasswordChangeDate(LocalDateTime.now());
        playerRepository.save(player);

        row.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(row);

        log.info("Password reset completed for player {} via an emailed link.", player.getId());
        return status(PasswordResetStatus.RESET, RESET_MESSAGE);
    }

    private PasswordResetResponse issueLink(Player player) {
        LocalDateTime now = LocalDateTime.now();
        int alreadySent = passwordResetTokenRepository
                .countByPlayerIdAndSentAtAfter(player.getId(), now.minusDays(windowDays));
        if (alreadySent >= maxPerWindow) {
            log.info("Player {} has used all {} reset links in the last {} days.",
                    player.getId(), maxPerWindow, windowDays);
            return status(PasswordResetStatus.LIMIT_REACHED, limitMessage());
        }

        PasswordResetTokenUtil.GeneratedToken generated = passwordResetTokenUtil.generate(player.getId());
        PasswordResetToken row = passwordResetTokenRepository.saveAndFlush(PasswordResetToken.builder()
                .player(player)
                .tokenHash(PasswordResetTokenUtil.hash(generated.token()))
                .sentAt(now)
                .expiresAt(generated.expiresAt())
                .build());

        if (!passwordResetEmailService.sendResetLink(player, generated.token(), generated.expiresAt())) {
            // A quota slot must never be spent on mail that never left the building.
            passwordResetTokenRepository.delete(row);
            log.error("Reset link for player {} was not accepted by the mail server; quota not charged.",
                    player.getId());
            return status(PasswordResetStatus.SEND_FAILED, SEND_FAILED_MESSAGE);
        }

        passwordResetTokenRepository.supersedeOtherLinks(player.getId(), row.getId(), now);
        return status(PasswordResetStatus.SENT, SENT_MESSAGE);
    }

    /**
     * Signature first, then the stored row: the signature proves the link was minted here, and the
     * row is what makes it single-use.
     */
    private PasswordResetToken requireUsableLink(String token) {
        Long playerId = passwordResetTokenUtil.parse(token);

        PasswordResetToken row = passwordResetTokenRepository.findByTokenHash(PasswordResetTokenUtil.hash(token))
                .orElseThrow(() -> new PasswordResetTokenException(PasswordResetStatus.INVALID, INVALID_MESSAGE));

        if (!row.getPlayer().getId().equals(playerId)) {
            throw new PasswordResetTokenException(PasswordResetStatus.INVALID, INVALID_MESSAGE);
        }
        if (row.getUsedAt() != null) {
            throw new PasswordResetTokenException(PasswordResetStatus.ALREADY_USED, ALREADY_USED_MESSAGE);
        }
        // Belt and braces: the JWT carries the same expiry, but the row is the authority once a
        // secret is ever rotated.
        if (row.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new PasswordResetTokenException(PasswordResetStatus.EXPIRED, EXPIRED_MESSAGE);
        }
        if (!row.getPlayer().isActive()) {
            throw new PasswordResetTokenException(PasswordResetStatus.INVALID, INVALID_MESSAGE);
        }
        return row;
    }

    private String limitMessage() {
        return String.format(
                "You have already used all %d password resets allowed in %d days. "
                        + "Please ask a club admin to reset it for you.",
                maxPerWindow, windowDays);
    }

    private String normalise(String email) {
        return email == null ? null : email.trim();
    }

    private PasswordResetResponse status(PasswordResetStatus status, String message) {
        return PasswordResetResponse.builder().status(status).message(message).build();
    }
}
