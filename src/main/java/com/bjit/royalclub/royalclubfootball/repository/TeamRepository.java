package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    @Query("select t from Team t left join fetch t.teamPlayers tp where t.tournament.id = :tournamentId")
    List<Team> findTeamsWithPlayersByTournamentId(@Param("tournamentId") Long tournamentId);

    @Query("select t from Team t left join fetch t.teamPlayers tp where t.tournament.id in :tournamentIds")
    List<Team> findTeamsWithPlayersByTournamentIds(@Param("tournamentIds") List<Long> tournamentIds);

    /**
     * One team with everything the chat needs to authorise a caller: the squad it may admit and the
     * tournament whose status decides whether the room still exists. Both are fetched here because
     * every single chat call needs both, and a lazy load would run the membership check against a
     * detached collection.
     */
    @Query("""
            select t from Team t
            left join fetch t.teamPlayers tp
            left join fetch tp.player
            join fetch t.tournament
            where t.id = :teamId
            """)
    Optional<Team> findByIdWithPlayersAndTournament(@Param("teamId") Long teamId);

    /**
     * Teams whose chat room is still open but whose tournament has finished - exactly the rooms the
     * purge has to destroy. Driven off the tournament status rather than a date so that concluding a
     * tournament early, by hand, tears the rooms down at the same moment.
     */
    @Query("""
            select t from Team t
            join t.tournament tr
            where t.chatOpenedAt is not null
              and tr.tournamentStatus = com.bjit.royalclub.royalclubfootball.enums.TournamentStatus.CONCLUDED
            """)
    List<Team> findTeamsWithChatToPurge();

    /** Open rooms belonging to one tournament, for the purge that runs the moment it is concluded. */
    @Query("select t from Team t where t.tournament.id = :tournamentId and t.chatOpenedAt is not null")
    List<Team> findTeamsWithOpenChatByTournamentId(@Param("tournamentId") Long tournamentId);
}
