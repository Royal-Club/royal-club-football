package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.model.PlayerStatisticsFilterRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerStatisticsResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentTopScorerResponse;
import com.bjit.royalclub.royalclubfootball.projection.PlayerStatisticsProjection;
import com.bjit.royalclub.royalclubfootball.projection.TournamentTopScorerProjection;
import com.bjit.royalclub.royalclubfootball.repository.MatchEventRepository;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerStatisticsServiceImpl implements PlayerStatisticsService {

    private final MatchEventRepository matchEventRepository;
    private final PlayerRepository playerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PlayerStatisticsResponse> getPlayerStatistics(PlayerStatisticsFilterRequest filterRequest) {
        log.info("Fetching player statistics with filters: {}", filterRequest);

        List<PlayerStatisticsProjection> aggregatedStats = fetchAggregatedStatistics(filterRequest);

        log.info("Found {} players with statistics", aggregatedStats.size());

        List<PlayerStatisticsResponse> playerStatsList = convertToPlayerStatistics(aggregatedStats);
        playerStatsList = applySorting(playerStatsList, filterRequest.getSortBy(), filterRequest.getSortOrder());

        List<PlayerStatisticsResponse> paginatedList = applyPagination(playerStatsList, filterRequest.getOffset(), filterRequest.getLimit());

        log.info("Returning {} players out of {} total", paginatedList.size(), playerStatsList.size());
        return paginatedList;
    }

    private List<PlayerStatisticsProjection> fetchAggregatedStatistics(PlayerStatisticsFilterRequest filterRequest) {
        log.info("Fetching stats from match_event with tournamentId: {} and position: {}",
                 filterRequest.getTournamentId(),
                 filterRequest.getPosition() != null ? filterRequest.getPosition().getDescription() : null);

        // Fetch all players (filtered by tournament only in query)
        List<PlayerStatisticsProjection> results = matchEventRepository.findAggregatedPlayerStatisticsFromEvents(
                filterRequest.getTournamentId()
        );

        // Filter by position in Java code if position filter is provided
        if (filterRequest.getPosition() != null) {
            results = results.stream()
                    .filter(projection -> {
                        Player player = playerRepository.findById(projection.getPlayerId()).orElse(null);
                        return player != null && player.getPosition() == filterRequest.getPosition();
                    })
                    .toList();
        }

        log.info("Query returned {} results from match_event table (after position filtering)", results.size());
        return results;
    }

    private List<PlayerStatisticsResponse> convertToPlayerStatistics(
            List<PlayerStatisticsProjection> aggregatedStats) {

        List<PlayerStatisticsResponse> playerStatsList = new ArrayList<>();

        for (PlayerStatisticsProjection projection : aggregatedStats) {
            PlayerStatisticsResponse playerStats = createPlayerStatistics(projection);
            if (playerStats != null) {
                playerStatsList.add(playerStats);
            }
        }

        return playerStatsList;
    }

    private PlayerStatisticsResponse createPlayerStatistics(PlayerStatisticsProjection projection) {
        Long playerId = projection.getPlayerId();
        Player player = playerRepository.findById(playerId).orElse(null);

        if (player == null) {
            return null;
        }

        String positionDescription = player.getPosition() != null ? player.getPosition().getDescription() : "Unassigned";

        return PlayerStatisticsResponse.builder()
                .playerId(playerId)
                .playerName(player.getName())
                .position(positionDescription)
                .statistics(buildStatisticsSummary(projection))
                .build();
    }


    private PlayerStatisticsResponse.StatisticsSummary buildStatisticsSummary(PlayerStatisticsProjection projection) {
        long goalsScored = projection.getGoalsScored() != null ? projection.getGoalsScored() : 0L;
        long assists = projection.getAssists() != null ? projection.getAssists() : 0L;
        long matchesPlayed = projection.getMatchesPlayed() != null ? projection.getMatchesPlayed() : 0L;
        long yellowCards = projection.getYellowCards() != null ? projection.getYellowCards() : 0L;
        long redCards = projection.getRedCards() != null ? projection.getRedCards() : 0L;

        return PlayerStatisticsResponse.StatisticsSummary.builder()
                .matchesPlayed((int) matchesPlayed)
                .goalsScored((int) goalsScored)
                .assists((int) assists)
                .goalsAndAssists((int) (goalsScored + assists))
                .yellowCards((int) yellowCards)
                .redCards((int) redCards)
                .build();
    }


    private List<PlayerStatisticsResponse> applyPagination(
            List<PlayerStatisticsResponse> players,
            int offset,
            int limit) {

        if (offset >= players.size()) {
            return new ArrayList<>();
        }

        int toIndex = Math.min(offset + limit, players.size());
        return players.subList(offset, toIndex);
    }


    private List<PlayerStatisticsResponse> applySorting(List<PlayerStatisticsResponse> players, String sortBy, String sortOrder) {
        Comparator<PlayerStatisticsResponse> comparator = createComparator(sortBy);

        if ("ASC".equalsIgnoreCase(sortOrder)) {
            return players.stream().sorted(comparator).toList();
        }
        return players.stream().sorted(comparator.reversed()).toList();
    }

    private Comparator<PlayerStatisticsResponse> createComparator(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "assists" -> Comparator.comparing(p -> p.getStatistics().getAssists());
            case "goalsassists", "goals+assists" -> Comparator.comparing(p -> p.getStatistics().getGoalsAndAssists());
            case "matches" -> Comparator.comparing(p -> p.getStatistics().getMatchesPlayed());
            default -> Comparator.comparing(p -> p.getStatistics().getGoalsScored());
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<TournamentTopScorerResponse> getTopScorersByTournament(Long tournamentId, Integer limit) {
        log.info("Fetching top {} scorers for tournament: {}", limit, tournamentId);

        List<TournamentTopScorerProjection> topScorers = matchEventRepository.findTopScorersByTournament(tournamentId);

        List<TournamentTopScorerResponse> result = topScorers.stream()
                .limit(limit)
                .map(this::mapToTopScorerResponse)
                .toList();

        log.info("Returning {} top scorers for tournament {}", result.size(), tournamentId);
        return result;
    }

    private TournamentTopScorerResponse mapToTopScorerResponse(TournamentTopScorerProjection projection) {
        return TournamentTopScorerResponse.builder()
                .playerId(projection.getPlayerId())
                .playerName(projection.getPlayerName())
                .teamId(projection.getTeamId())
                .teamName(projection.getTeamName())
                .position(projection.getPosition())
                .goalsScored(projection.getGoalsScored() != null ? projection.getGoalsScored().intValue() : 0)
                .assists(projection.getAssists() != null ? projection.getAssists().intValue() : 0)
                .matchesPlayed(projection.getMatchesPlayed() != null ? projection.getMatchesPlayed().intValue() : 0)
                .build();
    }
}
