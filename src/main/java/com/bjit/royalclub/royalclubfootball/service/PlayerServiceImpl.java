package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.config.PlayerProperties;
import com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.PlayerGoalkeepingHistory;
import com.bjit.royalclub.royalclubfootball.entity.Role;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipant;
import com.bjit.royalclub.royalclubfootball.enums.PlayerRole;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.exception.SecurityException;
import com.bjit.royalclub.royalclubfootball.model.GoalKeeperHistoryDto;
import com.bjit.royalclub.royalclubfootball.model.GoalKeeperPriorityDto;
import com.bjit.royalclub.royalclubfootball.model.GoalKeeperQueueResponseDto;
import com.bjit.royalclub.royalclubfootball.model.PlayerRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerResponse;
import com.bjit.royalclub.royalclubfootball.model.PlayerUpdateRequest;
import com.bjit.royalclub.royalclubfootball.repository.PlayerGoalkeepingHistoryRepository;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.RoleRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentParticipantRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.EMAIL_ALREADY_IN_USE;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.UNAUTHORIZED;
import static com.bjit.royalclub.royalclubfootball.security.util.SecurityUtil.getLoggedInPlayer;
import static com.bjit.royalclub.royalclubfootball.security.util.SecurityUtil.getLoggedInUserId;
import static com.bjit.royalclub.royalclubfootball.security.util.SecurityUtil.isUserAuthorizedForSelf;
import static com.bjit.royalclub.royalclubfootball.util.StringUtils.normalizeString;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PlayerGoalkeepingHistoryRepository goalkeepingHistoryRepository;
    private final PlayerProperties playerProperties;
    private final TournamentRepository tournamentRepository;
    private final TournamentParticipantRepository tournamentParticipantRepository;

    @Override
    public void registerPlayer(PlayerRegistrationRequest registrationRequest) {

        playerRepository.findByEmail(registrationRequest.getEmail()).ifPresent(player -> {
            throw new PlayerServiceException(RestErrorMessageDetail.PLAYER_ALREADY_EXISTS, HttpStatus.CONFLICT);
        });

        Role playerRole = roleRepository.findByName(PlayerRole.PLAYER.name())
                .orElseThrow(() -> new PlayerServiceException("Role PLAYER not found", HttpStatus.NOT_FOUND));

        Set<Role> roles = new HashSet<>();
        roles.add(playerRole);
        Player player = Player.builder()
                .email(registrationRequest.getEmail())
                .name(registrationRequest.getName())
                .employeeId(registrationRequest.getEmployeeId())
                .mobileNo(registrationRequest.getMobileNo())
                .skypeId(registrationRequest.getSkypeId())
                .position(registrationRequest.getPlayingPosition())
                .isActive(false)
                .password(passwordEncoder.encode(playerProperties.getDefaultPassword()))
                .lastPasswordChangeDate(null)
                .roles(roles).build();
        playerRepository.save(player);
    }

    @Override
    public List<PlayerResponse> getAllPlayers() {
        List<Player> players = playerRepository.findAll();
        return players.stream().map(this::convertToDto).toList();
    }

    @Override
    public PlayerResponse getPlayerById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        return convertToDto(player);
    }

    @Override
    public PlayerResponse getPlayerResponse(Player player) {
        return convertToDto(player);
    }

    @Override
    public Set<PlayerResponse> getPlayerResponses(Set<Player> players) {

        return players.stream().map(this::convertToDto).collect(Collectors.toSet());
    }

    @Override
    public Player getPlayerEntity(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Override
    public void updatePlayerStatus(Long id, boolean active) {
        Player player = playerRepository
                .findById(id).orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        player.setActive(active);
        playerRepository.save(player);
    }

    @Override
    public PlayerResponse updatePlayer(Long id, PlayerUpdateRequest updateRequest) {

        if (!isUserAuthorizedForSelf(id) &&
                getLoggedInPlayer().getRoles().stream()
                        .noneMatch(role -> "ADMIN".equals(role.getName()))) {
            throw new java.lang.SecurityException(UNAUTHORIZED);
        }

        // Check if email exists and does not belong to the current user
        Optional<Player> existingPlayerWithEmail = playerRepository.findByEmail(updateRequest.getEmail());
        if (existingPlayerWithEmail.isPresent() && !existingPlayerWithEmail.get().getId().equals(id)) {
            throw new SecurityException(EMAIL_ALREADY_IN_USE, HttpStatus.EXPECTATION_FAILED);
        }
        Player player;
        player = playerRepository.findById(id)
                .orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        player.setEmail(updateRequest.getEmail());
        player.setName(updateRequest.getName());
        player.setEmployeeId(updateRequest.getEmployeeId());
        player.setMobileNo(normalizeString(updateRequest.getMobileNo()));
        player.setSkypeId(updateRequest.getSkypeId());
        player.setPosition(updateRequest.getPlayingPosition());
        player = playerRepository.save(player);
        /*role need to be handle while update players. and only Admin can change the role*/
        return convertToDto(player);
    }

    @Override
    public Player findByEmail(String email) {
        return playerRepository
                .findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
    }


    @Override
    public Map<Integer, List<GoalKeeperHistoryDto>> goalKeepingHistory() {

        List<GoalKeeperHistoryDto> historyList = goalkeepingHistoryRepository.getGoalKeeperHistory();

        Set<Long> allPlayerIds = historyList.stream()
                .map(GoalKeeperHistoryDto::getPlayerId)
                .collect(Collectors.toSet());

        Map<Integer, List<GoalKeeperHistoryDto>> groupedByRound = historyList.stream()
                .collect(Collectors.groupingBy(dto ->
                                dto.getRoundNumber() == null || dto.getRoundNumber() == 0 ? 1 : dto.getRoundNumber(),
                        () -> new TreeMap<>(Collections.reverseOrder()),
                        Collectors.toList()));

        groupedByRound.forEach((roundNumber, roundList) -> {
            Set<Long> playersInThisRound = roundList.stream()
                    .map(GoalKeeperHistoryDto::getPlayerId)
                    .collect(Collectors.toSet());

            allPlayerIds.stream()
                    .filter(playerId -> !playersInThisRound.contains(playerId))
                    .forEach(playerId -> {
                        historyList.stream()
                                .filter(player -> player.getPlayerId().equals(playerId))
                                .findFirst()
                                .ifPresent(player -> {
                                    roundList.add(GoalKeeperHistoryDto.builder()
                                            .playerId(player.getPlayerId())
                                            .playerName(player.getPlayerName())
                                            .roundNumber(roundNumber)
                                            .playedDate(null)
                                            .build());
                                });
                    });
            // Comparator.nullsLast(...): ensures null dates (placeholders) go to the bottom of the list.
            roundList.sort(Comparator
                    .comparing(GoalKeeperHistoryDto::getPlayedDate, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(GoalKeeperHistoryDto::getPlayerId));
        });
        return groupedByRound;
    }


    private PlayerResponse convertToDto(Player player) {
        Set<com.bjit.royalclub.royalclubfootball.model.RoleResponse> roleResponses = player.getRoles() != null
                ? player.getRoles().stream()
                .map(role -> com.bjit.royalclub.royalclubfootball.model.RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .build())
                .collect(Collectors.toSet())
                : new HashSet<>();

        return PlayerResponse.builder()
                .id(player.getId())
                .name(player.getName())
                .email(player.getEmail())
                .mobileNo(player.getMobileNo())
                .skypeId(player.getSkypeId())
                .employeeId(player.getEmployeeId())
                .fullName(player.getName() + "[" + player.getEmployeeId() + "]")
                .playingPosition(player.getPosition())
                .isActive(player.isActive())
                .roles(roleResponses)
                .build();
    }

    @Override
    public int countActivePlayers() {
        return playerRepository.countByIsActiveTrue();
    }

    @Override
    public List<GoalKeeperHistoryDto> getGoalKeeperHistoryByLoggedInUser() {
        Long loggedInUserId = getLoggedInUserId();
        List<PlayerGoalkeepingHistory> playerGoalkeepingHistories =
                goalkeepingHistoryRepository.getAllByPlayerIdOrderByRoundNumberDesc(loggedInUserId);
        return playerGoalkeepingHistories.stream()
                .map(playerGoalkeepingHistory -> {
                    Player player = playerGoalkeepingHistory.getPlayer();
                    return GoalKeeperHistoryDto.builder()
                            .playerId(player.getId())
                            .playerName(player.getName())
                            .roundNumber(playerGoalkeepingHistory.getRoundNumber())
                            .playedDate(playerGoalkeepingHistory.getPlayedDate())
                            .build();
                })
                .toList();
    }

    @Override
    public GoalKeeperQueueResponseDto getGoalKeeperPriorityQueue(Long tournamentId) {
        // Fetch the current tournament
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new PlayerServiceException("Tournament not found", HttpStatus.NOT_FOUND));

        // Get the most recent tournament before current one
        Tournament mostRecentTournament = tournamentRepository
                .findMostRecentTournamentBefore(tournament.getTournamentDate());

        // Get all active participants in the current tournament
        List<TournamentParticipant> participants = tournamentParticipantRepository
                .findAllByTournamentIdAndParticipationStatusTrue(tournamentId);

        if (participants.isEmpty()) {
            return GoalKeeperQueueResponseDto.builder()
                    .tournamentId(tournamentId)
                    .tournamentName(tournament.getName())
                    .tournamentDate(tournament.getTournamentDate())
                    .goalKeeperPriorityQueue(new ArrayList<>())
                    .build();
        }

        // Get total active tournaments for frequency calculation
        Integer activeTournamentCount = goalkeepingHistoryRepository.countActiveTournaments();
        if (activeTournamentCount == null || activeTournamentCount == 0) {
            activeTournamentCount = 1; // Prevent division by zero
        }

        // Build list of players with scores
        List<PlayerWithMetadata> playersWithMetadata = new ArrayList<>();

        for (TournamentParticipant participant : participants) {
            Player player = participant.getPlayer();

            // ===== GATHER DATA =====
            // 1. Goalkeeper history (removed unused list variable)
            Integer totalGKTournaments = goalkeepingHistoryRepository
                    .countGoalKeeperHistoryExcludingTournament(player.getId(), tournamentId);

            // 2. Participation frequency data
            Integer totalTournamentParticipations = goalkeepingHistoryRepository
                    .countPlayerTournamentParticipations(player.getId());

            // 3. Most recent GK date
            Optional<LocalDateTime> mostRecentGKDate = goalkeepingHistoryRepository
                    .findMostRecentGoalKeeperDate(player.getId());
            LocalDateTime lastGoalKeeperDate = mostRecentGKDate.orElse(null);
            Integer daysSinceLastGoalkeeping = lastGoalKeeperDate != null
                    ? (int) java.time.temporal.ChronoUnit.DAYS.between(lastGoalKeeperDate.toLocalDate(), tournament.getTournamentDate().toLocalDate())
                    : Integer.MAX_VALUE;
            if (daysSinceLastGoalkeeping < 0) {
                daysSinceLastGoalkeeping = 0; // guard against future dated GK entries
            }

            // 4. Calculate consecutive tournaments missed BEFORE current tournament
            Integer consecutiveMissedTournaments = tournamentParticipantRepository
                    .countConsecutiveMissedTournamentsBeforeCurrent(player.getId(), tournamentId);
            if (consecutiveMissedTournaments == null) {
                consecutiveMissedTournaments = 0;
            }

            // 5. All GK dates for display (EXCLUDING current & FUTURE tournaments - only PAST tournaments)
            List<LocalDateTime> allGoalKeeperDateTimes = goalkeepingHistoryRepository
                    .findGoalKeeperHistoryExcludingTournament(player.getId(), tournamentId).stream()
                    .map(PlayerGoalkeepingHistory::getPlayedDate) // Only PAST dates
                    .filter(playedDate -> playedDate.isBefore(tournament.getTournamentDate()))
                    .toList();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yy");
            List<String> formattedGoalKeeperDates = allGoalKeeperDateTimes.stream()
                    .map(dateTime -> dateTime.format(dateFormatter))
                    .toList();

            // 6. Check if GK in most recent tournament
            boolean wasGKInMostRecent = false;
            if (mostRecentTournament != null) {
                wasGKInMostRecent = goalkeepingHistoryRepository
                        .wasGoalKeeperInTournament(player.getId(), mostRecentTournament.getId());
            }

            // 7. Get last played tournament date (excluding current tournament)
            Optional<LocalDateTime> lastParticipationDateOpt = tournamentParticipantRepository
                    .findMostRecentParticipationDateExcludingCurrent(player.getId(), tournamentId);
            String lastPlayedTournamentDate = null;
            if (lastParticipationDateOpt.isPresent()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy");
                lastPlayedTournamentDate = lastParticipationDateOpt.get().format(formatter);
            }

            // Basic frequency metrics
            Double participationFrequency = activeTournamentCount > 0
                    ? (totalTournamentParticipations * 100.0) / activeTournamentCount
                    : 0.0;
            Double gkExperienceFrequency = totalTournamentParticipations > 0
                    ? (totalGKTournaments * 100.0) / totalTournamentParticipations
                    : 0.0;

            // Build DTO
            GoalKeeperPriorityDto dto = GoalKeeperPriorityDto.builder()
                    .playerId(player.getId())
                    .playerName(player.getName())
                    .employeeId(player.getEmployeeId())
                    .playAsGkDates(formattedGoalKeeperDates)
                    .totalTournamentParticipations(totalTournamentParticipations)
                    .activeTournamentCount(activeTournamentCount)
                    .participationFrequency(Math.round(participationFrequency * 100.0) / 100.0)
                    .lastPlayedTournamentDate(lastPlayedTournamentDate)
                    .totalGoalKeeperTournaments(totalGKTournaments)
                    .lastGoalKeeperDate(lastGoalKeeperDate)
                    .build();

            // Store DTO with internal metadata
            playersWithMetadata.add(new PlayerWithMetadata(dto, wasGKInMostRecent, consecutiveMissedTournaments));
        }

        // CATEGORY-BASED ORDERING
        List<PlayerWithMetadata> regularPlayers = new ArrayList<>();
        List<PlayerWithMetadata> lastTournamentGK = new ArrayList<>();
        List<PlayerWithMetadata> brandNewPlayers = new ArrayList<>();

        for (PlayerWithMetadata metadata : playersWithMetadata) {
            GoalKeeperPriorityDto dto = metadata.dto;
            // Brand new player: ONLY if participating in current tournament AND never participated before in any tournament
            // Check: totalTournamentParticipations == 1 means only current tournament
            // consecutiveMissedTournaments == (activeTournamentCount - 1) means missed ALL previous tournaments (truly new)
            boolean isBrandNew = dto.getTotalTournamentParticipations() == 1
                    && metadata.consecutiveMissedTournaments == (dto.getActiveTournamentCount() - 1);

            if (isBrandNew) {
                brandNewPlayers.add(metadata);
            } else if (metadata.wasGKInMostRecent) {
                lastTournamentGK.add(metadata);
            } else {
                // Regular players (includes irregular/returning players who participated before)
                regularPlayers.add(metadata);
            }
        }

        // Comparator for regular players:
        // 1. Never GK first
        // 2. Fewer consecutive missed tournaments (more regular attendance)
        // 3. Fewer total GK times (less burden)
        // 4. Older lastGoalKeeperDate
        // 5. PlayerId tiebreaker
        Comparator<PlayerWithMetadata> regularComparator = Comparator
                .comparing((PlayerWithMetadata m) -> m.dto.getLastGoalKeeperDate() == null ? 0 : 1) // never GK first
                .thenComparing(m -> m.consecutiveMissedTournaments) // fewer missed = higher priority (regular attendance)
                .thenComparing(m -> m.dto.getTotalGoalKeeperTournaments()) // fewer GK times = higher priority (fair burden distribution)
                .thenComparing(m -> m.dto.getLastGoalKeeperDate(), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(m -> m.dto.getPlayerId());

        // Comparator for last tournament GK group: prioritize LESS experienced GK first (fewer times played GK)
        // Then by older lastGoalKeeperDate, then by playerId
        Comparator<PlayerWithMetadata> lastGKComparator = Comparator
                .comparing((PlayerWithMetadata m) -> m.dto.getTotalGoalKeeperTournaments()) // fewer GK times = higher priority
                .thenComparing(m -> m.dto.getLastGoalKeeperDate(), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(m -> m.dto.getPlayerId());

        regularPlayers.sort(regularComparator);
        lastTournamentGK.sort(lastGKComparator);
        brandNewPlayers.sort(regularComparator); // treat brand new similar to regular (will be placed last anyway)

        List<GoalKeeperPriorityDto> finalQueue = new ArrayList<>();
        finalQueue.addAll(regularPlayers.stream().map(m -> m.dto).toList());
        finalQueue.addAll(lastTournamentGK.stream().map(m -> m.dto).toList());
        finalQueue.addAll(brandNewPlayers.stream().map(m -> m.dto).toList());

        int priority = 1;
        for (GoalKeeperPriorityDto dto : finalQueue) {
            dto.setPriority(priority++);
        }

        return GoalKeeperQueueResponseDto.builder()
                .tournamentId(tournamentId)
                .tournamentName(tournament.getName())
                .tournamentDate(tournament.getTournamentDate())
                .goalKeeperPriorityQueue(finalQueue)
                .build();
    }

    // Inner class to hold DTO with internal metadata (not exposed in API response)
    private static class PlayerWithMetadata {
        final GoalKeeperPriorityDto dto;
        final boolean wasGKInMostRecent;
        final int consecutiveMissedTournaments;

        PlayerWithMetadata(GoalKeeperPriorityDto dto, boolean wasGKInMostRecent, int consecutiveMissedTournaments) {
            this.dto = dto;
            this.wasGKInMostRecent = wasGKInMostRecent;
            this.consecutiveMissedTournaments = consecutiveMissedTournaments;
        }
    }
}
