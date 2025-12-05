package com.bjit.royalclub.royalclubfootball.service.validation;

import com.bjit.royalclub.royalclubfootball.entity.Match;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.enums.MatchStatus;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FixtureValidationService {

    public void validateTeamsForFixtureGeneration(List<Team> teams, String type) {
        if (teams == null || teams.isEmpty()) {
            throw new TournamentServiceException("No teams found in tournament", HttpStatus.BAD_REQUEST);
        }

        if (teams.size() < 2) {
            throw new TournamentServiceException("Minimum 2 teams required for fixture generation", HttpStatus.BAD_REQUEST);
        }

        switch (type.toUpperCase()) {
            case "KNOCKOUT_SINGLE", "KNOCKOUT_DOUBLE" -> validateEliminationFormat(teams.size());
            case "GROUP_STAGE_KNOCKOUT" -> validateGroupStageFormat(teams.size());
            case "ROUND_ROBIN" -> validateRoundRobinFormat(teams.size());
        }
    }

    private void validateEliminationFormat(int teamCount) {
        if (teamCount < 2) {
            throw new TournamentServiceException("Elimination tournaments require minimum 2 teams", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateGroupStageFormat(int teamCount) {
        if (teamCount < 4) {
            throw new TournamentServiceException("Group stage tournaments require minimum 4 teams", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateRoundRobinFormat(int teamCount) {
        if (teamCount < 2) {
            throw new TournamentServiceException("Round-robin tournaments require minimum 2 teams", HttpStatus.BAD_REQUEST);
        }
    }

    public void validateMatchForStateTransition(Match match, MatchStatus targetStatus) {
        switch (targetStatus) {
            case ONGOING:
                if (!match.getMatchStatus().equals(MatchStatus.SCHEDULED)) {
                    throw new TournamentServiceException(
                            "Only scheduled matches can be started",
                            HttpStatus.CONFLICT
                    );
                }
                break;
            case COMPLETED:
                if (!match.getMatchStatus().equals(MatchStatus.ONGOING)) {
                    throw new TournamentServiceException(
                            "Only ongoing matches can be completed",
                            HttpStatus.CONFLICT
                    );
                }
                break;
            case SCHEDULED:
                throw new TournamentServiceException(
                        "Cannot transition to scheduled status",
                        HttpStatus.BAD_REQUEST
                );
        }
    }

    public void validateMatchEventData(Long playerId, Long teamId, Long matchId) {
        if (playerId == null || teamId == null || matchId == null) {
            throw new TournamentServiceException(
                    "Player ID, Team ID, and Match ID are required",
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
