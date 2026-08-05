package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Match;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.TeamFormation;
import com.bjit.royalclub.royalclubfootball.entity.TeamFormationSlot;
import com.bjit.royalclub.royalclubfootball.entity.TeamPlayer;
import com.bjit.royalclub.royalclubfootball.enums.MatchStatus;
import com.bjit.royalclub.royalclubfootball.enums.PositionGroup;
import com.bjit.royalclub.royalclubfootball.enums.TeamPlayerRole;
import com.bjit.royalclub.royalclubfootball.exception.TeamServiceException;
import com.bjit.royalclub.royalclubfootball.model.TeamFormationRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamFormationResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamFormationSlotRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamFormationSlotResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamPlayerResponse;
import com.bjit.royalclub.royalclubfootball.repository.MatchRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamFormationRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamPlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import com.bjit.royalclub.royalclubfootball.util.CurrentUserUtil;
import com.bjit.royalclub.royalclubfootball.util.FormationPresets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TeamFormationServiceImpl implements TeamFormationService {

    private static final int FALLBACK_TEAM_SIZE = 6;
    private static final BigDecimal MIN_COORDINATE = BigDecimal.valueOf(4);
    private static final BigDecimal MAX_COORDINATE = BigDecimal.valueOf(96);
    private static final String[] MANAGER_ROLES = {"ADMIN", "SUPERADMIN", "COORDINATOR"};

    private static final String NOT_CAPTAIN = "Only the team captain or a tournament admin can change this line-up";
    private static final String MATCH_OVER = "The match is finished, so its line-up can no longer be changed";

    private final TeamFormationRepository teamFormationRepository;
    private final TeamRepository teamRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final MatchRepository matchRepository;

    /* ---------------------------------------------------------------- */
    /* Reads                                                             */
    /* ---------------------------------------------------------------- */

    @Override
    @Transactional(readOnly = true)
    public TeamFormationResponse getDefaultFormation(Long teamId) {
        Team team = findTeam(teamId);
        return teamFormationRepository.findDefaultByTeamId(teamId)
                .map(formation -> toResponse(formation, team, null, true))
                .orElseGet(() -> presetResponse(team, null));
    }

    @Override
    @Transactional(readOnly = true)
    public TeamFormationResponse getMatchFormation(Long teamId, Long matchId) {
        Team team = findTeam(teamId);
        Match match = findMatch(matchId);
        requireTeamInMatch(team, match);
        return matchFormationOf(team, match);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamFormationResponse> getMatchFormations(Long matchId) {
        Match match = findMatch(matchId);
        List<TeamFormationResponse> responses = new ArrayList<>();
        for (Team team : List.of(match.getHomeTeam(), match.getAwayTeam())) {
            responses.add(matchFormationOf(team, match));
        }
        return responses;
    }

    /**
     * The saved match line-up, else the team default copied across, else the
     * preset. Nothing is written — the copy only becomes real when it is saved.
     */
    private TeamFormationResponse matchFormationOf(Team team, Match match) {
        Optional<TeamFormation> saved = teamFormationRepository.findByTeamIdAndMatchId(team.getId(), match.getId());
        if (saved.isPresent()) {
            return toResponse(saved.get(), team, match, true);
        }

        return teamFormationRepository.findDefaultByTeamId(team.getId())
                .map(defaultFormation -> {
                    TeamFormationResponse response = toResponse(defaultFormation, team, match, false);
                    // It is the default's slots being offered, not a stored match line-up.
                    response.setId(null);
                    response.setSlots(response.getSlots().stream()
                            .peek(slot -> slot.setId(null))
                            .toList());
                    return response;
                })
                .orElseGet(() -> presetResponse(team, match));
    }

    /* ---------------------------------------------------------------- */
    /* Writes                                                            */
    /* ---------------------------------------------------------------- */

    @Override
    @Transactional
    public TeamFormationResponse saveDefaultFormation(Long teamId, TeamFormationRequest request) {
        Team team = findTeam(teamId);
        requireEditable(team, null);

        TeamFormation formation = teamFormationRepository.findDefaultByTeamId(teamId)
                .orElseGet(() -> newFormation(team, null));
        return toResponse(persist(formation, team, request), team, null, true);
    }

    @Override
    @Transactional
    public TeamFormationResponse saveMatchFormation(Long teamId, Long matchId, TeamFormationRequest request) {
        Team team = findTeam(teamId);
        Match match = findMatch(matchId);
        requireTeamInMatch(team, match);
        requireEditable(team, match);

        TeamFormation formation = teamFormationRepository.findByTeamIdAndMatchId(teamId, matchId)
                .orElseGet(() -> newFormation(team, match));
        return toResponse(persist(formation, team, request), team, match, true);
    }

    @Override
    @Transactional
    public void resetMatchFormation(Long teamId, Long matchId) {
        Team team = findTeam(teamId);
        Match match = findMatch(matchId);
        requireTeamInMatch(team, match);
        requireEditable(team, match);

        teamFormationRepository.findByTeamIdAndMatchId(teamId, matchId)
                .ifPresent(teamFormationRepository::delete);
    }

    private TeamFormation newFormation(Team team, Match match) {
        return TeamFormation.builder()
                .team(team)
                .match(match)
                .teamSize(teamSizeOf(team))
                .presetName(FormationPresets.defaultPresetName(teamSizeOf(team)))
                .slots(new ArrayList<>())
                .build();
    }

    /**
     * Replaces the stored sheet with the one supplied. Slots are rebuilt rather
     * than diffed: a line-up is small, and rebuilding keeps {@code slotIndex}
     * contiguous no matter how the captain rearranged things.
     *
     * <p>The old rows are flushed away before the new ones are added. Without
     * that, Hibernate orders the inserts ahead of the deletes in a single flush
     * and the rebuilt sheet collides with the old one on
     * {@code (formation_id, slot_index)}.
     */
    private TeamFormation persist(TeamFormation formation, Team team, TeamFormationRequest request) {
        int teamSize = teamSizeOf(team);
        List<TeamFormationSlotRequest> slotRequests = request.getSlots();

        long starterCount = slotRequests.stream().filter(TeamFormationServiceImpl::isStarter).count();
        if (starterCount > teamSize) {
            throw new TeamServiceException(
                    "This tournament plays " + teamSize + " a side, so the line-up cannot have "
                            + starterCount + " starters", HttpStatus.BAD_REQUEST);
        }

        Map<Long, TeamPlayer> squad = squadOf(team.getId());
        Set<Long> used = new HashSet<>();

        formation.setPresetName(request.getPresetName().trim());
        formation.setNotes(request.getNotes());
        formation.setTeamSize(teamSize);
        formation.getSlots().clear();
        TeamFormation saved = teamFormationRepository.saveAndFlush(formation);

        int slotIndex = 0;
        // Starters keep the low indices so the bench order stays readable.
        for (TeamFormationSlotRequest slotRequest : orderStartersFirst(slotRequests)) {
            saved.getSlots().add(toSlot(saved, slotRequest, slotIndex++, squad, used, team));
        }
        return teamFormationRepository.save(saved);
    }

    private TeamFormationSlot toSlot(TeamFormation formation,
                                     TeamFormationSlotRequest request,
                                     int slotIndex,
                                     Map<Long, TeamPlayer> squad,
                                     Set<Long> used,
                                     Team team) {
        TeamPlayer teamPlayer = null;
        if (request.getTeamPlayerId() != null) {
            teamPlayer = squad.get(request.getTeamPlayerId());
            if (teamPlayer == null) {
                throw new TeamServiceException("A selected player is not in " + team.getTeamName(),
                        HttpStatus.BAD_REQUEST);
            }
            if (!used.add(teamPlayer.getId())) {
                throw new TeamServiceException(
                        teamPlayer.getPlayer().getName() + " cannot fill two places in the same line-up",
                        HttpStatus.BAD_REQUEST);
            }
        }

        return TeamFormationSlot.builder()
                .formation(formation)
                .teamPlayer(teamPlayer)
                .slotIndex(slotIndex)
                .positionGroup(PositionGroup.getGroupOrDefault(request.getPositionGroup()))
                .slotLabel(request.getSlotLabel())
                .x(clamp(request.getX()))
                .y(clamp(request.getY()))
                .isStarter(isStarter(request))
                .build();
    }

    private static List<TeamFormationSlotRequest> orderStartersFirst(List<TeamFormationSlotRequest> slots) {
        List<TeamFormationSlotRequest> ordered = new ArrayList<>(slots.size());
        slots.stream().filter(TeamFormationServiceImpl::isStarter).forEach(ordered::add);
        slots.stream().filter(slot -> !isStarter(slot)).forEach(ordered::add);
        return ordered;
    }

    private static boolean isStarter(TeamFormationSlotRequest slot) {
        return !Boolean.FALSE.equals(slot.getIsStarter());
    }

    /** Keeps tokens inside the pitch box even if the client sends 0 or 100. */
    private static BigDecimal clamp(BigDecimal value) {
        if (value == null) {
            return BigDecimal.valueOf(50);
        }
        return value.max(MIN_COORDINATE).min(MAX_COORDINATE);
    }

    /* ---------------------------------------------------------------- */
    /* Permissions                                                       */
    /* ---------------------------------------------------------------- */

    /**
     * @return why the caller cannot edit, or null when they can
     */
    private String lockedReason(Team team, Match match) {
        if (match != null && MatchStatus.COMPLETED.equals(match.getMatchStatus())) {
            return MATCH_OVER;
        }
        if (CurrentUserUtil.hasAnyRole(MANAGER_ROLES)) {
            return null;
        }
        boolean isCaptain = CurrentUserUtil.currentPlayerId()
                .flatMap(playerId -> teamPlayerRepository.findByTeamIdAndPlayerId(team.getId(), playerId))
                .map(TeamPlayer::getTeamPlayerRole)
                .filter(role -> TeamPlayerRole.CAPTAIN.equals(role) || TeamPlayerRole.VICE_CAPTAIN.equals(role))
                .isPresent();
        return isCaptain ? null : NOT_CAPTAIN;
    }

    private void requireEditable(Team team, Match match) {
        String reason = lockedReason(team, match);
        if (reason != null) {
            HttpStatus status = MATCH_OVER.equals(reason) ? HttpStatus.CONFLICT : HttpStatus.FORBIDDEN;
            throw new TeamServiceException(reason, status);
        }
    }

    /* ---------------------------------------------------------------- */
    /* Lookups and mapping                                               */
    /* ---------------------------------------------------------------- */

    private Team findTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamServiceException("Team not found", HttpStatus.NOT_FOUND));
    }

    private Match findMatch(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new TeamServiceException("Match not found", HttpStatus.NOT_FOUND));
    }

    private void requireTeamInMatch(Team team, Match match) {
        boolean playing = Objects.equals(idOf(match.getHomeTeam()), team.getId())
                || Objects.equals(idOf(match.getAwayTeam()), team.getId());
        if (!playing) {
            throw new TeamServiceException(team.getTeamName() + " is not playing in this match",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private static Long idOf(Team team) {
        return team == null ? null : team.getId();
    }

    private int teamSizeOf(Team team) {
        Integer size = team.getTournament() == null ? null : team.getTournament().getTeamSize();
        if (size == null || !FormationPresets.isSupportedTeamSize(size)) {
            return FALLBACK_TEAM_SIZE;
        }
        return size;
    }

    private List<TeamPlayerResponse> squadResponse(Long teamId) {
        return teamPlayerRepository.findAllByTeamId(teamId).stream()
                .map(teamPlayer -> {
                    String photoKey = teamPlayer.getPlayer().getPhotoKey();
                    return TeamPlayerResponse.builder()
                            .id(teamPlayer.getId())
                            .teamId(teamId)
                            .playerId(teamPlayer.getPlayer().getId())
                            .playerName(teamPlayer.getPlayer().getName())
                            .playingPosition(teamPlayer.getPlayingPosition())
                            .teamPlayerRole(teamPlayer.getTeamPlayerRole() == null
                                    ? null : teamPlayer.getTeamPlayerRole().name())
                            .isCaptain(TeamPlayerRole.CAPTAIN.equals(teamPlayer.getTeamPlayerRole()))
                            .jerseyNumber(teamPlayer.getJerseyNumber())
                            .photoKey(photoKey)
                            .photoUrl(photoKey == null || photoKey.isBlank()
                                    ? null : "/files/player-photos/" + photoKey)
                            .build();
                })
                .toList();
    }

    private Map<Long, TeamPlayer> squadOf(Long teamId) {
        Map<Long, TeamPlayer> squad = new LinkedHashMap<>();
        teamPlayerRepository.findAllByTeamId(teamId).forEach(player -> squad.put(player.getId(), player));
        return squad;
    }

    /** The shape a team starts from when nothing has been saved for it yet. */
    private TeamFormationResponse presetResponse(Team team, Match match) {
        int teamSize = teamSizeOf(team);
        String presetName = FormationPresets.defaultPresetName(teamSize);

        List<TeamFormationSlotResponse> slots = FormationPresets.slotsFor(teamSize, presetName).stream()
                .map(preset -> TeamFormationSlotResponse.builder()
                        .slotIndex(preset.getSlotIndex())
                        .positionGroup(preset.getPositionGroup())
                        .slotLabel(preset.getSlotLabel())
                        .x(preset.getX())
                        .y(preset.getY())
                        .isStarter(true)
                        .build())
                .toList();

        return baseResponse(team, match, teamSize, presetName, null)
                .saved(false)
                .slots(slots)
                .build();
    }

    private TeamFormationResponse toResponse(TeamFormation formation, Team team, Match match, boolean saved) {
        int teamSize = formation.getTeamSize() != null ? formation.getTeamSize() : teamSizeOf(team);

        List<TeamFormationSlotResponse> slots = formation.getSlots().stream()
                .map(this::toSlotResponse)
                .toList();

        return baseResponse(team, match, teamSize, formation.getPresetName(), formation.getNotes())
                .id(formation.getId())
                .saved(saved)
                .slots(slots)
                .build();
    }

    private TeamFormationResponse.TeamFormationResponseBuilder baseResponse(Team team,
                                                                           Match match,
                                                                           int teamSize,
                                                                           String presetName,
                                                                           String notes) {
        String reason = lockedReason(team, match);
        return TeamFormationResponse.builder()
                .squad(squadResponse(team.getId()))
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .teamLogoUrl(team.getLogoKey() == null || team.getLogoKey().isBlank()
                        ? null : "/files/team-logos/" + team.getLogoKey())
                .matchId(match == null ? null : match.getId())
                .isDefault(match == null)
                .presetName(presetName)
                .teamSize(teamSize)
                .notes(notes)
                .editable(reason == null)
                .lockedReason(reason)
                .availablePresets(FormationPresets.presetNames(teamSize));
    }

    private TeamFormationSlotResponse toSlotResponse(TeamFormationSlot slot) {
        TeamFormationSlotResponse.TeamFormationSlotResponseBuilder builder = TeamFormationSlotResponse.builder()
                .id(slot.getId())
                .slotIndex(slot.getSlotIndex())
                .positionGroup(slot.getPositionGroup())
                .slotLabel(slot.getSlotLabel())
                .x(slot.getX())
                .y(slot.getY())
                .isStarter(slot.getIsStarter());

        TeamPlayer teamPlayer = slot.getTeamPlayer();
        if (teamPlayer != null) {
            String photoKey = teamPlayer.getPlayer().getPhotoKey();
            builder.teamPlayerId(teamPlayer.getId())
                    .playerId(teamPlayer.getPlayer().getId())
                    .playerName(teamPlayer.getPlayer().getName())
                    .jerseyNumber(teamPlayer.getJerseyNumber())
                    .playingPosition(teamPlayer.getPlayingPosition() == null
                            ? null : teamPlayer.getPlayingPosition().name())
                    .isCaptain(TeamPlayerRole.CAPTAIN.equals(teamPlayer.getTeamPlayerRole()))
                    .photoKey(photoKey)
                    .photoUrl(photoKey == null || photoKey.isBlank() ? null : "/files/player-photos/" + photoKey);
        }

        return builder.build();
    }
}
