package com.bjit.royalclub.royalclubfootball.controller.auction;

import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.model.TeamRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamResponse;
import com.bjit.royalclub.royalclubfootball.model.auction.TeamBudgetRequest;
import com.bjit.royalclub.royalclubfootball.model.auction.TeamBudgetResponse;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import com.bjit.royalclub.royalclubfootball.service.TeamManagementService;
import com.bjit.royalclub.royalclubfootball.service.auction.TeamBudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("tournaments/{tournamentId}/auction/team-budgets")
@RequiredArgsConstructor
public class TeamBudgetController {

    private final TeamBudgetService teamBudgetService;
    private final TeamRepository teamRepository;
    private final TeamManagementService teamManagementService;

    @GetMapping
    public ResponseEntity<List<TeamBudgetResponse>> getTeamBudgets(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(teamBudgetService.getTeamBudgets(tournamentId));
    }

    @GetMapping("/available-teams")
    public ResponseEntity<List<TeamResponse>> getAvailableTeams(@PathVariable Long tournamentId) {
        List<Team> teams = teamRepository.findTeamsWithPlayersByTournamentId(tournamentId);
        List<TeamResponse> response = teams.stream()
                .map(t -> TeamResponse.builder()
                        .teamId(t.getId())
                        .teamName(t.getTeamName())
                        .tournamentId(tournamentId)
                        .build())
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-team")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<TeamResponse> createTeamForAuction(
            @PathVariable Long tournamentId,
            @RequestBody @Valid TeamRequest request) {
        request.setTournamentId(tournamentId);
        TeamResponse team = teamManagementService.createOrUpdateTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(team);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<TeamBudgetResponse> createTeamBudget(
            @PathVariable Long tournamentId,
            @Valid @RequestBody TeamBudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamBudgetService.createTeamBudget(tournamentId, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<TeamBudgetResponse> updateTeamBudget(
            @PathVariable Long tournamentId,
            @PathVariable Long id,
            @Valid @RequestBody TeamBudgetRequest request) {
        return ResponseEntity.ok(teamBudgetService.updateTeamBudget(tournamentId, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> deleteTeamBudget(
            @PathVariable Long tournamentId,
            @PathVariable Long id) {
        teamBudgetService.deleteTeamBudget(tournamentId, id);
        return ResponseEntity.noContent().build();
    }
}
