package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.entity.Match;
import com.bjit.royalclub.royalclubfootball.model.MatchStatisticsResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentStandingResponse;
import com.bjit.royalclub.royalclubfootball.repository.MatchRepository;
import com.bjit.royalclub.royalclubfootball.service.MatchStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/statistics")
public class StatisticsController {

    private final MatchStatisticsService matchStatisticsService;
    private final MatchRepository matchRepository;

    /**
     * Get tournament standings (teams ranked by points, goal difference, etc.)
     */
    @GetMapping("/tournaments/{tournamentId}/standings")
    public ResponseEntity<Object> getTournamentStandings(@PathVariable Long tournamentId) {
        List<TournamentStandingResponse> standings = matchStatisticsService.getTournamentStandings(tournamentId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, standings);
    }

    /**
     * Get top scorers in a tournament
     */
    @GetMapping("/tournaments/{tournamentId}/top-scorers")
    public ResponseEntity<Object> getTopScorers(@PathVariable Long tournamentId) {
        List<MatchStatisticsResponse> topScorers = matchStatisticsService.getTopScorersByTournament(tournamentId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, topScorers);
    }

    /**
     * Get top assist providers in a tournament
     */
    @GetMapping("/tournaments/{tournamentId}/top-assists")
    public ResponseEntity<Object> getTopAssists(@PathVariable Long tournamentId) {
        List<MatchStatisticsResponse> topAssists = matchStatisticsService.getTopAssistProvidersByTournament(tournamentId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, topAssists);
    }

    /**
     * Get players with most disciplinary cards (red + yellow) in a tournament
     */
    @GetMapping("/tournaments/{tournamentId}/top-cards")
    public ResponseEntity<Object> getTopCards(@PathVariable Long tournamentId) {
        List<MatchStatisticsResponse> topCards = matchStatisticsService.getTopCardReceiversByTournament(tournamentId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, topCards);
    }

    /**
     * Get all player statistics for a tournament
     */
    @GetMapping("/tournaments/{tournamentId}/players/{playerId}")
    public ResponseEntity<Object> getPlayerTournamentStatistics(
            @PathVariable Long tournamentId,
            @PathVariable Long playerId) {
        List<MatchStatisticsResponse> statistics = matchStatisticsService.getPlayerTournamentStatistics(tournamentId, playerId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, statistics);
    }

    /**
     * Get all player statistics for a specific match
     */
    @GetMapping("/matches/{matchId}")
    public ResponseEntity<Object> getMatchStatistics(@PathVariable Long matchId) {
        List<MatchStatisticsResponse> statistics = matchStatisticsService.getMatchStatistics(matchId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, statistics);
    }

    /**
     * Get specific player's statistics in a specific match
     */
    @GetMapping("/matches/{matchId}/players/{playerId}")
    public ResponseEntity<Object> getPlayerMatchStatistics(
            @PathVariable Long matchId,
            @PathVariable Long playerId) {
        MatchStatisticsResponse statistics = matchStatisticsService.getPlayerMatchStatistics(matchId, playerId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, statistics);
    }

    /**
     * Get team statistics for a specific match
     */
    @GetMapping("/matches/{matchId}/teams/{teamId}")
    public ResponseEntity<Object> getTeamMatchStatistics(
            @PathVariable Long matchId,
            @PathVariable Long teamId) {
        List<MatchStatisticsResponse> statistics = matchStatisticsService.getTeamMatchStatistics(matchId, teamId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, statistics);
    }

    /**
     * Get total goals scored by a team in a tournament
     */
    @GetMapping("/tournaments/{tournamentId}/teams/{teamId}/goals")
    public ResponseEntity<Object> getTeamTotalGoals(
            @PathVariable Long tournamentId,
            @PathVariable Long teamId) {
        Long totalGoals = matchStatisticsService.getTeamTotalGoalsInTournament(tournamentId, teamId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, totalGoals);
    }

    /**
     * Get total disciplinary cards for a player in a tournament
     */
    @GetMapping("/tournaments/{tournamentId}/players/{playerId}/cards")
    public ResponseEntity<Object> getPlayerTotalCards(
            @PathVariable Long tournamentId,
            @PathVariable Long playerId) {
        Integer totalCards = matchStatisticsService.getPlayerTotalDisciplinaryCardsInTournament(tournamentId, playerId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, totalCards);
    }

    /**
     * Aggregate statistics for all matches in a tournament
     * This is a utility endpoint to process existing matches that don't have statistics yet
     * Only ADMIN and COORDINATOR can trigger this operation
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @PostMapping("/tournaments/{tournamentId}/aggregate")
    public ResponseEntity<Object> aggregateTournamentStatistics(@PathVariable Long tournamentId) {
        // Get all matches for this tournament
        List<Match> matches = matchRepository.findByTournamentId(tournamentId);

        int successCount = 0;
        int errorCount = 0;

        // Aggregate statistics for each match
        for (Match match : matches) {
            try {
                matchStatisticsService.aggregateMatchStatistics(match.getId());
                successCount++;
            } catch (Exception e) {
                errorCount++;
                System.err.println("Failed to aggregate statistics for match " + match.getId() + ": " + e.getMessage());
            }
        }

        String message = String.format("Aggregated statistics for %d matches (%d successful, %d failed)",
                matches.size(), successCount, errorCount);

        return buildSuccessResponse(HttpStatus.OK, message);
    }
}
