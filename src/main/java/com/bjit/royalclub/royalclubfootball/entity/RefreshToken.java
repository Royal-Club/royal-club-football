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
 * One row per issued refresh token - the long-lived half of a login, held only by the device.
 * <p>
 * Tokens are single-use: {@link com.bjit.royalclub.royalclubfootball.service.RefreshTokenService}
 * revokes a token the moment it is exchanged and issues a fresh one in its place. That rotation is
 * what makes a stolen token detectable, because the thief and the real device cannot both spend the
 * same row - whoever comes second presents an already-revoked token and gets the whole family killed.
 * <p>
 * Only the SHA-256 of the token is stored, for the same reason as
 * {@link PasswordResetToken}: a leaked database must not hand anyone a working session.
 */
@Entity
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "refresh_token")
public class RefreshToken extends AuditBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    /** SHA-256 hex of the token held by the device, never the token itself. */
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Set when the token is exchanged, revoked by a sign-out, or killed by reuse detection. */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    public boolean isSpendable(LocalDateTime now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }
}
