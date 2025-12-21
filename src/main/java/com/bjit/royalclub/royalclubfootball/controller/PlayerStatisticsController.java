package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.enums.FootballPosition;
import com.bjit.royalclub.royalclubfootball.model.PlayerStatisticsFilterRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerStatisticsResponse;
import com.bjit.royalclub.royalclubfootball.service.PlayerStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/player-statistics")
public class PlayerStatisticsController {

    private final PlayerStatisticsService playerStatisticsService;

    /**
     * Get player statistics with optional filters
     * Requires authentication
     *
     * @param tournamentId Filter by tournament participation (optional, null = all players from all tournaments)
     *                     When provided, returns only players who participated in teams for that tournament
     * @param position     Filter by player position (optional)
     * @param sortBy       Sort field: goals, assists, goalsAssists, matches (default: goalsAssists)
     * @param sortOrder    ASC or DESC (default: DESC)
     * @param limit        Limit results (default: 100)
     * @param offset       Pagination offset (default: 0)
     * @return List of PlayerStatisticsResponse with filtered and sorted player statistics
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<Object> getPlayerStatistics(
            @RequestParam(required = false) Long tournamentId,
            @RequestParam(required = false) FootballPosition position,
            @RequestParam(required = false, defaultValue = "goalsAssists") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortOrder,
            @RequestParam(required = false, defaultValue = "100") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset
    ) {
        PlayerStatisticsFilterRequest filterRequest = PlayerStatisticsFilterRequest.builder()
                .tournamentId(tournamentId)
                .position(position)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .limit(limit)
                .offset(offset)
                .build();

        List<PlayerStatisticsResponse> response = playerStatisticsService.getPlayerStatistics(filterRequest);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, response);
    }
}
