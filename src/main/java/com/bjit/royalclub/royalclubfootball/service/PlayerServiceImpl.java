package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.config.GoalKeeperQueueProperties;
import com.bjit.royalclub.royalclubfootball.config.PlayerProperties;
import com.bjit.royalclub.royalclubfootball.util.PaginationUtil;
import com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.PlayerGoalkeepingHistory;
import com.bjit.royalclub.royalclubfootball.entity.Role;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipant;
import com.bjit.royalclub.royalclubfootball.enums.GoalKeeperQueueTier;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
    private final GoalKeeperQueueProperties goalKeeperQueueProperties;
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
        if (updateRequest.getGkEligible() != null) {
            player.setGkEligible(updateRequest.getGkEligible());
        }
        if (updateRequest.getProfilePhoto() != null && !updateRequest.getProfilePhoto().isBlank()) {
            String oldKey = player.getProfilePhoto();
            if (oldKey != null && !oldKey.isBlank() && !oldKey.equals(updateRequest.getProfilePhoto())) {
                storageProvider.delete(oldKey);
            }
        }
        player.setProfilePhoto(updateRequest.getProfilePhoto());
        applyPhotoKey(player, updateRequest.getPhotoKey());
        player = playerRepository.save(player);
        /*role need to be handle while update players. and only Admin can change the role*/
        return convertToDto(player);
    }

    @Override
    public PlayerResponse updatePhoto(Long id, String photoKey) {
        // Same rule as a full update: your own profile, or an admin fixing someone else's.
        if (!isUserAuthorizedForSelf(id) &&
                getLoggedInPlayer().getRoles().stream()
                        .noneMatch(role -> "ADMIN".equals(role.getName()))) {
            throw new java.lang.SecurityException(UNAUTHORIZED);
        }
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        applyPhotoKey(player, photoKey);
        return convertToDto(playerRepository.save(player));
    }

    /**
     * Points a player at a newly uploaded photo, retiring the one it replaces.
     * <p>
     * Shared by the full player update and the photo-only endpoint so both retire the old object and
     * start the rate-limit window identically - a client that could change a photo without stamping
     * {@code photoUpdatedAt} would be a way around the quota.
     */
    private void applyPhotoKey(Player player, String photoKey) {
        if (photoKey == null || photoKey.isBlank()) {
            return;
        }
        String previousKey = player.getPhotoKey();
        boolean isReplacement = previousKey != null && !previousKey.isBlank()
                && !previousKey.equals(photoKey);
        if (isReplacement) {
            playerPhotoStorageProvider.delete(previousKey);
            // Stamped only on a genuine replacement, so the first photo a member ever sets stays
            // free and does not cost them their next change.
            player.setPhotoUpdatedAt(LocalDateTime.now());
        }
        player.setPhotoKey(photoKey);
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
                .gkEligible(player.isGkEligible())
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

    /**
     * Suggests who should keep goal next, as a ranking of the players confirmed for this tournament.
     * <p>
     * The ranking is a ledger, not a counter. Every tournament creates a fixed amount of goalkeeping
     * work, shared equally between the people who turned up for it, so each appearance accrues a
     * fraction of a turn; each turn actually served pays one off. Ranking by what remains
     * ({@code accrued - served}) is what makes a newcomer, a returning member and a ten-year veteran
     * comparable at all: a raw count of turns cannot tell "has been unfairly skipped" apart from
     * "has not been here long enough to be asked", and rewards both alike.
     * <p>
     * Obligation accrues only for tournaments a player attended, so an absence neither builds up a
     * debt they never had the chance to pay nor flatters them for a low count they only have because
     * they were not there to be picked.
     * <p>
     * Held separately from that is whether someone can reasonably take a turn <em>now</em>: anyone
     * who kept goal within the cooldown window drops below everyone who did not, and anyone who has
     * opted out is shown but not ranked. Both bands are applied before the ledger sorts within them,
     * because folding fairness and rotation into one ordering lets whichever comes first silently
     * override the other.
     *
     * @see GoalKeeperQueueProperties
     * @see GoalKeeperQueueTier
     */
    @Override
    public GoalKeeperQueueResponseDto getGoalKeeperPriorityQueue(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new PlayerServiceException("Tournament not found", HttpStatus.NOT_FOUND));

        List<TournamentParticipant> participants = tournamentParticipantRepository
                .findAllByTournamentIdAndParticipationStatusTrueWithPlayer(tournamentId);

        if (participants.isEmpty()) {
            return GoalKeeperQueueResponseDto.builder()
                    .tournamentId(tournamentId)
                    .tournamentName(tournament.getName())
                    .tournamentDate(tournament.getTournamentDate())
                    .goalKeeperPriorityQueue(new ArrayList<>())
                    .cooldownTournaments(goalKeeperQueueProperties.getCooldownTournaments())
                    .build();
        }

        Integer activeTournamentCount = goalkeepingHistoryRepository.countActiveTournaments();
        if (activeTournamentCount == null || activeTournamentCount == 0) {
            activeTournamentCount = 1; // Prevent division by zero
        }

        List<Long> playerIds = participants.stream()
                .map(p -> p.getPlayer().getId())
                .toList();

        // ===== LEDGER INPUTS (all batched) =====

        // Every past tournament each player actually turned up to. Obligation accrues per
        // appearance, so this is the ledger's spine rather than a statistic on the side.
        Map<Long, List<Long>> attendedByPlayer = new HashMap<>();
        tournamentParticipantRepository.findAttendedTournamentIdsBatch(playerIds, tournamentId)
                .forEach(row -> attendedByPlayer
                        .computeIfAbsent(((Number) row[0]).longValue(), key -> new ArrayList<>())
                        .add(((Number) row[1]).longValue()));

        Set<Long> ledgerTournamentIds = attendedByPlayer.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        // What one appearance at each of those tournaments asked of an attendee: the keeper slots
        // it had to fill, shared between everyone who turned up for it. Normalising by head count
        // is what stops a thin turnout and a full one being treated as equal burdens.
        Map<Long, Double> accrualRateByTournament = new HashMap<>();
        int tournamentsWithRecordedKeepers = 0;
        int tournamentsEstimated = 0;
        if (!ledgerTournamentIds.isEmpty()) {
            Map<Long, Integer> slotsByTournament = new HashMap<>();
            goalkeepingHistoryRepository.countGoalKeeperSlotsByTournamentIds(ledgerTournamentIds)
                    .forEach(row -> slotsByTournament.put(
                            ((Number) row[0]).longValue(), ((Number) row[1]).intValue()));

            Map<Long, Integer> headCountByTournament = new HashMap<>();
            tournamentParticipantRepository.countParticipantsByTournamentIds(ledgerTournamentIds)
                    .forEach(row -> headCountByTournament.put(
                            ((Number) row[0]).longValue(), ((Number) row[1]).intValue()));

            boolean estimating = goalKeeperQueueProperties.isEstimateMissingSlots();
            for (Long ledgerTournamentId : ledgerTournamentIds) {
                Integer recordedSlots = slotsByTournament.get(ledgerTournamentId);
                int slots;
                if (recordedSlots != null) {
                    tournamentsWithRecordedKeepers++;
                    slots = recordedSlots;
                } else if (estimating) {
                    // A tournament whose keepers were never entered looks exactly like one that
                    // genuinely needed none. Counted and reported rather than quietly absorbed.
                    tournamentsEstimated++;
                    slots = goalKeeperQueueProperties.getDefaultSlotsPerTournament();
                } else {
                    slots = 0;
                }
                // With no recorded head count there is nobody to share the work between.
                int headCount = headCountByTournament.getOrDefault(ledgerTournamentId, 0);
                accrualRateByTournament.put(ledgerTournamentId,
                        headCount > 0 ? (double) slots / headCount : 0.0);
            }
        }

        // Turns actually served, counting shifts rather than tournament days.
        Map<Long, Integer> stintsByPlayer = new HashMap<>();
        goalkeepingHistoryRepository
                .countGoalKeeperStintsBatch(playerIds, tournamentId, tournament.getTournamentDate())
                .forEach(row -> stintsByPlayer.put(
                        ((Number) row[0]).longValue(), ((Number) row[1]).intValue()));

        // Who is resting, and how far back their turn was. Index 0 is the tournament just gone.
        Map<Long, Integer> cooldownIndexByPlayer = new HashMap<>();
        int cooldownWindow = Math.max(0, goalKeeperQueueProperties.getCooldownTournaments());
        if (cooldownWindow > 0) {
            List<Long> recentTournamentIds = tournamentRepository.findRecentTournamentIdsBefore(
                    tournament.getTournamentDate(), PageRequest.of(0, cooldownWindow));
            if (!recentTournamentIds.isEmpty()) {
                Map<Long, Integer> cooldownPositions = new HashMap<>();
                for (int index = 0; index < recentTournamentIds.size(); index++) {
                    cooldownPositions.put(recentTournamentIds.get(index), index);
                }
                goalkeepingHistoryRepository
                        .findGoalKeeperAssignmentsInTournaments(playerIds, recentTournamentIds)
                        .forEach(row -> {
                            Integer index = cooldownPositions.get(((Number) row[1]).longValue());
                            if (index != null) {
                                // Their most recent turn is the one that sets the rest period.
                                cooldownIndexByPlayer.merge(
                                        ((Number) row[0]).longValue(), index, Math::min);
                            }
                        });
            }
        }

        // ===== DISPLAY DATA =====

        List<PlayerGoalkeepingHistory> allGkHistories = goalkeepingHistoryRepository
                .findGoalKeeperHistoryByPlayerIdsExcludingTournament(playerIds, tournamentId);
        Map<Long, List<PlayerGoalkeepingHistory>> gkHistoryByPlayer = allGkHistories.stream()
                .collect(Collectors.groupingBy(h -> h.getPlayer().getId()));

        Map<Long, Integer> participationCountMap = new HashMap<>();
        goalkeepingHistoryRepository.countPlayerTournamentParticipationsBatch(playerIds)
                .forEach(row -> participationCountMap.put((Long) row[0], ((Long) row[1]).intValue()));

        Map<Long, LocalDateTime> lastParticipationMap = new HashMap<>();
        tournamentParticipantRepository.findMostRecentParticipationDateBatch(playerIds, tournamentId)
                .forEach(row -> lastParticipationMap.put((Long) row[0], (LocalDateTime) row[1]));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yy");

        List<GoalKeeperPriorityDto> eligible = new ArrayList<>();
        List<GoalKeeperPriorityDto> cooldown = new ArrayList<>();
        List<GoalKeeperPriorityDto> exempt = new ArrayList<>();

        for (TournamentParticipant participant : participants) {
            Player player = participant.getPlayer();
            Long playerId = player.getId();

            List<PlayerGoalkeepingHistory> playerGkHistory =
                    gkHistoryByPlayer.getOrDefault(playerId, List.of());

            // Keeper rows are written when a team sheet is filled in, which can happen before the
            // day itself, so anything not strictly in the past is excluded - a queue must never
            // read an assignment already pencilled in for the tournament it is ranking.
            List<LocalDateTime> pastGkDates = playerGkHistory.stream()
                    .map(PlayerGoalkeepingHistory::getPlayedDate)
                    .filter(playedDate -> playedDate.isBefore(tournament.getTournamentDate()))
                    .toList();

            Integer totalGKTournaments = (int) playerGkHistory.stream()
                    .filter(h -> h.getPlayedDate().isBefore(tournament.getTournamentDate()))
                    .map(h -> h.getTournament().getId())
                    .distinct()
                    .count();

            LocalDateTime lastGoalKeeperDate = pastGkDates.stream()
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

            List<Long> attended = attendedByPlayer.getOrDefault(playerId, List.of());
            double accruedObligation = attended.stream()
                    .mapToDouble(attendedId -> accrualRateByTournament.getOrDefault(attendedId, 0.0))
                    .sum();
            int stints = stintsByPlayer.getOrDefault(playerId, 0);

            Integer totalTournamentParticipations = participationCountMap.getOrDefault(playerId, 0);
            double participationFrequency =
                    (totalTournamentParticipations * 100.0) / activeTournamentCount;

            String lastPlayedTournamentDate = null;
            LocalDateTime lastParticipationDate = lastParticipationMap.get(playerId);
            if (lastParticipationDate != null) {
                lastPlayedTournamentDate = lastParticipationDate.format(dateFormatter);
            }

            Integer cooldownIndex = cooldownIndexByPlayer.get(playerId);
            GoalKeeperQueueTier tier;
            if (!player.isGkEligible()) {
                tier = GoalKeeperQueueTier.EXEMPT;
            } else if (cooldownIndex != null) {
                tier = GoalKeeperQueueTier.COOLDOWN;
            } else {
                tier = GoalKeeperQueueTier.ELIGIBLE;
            }

            GoalKeeperPriorityDto dto = GoalKeeperPriorityDto.builder()
                    .category(tier.name())
                    .playerId(playerId)
                    .playerName(player.getName())
                    .employeeId(player.getEmployeeId())
                    .playAsGkDates(pastGkDates.stream()
                            .map(playedDate -> playedDate.format(dateFormatter))
                            .toList())
                    .totalTournamentParticipations(totalTournamentParticipations)
                    .activeTournamentCount(activeTournamentCount)
                    .participationFrequency(roundToTwoPlaces(participationFrequency))
                    .lastPlayedTournamentDate(lastPlayedTournamentDate)
                    .totalGoalKeeperTournaments(totalGKTournaments)
                    .lastGoalKeeperDate(lastGoalKeeperDate)
                    .accruedObligation(roundToTwoPlaces(accruedObligation))
                    .goalKeeperStints(stints)
                    .goalKeeperDebt(roundToTwoPlaces(accruedObligation - stints))
                    .attendedTournaments(attended.size())
                    .cooldownRemaining(cooldownIndex == null ? null : cooldownWindow - cooldownIndex)
                    .build();

            if (tier == GoalKeeperQueueTier.EXEMPT) {
                exempt.add(dto);
            } else if (tier == GoalKeeperQueueTier.COOLDOWN) {
                cooldown.add(dto);
            } else {
                eligible.add(dto);
            }
        }

        // Most owed first. A player who has never kept goal edges out an equal debt who has, and
        // player id only ever settles a genuine dead heat.
        Comparator<GoalKeeperPriorityDto> byDebtOwed =
                Comparator.comparingDouble((GoalKeeperPriorityDto dto) -> dto.getGoalKeeperDebt())
                        .reversed()
                        .thenComparing(GoalKeeperPriorityDto::getLastGoalKeeperDate,
                                Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(GoalKeeperPriorityDto::getPlayerId);

        eligible.sort(byDebtOwed);
        // Within the rest band, the further back someone's turn was the sooner they come back up.
        cooldown.sort(Comparator
                .comparingInt((GoalKeeperPriorityDto dto) -> dto.getCooldownRemaining())
                .thenComparing(byDebtOwed));
        exempt.sort(Comparator.comparing(GoalKeeperPriorityDto::getPlayerName,
                String.CASE_INSENSITIVE_ORDER));

        List<GoalKeeperPriorityDto> finalQueue = new ArrayList<>(participants.size());
        finalQueue.addAll(eligible);
        finalQueue.addAll(cooldown);
        finalQueue.addAll(exempt);

        int priority = 1;
        for (GoalKeeperPriorityDto dto : finalQueue) {
            dto.setPriority(priority++);
        }

        return GoalKeeperQueueResponseDto.builder()
                .tournamentId(tournamentId)
                .tournamentName(tournament.getName())
                .tournamentDate(tournament.getTournamentDate())
                .goalKeeperPriorityQueue(finalQueue)
                .cooldownTournaments(cooldownWindow)
                .ledgerCoverage(GoalKeeperQueueResponseDto.LedgerCoverage.builder()
                        .tournamentsConsidered(ledgerTournamentIds.size())
                        .tournamentsWithRecordedKeepers(tournamentsWithRecordedKeepers)
                        .tournamentsEstimated(tournamentsEstimated)
                        .estimatingMissingSlots(goalKeeperQueueProperties.isEstimateMissingSlots())
                        .build())
                .build();
    }

    /** Ledger figures are read by people arguing about their place in a queue, so keep them legible. */
    private static double roundToTwoPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
