package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.PasswordResetToken;
import com.bjit.royalclub.royalclubfootball.enums.PasswordResetDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /**
     * How many links this member has been charged for since {@code since} - the rolling-window quota.
     * <p>
     * Excludes FAILED, so a send the mail server refused never costs a slot. PENDING still counts:
     * while a send is in flight the honest assumption is that it worked, and the reaper flips
     * anything genuinely orphaned to FAILED, which hands the slot back.
     */
    int countByPlayerIdAndSentAtAfterAndStatusNot(Long playerId, LocalDateTime since,
                                                  PasswordResetDeliveryStatus excluded);

    /**
     * Retires every other live link for this member, so only the newest one works. Without this, a
     * reset mailed three weeks ago would still be usable from an old mailbox.
     */
    // Carries its own transaction: the caller runs outside one so that an SMTP call never holds a
    // connection, and a @Modifying query cannot execute without one.
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PasswordResetToken t SET t.usedAt = :now "
            + "WHERE t.player.id = :playerId AND t.usedAt IS NULL AND t.id <> :keepId")
    void supersedeOtherLinks(@Param("playerId") Long playerId,
                             @Param("keepId") Long keepId,
                             @Param("now") LocalDateTime now);

    /**
     * Resolves rows abandoned mid-send - the process died between writing the row and learning
     * whether the mail went out.
     * <p>
     * They are settled as FAILED rather than SENT deliberately: we cannot know either way, and
     * wrongly refunding a quota slot is a far smaller harm than wrongly refusing someone a password
     * reset. The link itself stays usable if it did reach a mailbox, because validity is governed by
     * {@code usedAt} and {@code expiresAt}, not by this column.
     *
     * @return how many rows were reaped.
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PasswordResetToken t SET t.status = "
            + "com.bjit.royalclub.royalclubfootball.enums.PasswordResetDeliveryStatus.FAILED "
            + "WHERE t.status = com.bjit.royalclub.royalclubfootball.enums.PasswordResetDeliveryStatus.PENDING "
            + "AND t.sentAt < :cutoff")
    int failStalePendingLinks(@Param("cutoff") LocalDateTime cutoff);
}
