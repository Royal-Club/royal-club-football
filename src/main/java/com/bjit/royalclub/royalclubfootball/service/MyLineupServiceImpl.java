package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.TeamPlayer;
import com.bjit.royalclub.royalclubfootball.model.MyLineupResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamFormationResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamFormationSlotResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamPlayerResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentTeamResponse;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import com.bjit.royalclub.royalclubfootball.util.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Answers "what is my line-up for this match?" in one call.
 *
 * The rule for whether a line-up is worth showing lives here rather than in each client: a saved
 * formation with at least one player placed. An unsaved formation is only the preset the editor
 * offers a captain as a starting point - showing it would tell players they had been put in
 * positions nobody chose.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MyLineupServiceImpl implements MyLineupService {

    private final TeamRepository teamRepository;
    private final TeamFormationService teamFormationService;

    @Override
    @Transactional(readOnly = true)
    public Optional<MyLineupResponse> getMyLineup(Long tournamentId) {
        Optional<Long> playerId = CurrentUserUtil.currentPlayerId();
        if (playerId.isEmpty()) {
            return Optional.empty();
        }

        Optional<Team> myTeam = teamRepository.findTeamsWithPlayersByTournamentId(tournamentId).stream()
                .filter(team -> holdsPlayer(team, playerId.get()))
                .findFirst();
        if (myTeam.isEmpty()) {
            return Optional.empty();
        }

        Team team = myTeam.get();
        TeamFormationResponse formation = teamFormationService.getDefaultFormation(team.getId());
        List<TeamFormationSlotResponse> slots =
                formation.getSlots() == null ? List.of() : formation.getSlots();

        List<TeamFormationSlotResponse> filled = slots.stream()
                .filter(slot -> slot.getPlayerId() != null)
                .toList();

        // Not announced yet. Both halves matter: `saved` false means no captain has ever opened the
        // editor, and an empty `filled` means one saved a shape without putting anyone in it.
        if (!Boolean.TRUE.equals(formation.getSaved()) || filled.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(MyLineupResponse.builder()
                .tournamentId(tournamentId)
                .team(toTeamResponse(team))
                .formation(formation)
                .mySlot(filled.stream()
                        .filter(slot -> Objects.equals(slot.getPlayerId(), playerId.get()))
                        .findFirst()
                        .orElse(null))
                .filledSlots(filled.size())
                .totalSlots(slots.size())
                .build());
    }

    private boolean holdsPlayer(Team team, Long playerId) {
        return team.getTeamPlayers() != null && team.getTeamPlayers().stream()
                .anyMatch(teamPlayer -> teamPlayer.getPlayer() != null
                        && Objects.equals(teamPlayer.getPlayer().getId(), playerId));
    }

    /**
     * Carries the full squad, not just the starters - the client needs it to work out who is on the
     * bench, and {@link TeamFormationResponse} does not populate its own player list.
     */
    private TournamentTeamResponse toTeamResponse(Team team) {
        List<TeamPlayerResponse> players = team.getTeamPlayers().stream()
                .map(this::toPlayerResponse)
                .toList();

        return TournamentTeamResponse.builder()
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .logoKey(team.getLogoKey())
                .logoUrl(buildTeamLogoUrl(team.getLogoKey()))
                .players(players)
                .build();
    }

    private TeamPlayerResponse toPlayerResponse(TeamPlayer teamPlayer) {
        return TeamPlayerResponse.builder()
                .id(teamPlayer.getId())
                .teamId(teamPlayer.getTeam() != null ? teamPlayer.getTeam().getId() : null)
                .playerId(teamPlayer.getPlayer().getId())
                .playerName(teamPlayer.getPlayer().getName())
                .playingPosition(teamPlayer.getPlayingPosition())
                .teamPlayerRole(teamPlayer.getTeamPlayerRole() != null
                        ? teamPlayer.getTeamPlayerRole().name() : null)
                .isCaptain(teamPlayer.getIsCaptain())
                .jerseyNumber(teamPlayer.getJerseyNumber())
                .photoKey(teamPlayer.getPlayer().getPhotoKey())
                .photoUrl(buildPlayerPhotoUrl(teamPlayer.getPlayer().getPhotoKey()))
                .build();
    }

    private String buildPlayerPhotoUrl(String photoKey) {
        return photoKey == null || photoKey.isBlank() ? null : "/files/player-photos/" + photoKey;
    }

    private String buildTeamLogoUrl(String logoKey) {
        return logoKey == null || logoKey.isBlank() ? null : "/files/team-logos/" + logoKey;
    }
}
