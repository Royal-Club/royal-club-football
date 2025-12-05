package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.MatchStatisticsResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentStandingResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface MatchStatisticsService {

    /**
     * Get statistics for a specific player in a specific match
     */
    MatchStatisticsResponse getPlayerMatchStatistics(Long matchId, Long playerId);

    /**
     * Get all player statistics for a specific match
     */
    List<MatchStatisticsResponse> getMatchStatistics(Long matchId);

    /**
     * Get all player statistics for a team in a specific match
     */
    List<MatchStatisticsResponse> getTeamMatchStatistics(Long matchId, Long teamId);

    /**
     * Get all statistics for a player across all matches in a tournament
     */
    List<MatchStatisticsResponse> getPlayerTournamentStatistics(Long tournamentId, Long playerId);

    /**
     * Get tournament-wide top scorers
     */
    List<MatchStatisticsResponse> getTopScorersByTournament(Long tournamentId);

    /**
     * Get tournament-wide top assist providers
     */
    List<MatchStatisticsResponse> getTopAssistProvidersByTournament(Long tournamentId);

    /**
     * Get tournament standings (points, goals, etc.)
     */
    List<TournamentStandingResponse> getTournamentStandings(Long tournamentId);

    /**
     * Aggregate statistics from match events to update player statistics
     */
    @Transactional
    void aggregateMatchStatistics(Long matchId);

    /**
     * Get total goals scored by a team in a tournament
     */
    Long getTeamTotalGoalsInTournament(Long tournamentId, Long teamId);

    /**
     * Get total disciplinary cards for a player in a tournament
     */
    Integer getPlayerTotalDisciplinaryCardsInTournament(Long tournamentId, Long playerId);

}
