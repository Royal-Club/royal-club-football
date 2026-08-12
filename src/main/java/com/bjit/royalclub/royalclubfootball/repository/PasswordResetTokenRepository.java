package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.PasswordResetToken;
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
     * How many links this member has been sent since {@code since} - the rolling-window quota.
     * Counts links sent, not resets completed, so a member cannot drain the club's daily mail
     * allowance by requesting links they never use.
     */
    int countByPlayerIdAndSentAtAfter(Long playerId, LocalDateTime since);

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
}
