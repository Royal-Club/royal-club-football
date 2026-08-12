package com.bjit.royalclub.royalclubfootball.entity;

import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * One row per password-reset link that actually reached a member's mailbox.
 * <p>
 * Serves two jobs at once: {@code sentAt} is what the three-per-month quota counts, and
 * {@code usedAt} is what makes a link single-use. Only the SHA-256 of the token is kept - the link
 * in the mailbox is the secret, and a leaked database must not hand anyone a working reset.
 *
 * @see com.bjit.royalclub.royalclubfootball.entity.MonthlyDuesReminderLog for the same
 * send-log-as-quota-counter pattern.
 */
@Entity
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "password_reset_token")
public class PasswordResetToken extends AuditBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    /** SHA-256 hex of the emailed token, never the token itself. */
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    /** Counted by the monthly quota, so it is set only once delivery is confirmed. */
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Set when the link is spent, or when a newer link supersedes it. Null means still usable. */
    @Column(name = "used_at")
    private LocalDateTime usedAt;
}
