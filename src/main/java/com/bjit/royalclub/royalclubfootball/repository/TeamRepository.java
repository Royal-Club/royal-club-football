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
     * The id of the team this player is on in this tournament, if any.
     *
     * <p>Returns the id rather than the entity, and joins nothing, on purpose. The caller needs the
     * full fetch graph anyway and re-reads it through the access service; the alternative -
     * loading every team in the tournament with its squad and filtering in memory - costs a query
     * per squad member, because {@code TeamPlayer.player} is a plain {@code @ManyToOne} and so
     * eager. That is a lot of round trips to answer "which team am I on".
     */
    @Query("""
            select t.id from Team t
            join t.teamPlayers tp
            where t.tournament.id = :tournamentId
              and tp.player.id = :playerId
            """)
    Optional<Long> findTeamIdOfPlayerInTournament(@Param("tournamentId") Long tournamentId,
                                                  @Param("playerId") Long playerId);

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

    /**
     * Ids of the teams whose open room this player is in, newest room first.
     *
     * <p>Backs the dock that follows the player around the site, which has no tournament in its URL
     * to scope by. Concluded tournaments are excluded here rather than left to the access service:
     * this asks "is there anything to open", so a room the purge is about to destroy must not count.
     *
     * <p>A list rather than a single id, even though one open room at a time is the normal case -
     * two tournaments can overlap, and silently picking one of them inside a query would make the
     * dock point at the wrong squad with nothing to explain why.
     */
    @Query("""
            select t.id from Team t
            join t.teamPlayers tp
            join t.tournament tr
            where tp.player.id = :playerId
              and t.chatOpenedAt is not null
              and tr.tournamentStatus <> com.bjit.royalclub.royalclubfootball.enums.TournamentStatus.CONCLUDED
            order by t.chatOpenedAt desc
            """)
    List<Long> findOpenChatTeamIdsOfPlayer(@Param("playerId") Long playerId);

    /** Open rooms belonging to one tournament, for the purge that runs the moment it is concluded. */
    @Query("select t from Team t where t.tournament.id = :tournamentId and t.chatOpenedAt is not null")
    List<Team> findTeamsWithOpenChatByTournamentId(@Param("tournamentId") Long tournamentId);
}
