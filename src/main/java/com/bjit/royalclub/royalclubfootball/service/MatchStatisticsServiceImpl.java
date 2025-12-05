package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Match;
import com.bjit.royalclub.royalclubfootball.entity.MatchEvent;
import com.bjit.royalclub.royalclubfootball.entity.MatchStatistics;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.enums.MatchStatus;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.model.MatchStatisticsResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentStandingResponse;
import com.bjit.royalclub.royalclubfootball.repository.MatchEventRepository;
import com.bjit.royalclub.royalclubfootball.repository.MatchRepository;
import com.bjit.royalclub.royalclubfootball.repository.MatchStatisticsRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchStatisticsServiceImpl implements MatchStatisticsService {

    private final MatchStatisticsRepository matchStatisticsRepository;
    private final MatchEventRepository matchEventRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;

    @Override
    public MatchStatisticsResponse getPlayerMatchStatistics(Long matchId, Long playerId) {
        MatchStatistics stats = matchStatisticsRepository.findByMatchIdAndPlayerId(matchId, playerId)
                .orElseThrow(() -> new TournamentServiceException("Statistics not found", HttpStatus.NOT_FOUND));

        return convertToResponse(stats);
    }

    @Override
    public List<MatchStatisticsResponse> getMatchStatistics(Long matchId) {
        List<MatchStatistics> stats = matchStatisticsRepository.findByMatchId(matchId);
        return stats.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MatchStatisticsResponse> getTeamMatchStatistics(Long matchId, Long teamId) {
        List<MatchStatistics> stats = matchStatisticsRepository.findByMatchIdAndTeamId(matchId, teamId);
        return stats.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MatchStatisticsResponse> getPlayerTournamentStatistics(Long tournamentId, Long playerId) {
        List<MatchStatistics> stats = matchStatisticsRepository.findPlayerStatsByTournament(tournamentId, playerId);
        return stats.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MatchStatisticsResponse> getTopScorersByTournament(Long tournamentId) {
        List<MatchStatistics> stats = matchStatisticsRepository.findByTournamentId(tournamentId);

        Map<Long, MatchStatistics> aggregated = new HashMap<>();
        for (MatchStatistics stat : stats) {
            Long playerId = stat.getPlayer().getId();
            aggregated.computeIfPresent(playerId, (k, v) -> {
                v.setGoalsScored(v.getGoalsScored() + stat.getGoalsScored());
                return v;
            });
            aggregated.putIfAbsent(playerId, stat);
        }

        return aggregated.values().stream()
                .sorted((a, b) -> Integer.compare(b.getGoalsScored(), a.getGoalsScored()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchStatisticsResponse> getTopAssistProvidersByTournament(Long tournamentId) {
        List<MatchStatistics> stats = matchStatisticsRepository.findByTournamentId(tournamentId);

        Map<Long, MatchStatistics> aggregated = new HashMap<>();
        for (MatchStatistics stat : stats) {
            Long playerId = stat.getPlayer().getId();
            aggregated.computeIfPresent(playerId, (k, v) -> {
                v.setAssists(v.getAssists() + stat.getAssists());
                return v;
            });
            aggregated.putIfAbsent(playerId, stat);
        }

        return aggregated.values().stream()
                .sorted((a, b) -> Integer.compare(b.getAssists(), a.getAssists()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TournamentStandingResponse> getTournamentStandings(Long tournamentId) {
        List<Match> matches = matchRepository.findByTournamentIdAndStatus(tournamentId, MatchStatus.COMPLETED);
        List<Team> teams = teamRepository.findTeamsWithPlayersByTournamentId(tournamentId);

        Map<Long, TournamentStandingResponse> standings = new HashMap<>();

        for (Team team : teams) {
            standings.put(team.getId(), TournamentStandingResponse.builder()
                    .teamId(team.getId())
                    .teamName(team.getTeamName())
                    .points(0)
                    .goalsFor(0)
                    .goalsAgainst(0)
                    .matches(0)
                    .wins(0)
                    .draws(0)
                    .losses(0)
                    .goalDifference(0)
                    .build());
        }

        for (Match match : matches) {
            Long homeTeamId = match.getHomeTeam().getId();
            Long awayTeamId = match.getAwayTeam().getId();
            Integer homeScore = match.getHomeTeamScore();
            Integer awayScore = match.getAwayTeamScore();

            TournamentStandingResponse homeStanding = standings.get(homeTeamId);
            TournamentStandingResponse awayStanding = standings.get(awayTeamId);

            if (homeStanding != null && awayStanding != null) {
                homeStanding.setGoalsFor(homeStanding.getGoalsFor() + homeScore);
                homeStanding.setGoalsAgainst(homeStanding.getGoalsAgainst() + awayScore);
                homeStanding.setMatches(homeStanding.getMatches() + 1);

                awayStanding.setGoalsFor(awayStanding.getGoalsFor() + awayScore);
                awayStanding.setGoalsAgainst(awayStanding.getGoalsAgainst() + homeScore);
                awayStanding.setMatches(awayStanding.getMatches() + 1);

                if (homeScore > awayScore) {
                    homeStanding.setPoints(homeStanding.getPoints() + 3);
                    homeStanding.setWins(homeStanding.getWins() + 1);
                    awayStanding.setLosses(awayStanding.getLosses() + 1);
                } else if (homeScore < awayScore) {
                    awayStanding.setPoints(awayStanding.getPoints() + 3);
                    awayStanding.setWins(awayStanding.getWins() + 1);
                    homeStanding.setLosses(homeStanding.getLosses() + 1);
                } else {
                    homeStanding.setPoints(homeStanding.getPoints() + 1);
                    homeStanding.setDraws(homeStanding.getDraws() + 1);
                    awayStanding.setPoints(awayStanding.getPoints() + 1);
                    awayStanding.setDraws(awayStanding.getDraws() + 1);
                }
            }
        }

        return standings.values().stream()
                .peek(response -> response.setGoalDifference(response.getGoalsFor() - response.getGoalsAgainst()))
                .sorted((a, b) -> {
                    int pointDiff = Integer.compare(b.getPoints(), a.getPoints());
                    if (pointDiff != 0) return pointDiff;
                    return Integer.compare(b.getGoalDifference(), a.getGoalDifference());
                })
                .collect(Collectors.toList());
    }

    @Override
    public void aggregateMatchStatistics(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new TournamentServiceException("Match not found", HttpStatus.NOT_FOUND));

        List<MatchEvent> matchEvents = matchEventRepository.findByMatchId(matchId);

        Map<Long, MatchStatistics> playerStats = new HashMap<>();

        for (MatchEvent event : matchEvents) {
            Long playerId = event.getPlayer().getId();
            MatchStatistics stats = playerStats.getOrDefault(playerId, MatchStatistics.builder()
                    .match(match)
                    .player(event.getPlayer())
                    .team(event.getTeam())
                    .goalsScored(0)
                    .assists(0)
                    .redCards(0)
                    .yellowCards(0)
                    .substitutionIn(0)
                    .substitutionOut(0)
                    .minutesPlayed(0)
                    .build());

            switch (event.getEventType()) {
                case GOAL -> stats.setGoalsScored(stats.getGoalsScored() + 1);
                case ASSIST -> stats.setAssists(stats.getAssists() + 1);
                case RED_CARD -> stats.setRedCards(stats.getRedCards() + 1);
                case YELLOW_CARD -> stats.setYellowCards(stats.getYellowCards() + 1);
                case SUBSTITUTION -> {
                    if ("IN".equals(event.getDescription())) {
                        stats.setSubstitutionIn(stats.getSubstitutionIn() + 1);
                    } else {
                        stats.setSubstitutionOut(stats.getSubstitutionOut() + 1);
                    }
                }
                default -> {
                }
            }

            playerStats.put(playerId, stats);
        }

        matchStatisticsRepository.saveAll(playerStats.values());
    }

    @Override
    public Long getTeamTotalGoalsInTournament(Long tournamentId, Long teamId) {
        Long totalGoals = matchStatisticsRepository.getTotalGoalsByTeamInTournament(tournamentId, teamId);
        return totalGoals != null ? totalGoals : 0L;
    }

    @Override
    public Integer getPlayerTotalDisciplinaryCardsInTournament(Long tournamentId, Long playerId) {
        Integer redCards = matchStatisticsRepository.getTotalRedCardsByPlayerInTournament(tournamentId, playerId);
        return redCards != null ? redCards : 0;
    }

    private MatchStatisticsResponse convertToResponse(MatchStatistics stats) {
        return MatchStatisticsResponse.builder()
                .id(stats.getId())
                .matchId(stats.getMatch().getId())
                .playerId(stats.getPlayer().getId())
                .playerName(stats.getPlayer().getName())
                .teamId(stats.getTeam().getId())
                .teamName(stats.getTeam().getTeamName())
                .goalsScored(stats.getGoalsScored())
                .assists(stats.getAssists())
                .redCards(stats.getRedCards())
                .yellowCards(stats.getYellowCards())
                .substitutionIn(stats.getSubstitutionIn())
                .substitutionOut(stats.getSubstitutionOut())
                .minutesPlayed(stats.getMinutesPlayed())
                .build();
    }

}
