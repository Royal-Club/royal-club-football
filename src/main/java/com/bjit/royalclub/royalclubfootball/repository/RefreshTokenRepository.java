package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Loads by hash including already-revoked rows, because a revoked row is exactly what reuse
     * detection needs to see. Fetches the player and roles eagerly - the caller mints a new access
     * token from them and must not rely on an open session to do it.
     */
    @Query("SELECT t FROM RefreshToken t JOIN FETCH t.player p LEFT JOIN FETCH p.roles "
            + "WHERE t.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHash(@Param("tokenHash") String tokenHash);

    /**
     * Kills every live token a member holds. Used by reuse detection, where we know one of two
     * holders is an attacker but not which, so the safe move is to end both sessions.
     *
     * @return how many sessions were ended.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RefreshToken t SET t.revokedAt = :now "
            + "WHERE t.player.id = :playerId AND t.revokedAt IS NULL")
    int revokeAllForPlayer(@Param("playerId") Long playerId, @Param("now") LocalDateTime now);

    /**
     * Drops rows that can never be spent again. Without this the table grows by one row per refresh
     * forever; nothing reads a token past its expiry, so they are safe to delete rather than archive.
     *
     * @return how many rows were removed.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RefreshToken t WHERE t.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") LocalDateTime cutoff);
}
