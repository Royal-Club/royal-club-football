package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.config.PlayerProperties;
import com.bjit.royalclub.royalclubfootball.util.PaginationUtil;
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
import com.bjit.royalclub.royalclubfootball.storage.StorageProvider;
import com.bjit.royalclub.royalclubfootball.storage.playerphoto.PlayerPhotoStorageProvider;
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
import java.util.Objects;
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
    private final StorageProvider storageProvider;
    private final PlayerPhotoStorageProvider playerPhotoStorageProvider;
    private final PlayerPhotoQuotaService playerPhotoQuotaService;

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
                .profilePhoto(registrationRequest.getProfilePhoto())
                .photoKey(registrationRequest.getPhotoKey())
                .isActive(false)
                .password(passwordEncoder.encode(playerProperties.getDefaultPassword()))
                .lastPasswordChangeDate(null)
                .roles(roles).build();
        playerRepository.save(player);
    }

    @Override
    public List<PlayerResponse> getAllPlayers() {
        // Page the IDs, then fetch that page with roles joined: convertToDto reads getRoles(),
        // which would otherwise be one lazy select per player.
        List<Long> ids = playerRepository.findAllPlayerIds(PaginationUtil.cappedListByIdDesc()).getContent();
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Player> playersById = playerRepository.findAllByIdWithRoles(ids).stream()
                .collect(Collectors.toMap(Player::getId, player -> player));
        return ids.stream()
                .map(playersById::get)
                .filter(Objects::nonNull)
                .map(this::convertToDto)
                .toList();
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
    public Set<Player> getPlayerEntities(java.util.Collection<Long> ids) {
        List<Player> players = playerRepository.findAllById(ids);
        if (players.size() != ids.size()) {
            throw new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return new HashSet<>(players);
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
        if (updateRequest.getProfilePhoto() != null && !updateRequest.getProfilePhoto().isBlank()) {
            String oldKey = player.getProfilePhoto();
            if (oldKey != null && !oldKey.isBlank() && !oldKey.equals(updateRequest.getProfilePhoto())) {
                storageProvider.delete(oldKey);
            }
        }
        player.setProfilePhoto(updateRequest.getProfilePhoto());
        // Handle photo replacement: delete old photo if a new one is provided
        if (updateRequest.getPhotoKey() != null && !updateRequest.getPhotoKey().isBlank()) {
            String previousKey = player.getPhotoKey();
            boolean isReplacement = previousKey != null && !previousKey.isBlank()
                    && !previousKey.equals(updateRequest.getPhotoKey());
            if (isReplacement) {
                playerPhotoStorageProvider.delete(previousKey);
                // Starts the rolling window. Stamped only on a genuine replacement, so the first
                // photo a member ever sets stays free and does not cost them their next change.
                player.setPhotoUpdatedAt(LocalDateTime.now());
            }
            player.setPhotoKey(updateRequest.getPhotoKey());
        }
        player = playerRepository.save(player);
        /*role need to be handle while update players. and only Admin can change the role*/
        return convertToDto(player);
    }

    @Override
    public Player findByEmail(String email) {
        return playerRepository
                .findByEmailAndIsActiveTrueWithRoles(email)
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
                .profilePhoto(player.getProfilePhoto())
                .playingPosition(player.getPosition())
                .isActive(player.isActive())
                .roles(roleResponses)
                .photoKey(player.getPhotoKey())
                .photoUrl(player.getPhotoKey() != null
                        ? "/files/player-photos/" + player.getPhotoKey()
                        : null)
                .photoUpdatedAt(player.getPhotoUpdatedAt())
                // Null means "can change now". Lets a client disable the button and name the date
                // instead of letting the member pick a photo and only then be refused.
                .photoChangeAvailableAt(playerPhotoQuotaService.changeAvailableAt(player))
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

        // ===== BATCH PRE-FETCH ALL DATA =====
        List<Long> playerIds = participants.stream()
                .map(p -> p.getPlayer().getId())
                .toList();

        // 1. All GK histories excluding current tournament (batch)
        List<PlayerGoalkeepingHistory> allGkHistories = goalkeepingHistoryRepository
                .findGoalKeeperHistoryByPlayerIdsExcludingTournament(playerIds, tournamentId);
        Map<Long, List<PlayerGoalkeepingHistory>> gkHistoryByPlayer = allGkHistories.stream()
                .collect(Collectors.groupingBy(h -> h.getPlayer().getId()));

        // 2. Participation counts (batch)
        Map<Long, Integer> participationCountMap = new java.util.HashMap<>();
        goalkeepingHistoryRepository.countPlayerTournamentParticipationsBatch(playerIds)
                .forEach(row -> participationCountMap.put((Long) row[0], ((Long) row[1]).intValue()));

        // 3. Most recent GK dates (batch)
        Map<Long, LocalDateTime> mostRecentGkDateMap = new java.util.HashMap<>();
        goalkeepingHistoryRepository.findMostRecentGoalKeeperDateBatch(playerIds)
                .forEach(row -> mostRecentGkDateMap.put((Long) row[0], (LocalDateTime) row[1]));

        // 4. Consecutive missed tournaments (batch) - players with no missed run produce no row
        Map<Long, Integer> consecutiveMissedMap = new java.util.HashMap<>();
        tournamentParticipantRepository.countConsecutiveMissedTournamentsBatch(playerIds, tournamentId)
                .forEach(row -> consecutiveMissedMap.put(
                        ((Number) row[0]).longValue(), ((Number) row[1]).intValue()));

        // 5. GK in most recent tournament (batch)
        java.util.Set<Long> gkInMostRecentTournament = new java.util.HashSet<>();
        if (mostRecentTournament != null) {
            gkInMostRecentTournament.addAll(goalkeepingHistoryRepository
                    .findPlayerIdsWhoWereGoalKeeperInTournament(playerIds, mostRecentTournament.getId()));
        }

        // 6. Last participation dates (batch)
        Map<Long, LocalDateTime> lastParticipationMap = new java.util.HashMap<>();
        tournamentParticipantRepository.findMostRecentParticipationDateBatch(playerIds, tournamentId)
                .forEach(row -> lastParticipationMap.put((Long) row[0], (LocalDateTime) row[1]));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yy");

        for (TournamentParticipant participant : participants) {
            Player player = participant.getPlayer();
            Long playerId = player.getId();

            // Derive stats from pre-fetched data
            List<PlayerGoalkeepingHistory> playerGkHistory = gkHistoryByPlayer.getOrDefault(playerId, List.of());
            Integer totalGKTournaments = (int) playerGkHistory.stream()
                    .map(h -> h.getTournament().getId())
                    .distinct()
                    .count();

            Integer totalTournamentParticipations = participationCountMap.getOrDefault(playerId, 0);

            LocalDateTime lastGoalKeeperDate = mostRecentGkDateMap.get(playerId);
            Integer daysSinceLastGoalkeeping = lastGoalKeeperDate != null
                    ? (int) java.time.temporal.ChronoUnit.DAYS.between(lastGoalKeeperDate.toLocalDate(), tournament.getTournamentDate().toLocalDate())
                    : Integer.MAX_VALUE;
            if (daysSinceLastGoalkeeping < 0) {
                daysSinceLastGoalkeeping = 0;
            }

            Integer consecutiveMissedTournaments = consecutiveMissedMap.getOrDefault(playerId, 0);

            List<String> formattedGoalKeeperDates = playerGkHistory.stream()
                    .map(PlayerGoalkeepingHistory::getPlayedDate)
                    .filter(playedDate -> playedDate.isBefore(tournament.getTournamentDate()))
                    .map(dateTime -> dateTime.format(dateFormatter))
                    .toList();

            boolean wasGKInMostRecent = gkInMostRecentTournament.contains(playerId);

            String lastPlayedTournamentDate = null;
            LocalDateTime lastParticipationDate = lastParticipationMap.get(playerId);
            if (lastParticipationDate != null) {
                lastPlayedTournamentDate = lastParticipationDate.format(dateFormatter);
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
                dto.setCategory("NEW");
                brandNewPlayers.add(metadata);
            } else if (metadata.wasGKInMostRecent) {
                dto.setCategory("LAST_GK");
                lastTournamentGK.add(metadata);
            } else {
                // Regular players (includes irregular/returning players who participated before)
                dto.setCategory("REGULAR");
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
