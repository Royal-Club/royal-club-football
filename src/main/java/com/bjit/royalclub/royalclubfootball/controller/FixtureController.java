package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.entity.Match;
import com.bjit.royalclub.royalclubfootball.model.FixtureGenerationRequest;
import com.bjit.royalclub.royalclubfootball.model.MatchFixtureUpdateRequest;
import com.bjit.royalclub.royalclubfootball.model.MatchResponse;
import com.bjit.royalclub.royalclubfootball.model.Response;
import com.bjit.royalclub.royalclubfootball.repository.MatchRepository;
import com.bjit.royalclub.royalclubfootball.service.FixtureGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Legacy Fixture Controller
 * 
 * This controller handles the old auto-fixture generation system.
 * For new tournaments, use the Manual Fixture System instead:
 * - Tournament Rounds: /api/rounds
 * - Round Groups: /api/groups
 * - Logic Nodes: /api/logic-nodes
 * 
 * @deprecated For new implementations, prefer the Manual Fixture System
 * @see TournamentRoundController
 * @see RoundGroupController
 * @see LogicNodeController
 */
@RestController
@RequestMapping("/fixtures")
@RequiredArgsConstructor
@Validated
public class FixtureController {

    private final FixtureGenerationService fixtureGenerationService;
    private final MatchRepository matchRepository;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> generateFixtures(@Valid @RequestBody FixtureGenerationRequest request) {
        List<Match> matches = fixtureGenerationService.generateFixtures(request.getTournamentId(), request);
        List<MatchResponse> responses = matches.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .status("CREATED")
                        .message("Fixtures generated successfully")
                        .content(responses)
                        .numberOfElement(responses.size())
                        .timeStamp(System.currentTimeMillis())
                        .build());
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<Response> getFixturesByTournament(@PathVariable Long tournamentId) {
        List<Match> matches = matchRepository.findByTournamentId(tournamentId);

        if (matches.isEmpty()) {
            return ResponseEntity.ok(Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .status("OK")
                    .message("No fixtures found for this tournament")
                    .content(matches)
                    .numberOfElement(0)
                    .timeStamp(System.currentTimeMillis())
                    .build());
        }

        List<MatchResponse> responses = matches.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Response.builder()
                .statusCode(HttpStatus.OK.value())
                .status("OK")
                .message("Fixtures retrieved successfully")
                .content(responses)
                .numberOfElement(responses.size())
                .timeStamp(System.currentTimeMillis())
                .build());
    }

    @DeleteMapping("/tournament/{tournamentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> clearFixtures(@PathVariable Long tournamentId) {
        fixtureGenerationService.clearFixtures(tournamentId);
        return ResponseEntity.ok(Response.builder()
                .statusCode(HttpStatus.OK.value())
                .status("OK")
                .message("Fixtures cleared successfully")
                .timeStamp(System.currentTimeMillis())
                .build());
    }

    @PutMapping("/{matchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> updateMatchFixture(@PathVariable Long matchId, @Valid @RequestBody MatchFixtureUpdateRequest request) {
        Match updatedMatch = fixtureGenerationService.updateMatchFixture(matchId, request);
        MatchResponse response = convertToResponse(updatedMatch);

        return ResponseEntity.ok(Response.builder()
                .statusCode(HttpStatus.OK.value())
                .status("OK")
                .message("Match fixture updated successfully")
                .content(response)
                .timeStamp(System.currentTimeMillis())
                .build());
    }

    private MatchResponse convertToResponse(Match match) {
        return MatchResponse.builder()
                .id(match.getId())
                .tournamentId(match.getTournament().getId())
                .tournamentName(match.getTournament().getName())
                .homeTeamId(match.getHomeTeam().getId())
                .homeTeamName(match.getHomeTeam().getTeamName())
                .awayTeamId(match.getAwayTeam().getId())
                .awayTeamName(match.getAwayTeam().getTeamName())
                .venueId(match.getVenue() != null ? match.getVenue().getId() : null)
                .venueName(match.getVenue() != null ? match.getVenue().getName() : null)
                .matchDate(match.getMatchDate())
                .matchStatus(match.getMatchStatus().toString())
                .matchOrder(match.getMatchOrder())
                .round(match.getLegacyRound())
                .groupName(match.getGroupName())
                .homeTeamScore(match.getHomeTeamScore())
                .awayTeamScore(match.getAwayTeamScore())
                .matchDurationMinutes(match.getMatchDurationMinutes())
                .elapsedTimeSeconds(match.getElapsedTimeSeconds())
                .startedAt(match.getStartedAt())
                .completedAt(match.getCompletedAt())
                .createdDate(match.getCreatedDate())
                .updatedDate(match.getUpdatedDate())
                .build();
    }

}
