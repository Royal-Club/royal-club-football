package com.bjit.royalclub.royalclubfootball.service.auction;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.auction.TeamBudget;
import com.bjit.royalclub.royalclubfootball.exception.ResourceNotFoundException;
import com.bjit.royalclub.royalclubfootball.model.auction.TeamBudgetRequest;
import com.bjit.royalclub.royalclubfootball.model.auction.TeamBudgetResponse;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import com.bjit.royalclub.royalclubfootball.repository.auction.TeamBudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamBudgetServiceImpl implements TeamBudgetService {

    private final TeamBudgetRepository teamBudgetRepository;
    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    @Override
    public List<TeamBudgetResponse> getTeamBudgets(Long tournamentId) {
        return teamBudgetRepository.findByTournamentId(tournamentId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TeamBudgetResponse createTeamBudget(Long tournamentId, TeamBudgetRequest request) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found: " + tournamentId));

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + request.getTeamId()));

        Player owner = playerRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found: " + request.getOwnerId()));

        if (teamBudgetRepository.findByTournamentIdAndTeamId(tournamentId, request.getTeamId()).isPresent()) {
            throw new IllegalStateException("Budget already exists for this team in this tournament");
        }

        TeamBudget budget = TeamBudget.builder()
                .tournament(tournament)
                .team(team)
                .owner(owner)
                .totalBudget(request.getTotalBudget())
                .remainingBudget(request.getTotalBudget())
                .totalSpent(0)
                .playersBought(0)
                .build();

        budget = teamBudgetRepository.save(budget);
        return mapToResponse(budget);
    }

    @Override
    @Transactional
    public TeamBudgetResponse updateTeamBudget(Long tournamentId, Long id, TeamBudgetRequest request) {
        TeamBudget budget = teamBudgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team budget not found: " + id));

        if (!budget.getTournament().getId().equals(tournamentId)) {
            throw new IllegalStateException("Budget does not belong to this tournament");
        }

        if (request.getTotalBudget() != null) {
            int difference = request.getTotalBudget() - budget.getTotalBudget();
            budget.setTotalBudget(request.getTotalBudget());
            budget.setRemainingBudget(budget.getRemainingBudget() + difference);
        }

        if (request.getOwnerId() != null) {
            Player owner = playerRepository.findById(request.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Owner not found: " + request.getOwnerId()));
            budget.setOwner(owner);
        }

        budget = teamBudgetRepository.save(budget);
        return mapToResponse(budget);
    }

    @Override
    @Transactional
    public void deleteTeamBudget(Long tournamentId, Long id) {
        TeamBudget budget = teamBudgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team budget not found: " + id));
        if (!budget.getTournament().getId().equals(tournamentId)) {
            throw new IllegalStateException("Budget does not belong to this tournament");
        }
        if (budget.getPlayersBought() > 0) {
            throw new IllegalStateException("Cannot delete a team budget with players already bought");
        }
        teamBudgetRepository.delete(budget);
    }

    private TeamBudgetResponse mapToResponse(TeamBudget budget) {
        return TeamBudgetResponse.builder()
                .id(budget.getId())
                .tournamentId(budget.getTournament().getId())
                .teamId(budget.getTeam().getId())
                .teamName(budget.getTeam().getTeamName())
                .ownerId(budget.getOwner().getId())
                .ownerName(budget.getOwner().getName())
                .totalBudget(budget.getTotalBudget())
                .remainingBudget(budget.getRemainingBudget())
                .totalSpent(budget.getTotalSpent())
                .playersBought(budget.getPlayersBought())
                .build();
    }
}
