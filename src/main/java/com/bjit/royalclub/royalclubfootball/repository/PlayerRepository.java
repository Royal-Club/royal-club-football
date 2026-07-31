package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByEmail(String email);

    Optional<Player> findByEmailAndIsActiveTrue(String email);

    @Query("SELECT p FROM Player p LEFT JOIN FETCH p.roles WHERE p.email = :email AND p.isActive = true")
    Optional<Player> findByEmailAndIsActiveTrueWithRoles(@Param("email") String email);

    @Query("SELECT DISTINCT p FROM Player p LEFT JOIN FETCH p.roles WHERE p.id IN :ids")
    List<Player> findAllByIdWithRoles(@Param("ids") Collection<Long> ids);

    /**
     * Paginated IDs only. Pair with {@link #findAllByIdWithRoles} to page players whose roles are
     * needed: a {@code JOIN FETCH} on the collection cannot be paged in SQL, so Hibernate would
     * otherwise fall back to loading every row and paginating in memory.
     */
    @Query("SELECT p.id FROM Player p")
    Page<Long> findAllPlayerIds(Pageable pageable);

    int countByIsActiveTrue();

    /**
     * Active players who have NOT responded (no tournament_participant row) for the given tournament.
     * These are the players that should be reminded to confirm attendance.
     */
    @Query("SELECT p FROM Player p WHERE p.isActive = true AND p.id NOT IN "
            + "(SELECT tp.player.id FROM TournamentParticipant tp WHERE tp.tournament.id = :tournamentId)")
    List<Player> findActivePlayersWithoutParticipation(@Param("tournamentId") Long tournamentId);

    /**
     * Active players with no collection recorded against the month spanning [start, end].
     * Any collection at all clears a player for that month — the club has no per-player expected
     * amount, so a partial payment counts as paid.
     */
    @Query("SELECT p FROM Player p WHERE p.isActive = true AND p.id NOT IN "
            + "(SELECT pl.id FROM AcCollection c JOIN c.players pl "
            + "WHERE c.monthOfPayment BETWEEN :start AND :end)")
    List<Player> findActivePlayersWithoutCollectionForMonth(@Param("start") LocalDate start,
                                                            @Param("end") LocalDate end);
}
