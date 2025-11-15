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

        if (Boolean.TRUE.equals(!isUserAuthorizedForSelf(id)) &&
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

        List<GoalKeeperPriorityDto> priorityQueue = new ArrayList<>();
        int priority = 1;

        // Category 1: Never played as GK
        List<GoalKeeperPriorityDto> neverPlayedAsGK = new ArrayList<>();
        // Category 2: Played as GK in most recent tournament (LOWEST priority - need rotation)
        List<GoalKeeperPriorityDto> playedInMostRecentTournament = new ArrayList<>();
        // Category 3: Played as GK before but NOT in most recent tournament (MEDIUM priority)
        List<GoalKeeperPriorityDto> playedButNotInMostRecent = new ArrayList<>();

        for (TournamentParticipant participant : participants) {
            Player player = participant.getPlayer();

            // Get goalkeeper history excluding current tournament
            List<PlayerGoalkeepingHistory> previousGoalKeeperHistory =
                    goalkeepingHistoryRepository.findGoalKeeperHistoryExcludingTournament(
                            player.getId(), tournamentId);

            Integer countPreviousTournaments = goalkeepingHistoryRepository
                    .countGoalKeeperHistoryExcludingTournament(player.getId(), tournamentId);

            if (previousGoalKeeperHistory.isEmpty()) {
                // HIGHEST PRIORITY: Never played as goalkeeper
                neverPlayedAsGK.add(GoalKeeperPriorityDto.builder()
                        .playerId(player.getId())
                        .playerName(player.getName())
                        .employeeId(player.getEmployeeId())
                        .previousGoalKeepingTournaments(0)
                        .wasGoalKeeperInMostRecentTournament(false)
                        .playAsGkDates(new ArrayList<>())
                        .build());
            } else {
                // Get the most recent goalkeeper date
                LocalDateTime lastGoalKeeperDate = previousGoalKeeperHistory.get(0).getPlayedDate();

                // Get all goalkeeper dates and format as dd-MM-yy
                List<LocalDateTime> allGoalKeeperDateTimes = goalkeepingHistoryRepository
                        .findAllGoalKeeperDates(player.getId(), tournamentId);

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yy");
                List<String> formattedGoalKeeperDates = allGoalKeeperDateTimes.stream()
                        .map(dateTime -> dateTime.format(dateFormatter))
                        .toList();

                // Check if they were goalkeeper in the most recent tournament
                boolean wasGKInMostRecent = false;
                if (mostRecentTournament != null) {
                    wasGKInMostRecent = goalkeepingHistoryRepository
                            .wasGoalKeeperInTournament(player.getId(), mostRecentTournament.getId());
                }

                GoalKeeperPriorityDto dto = GoalKeeperPriorityDto.builder()
                        .playerId(player.getId())
                        .playerName(player.getName())
                        .employeeId(player.getEmployeeId())
                        .previousGoalKeepingTournaments(countPreviousTournaments)
                        .wasGoalKeeperInMostRecentTournament(wasGKInMostRecent)
                        .playAsGkDates(formattedGoalKeeperDates)
                        .build();

                if (wasGKInMostRecent) {
                    playedInMostRecentTournament.add(dto);
                } else {
                    playedButNotInMostRecent.add(dto);
                }
            }
        }


        // Build priority queue:
        // 1. Never played as GK (HIGHEST - encourage first-timers)
        for (GoalKeeperPriorityDto dto : neverPlayedAsGK) {
            dto.setPriority(priority++);
            priorityQueue.add(dto);
        }

        // 2. Played before but not in most recent tournament (MEDIUM - fair rotation)
        for (GoalKeeperPriorityDto dto : playedButNotInMostRecent) {
            dto.setPriority(priority++);
            priorityQueue.add(dto);
        }

        // 3. Played in most recent tournament (LOWEST - they just played)
        for (GoalKeeperPriorityDto dto : playedInMostRecentTournament) {
            dto.setPriority(priority++);
            priorityQueue.add(dto);
        }

        return GoalKeeperQueueResponseDto.builder()
                .tournamentId(tournamentId)
                .tournamentName(tournament.getName())
                .tournamentDate(tournament.getTournamentDate())
                .goalKeeperPriorityQueue(priorityQueue)
                .build();
    }
}
