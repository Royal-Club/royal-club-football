package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Role;
import com.bjit.royalclub.royalclubfootball.enums.PlayerRole;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.exception.SecurityException;
import com.bjit.royalclub.royalclubfootball.model.GoalKeeperHistoryDto;
import com.bjit.royalclub.royalclubfootball.model.PlayerRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerResponse;
import com.bjit.royalclub.royalclubfootball.model.PlayerUpdateRequest;
import com.bjit.royalclub.royalclubfootball.repository.PlayerGoalkeepingHistoryRepository;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
import static com.bjit.royalclub.royalclubfootball.security.util.SecurityUtil.isUserAuthorizedForSelf;
import static com.bjit.royalclub.royalclubfootball.util.StringUtils.normalizeString;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    private final PlayerGoalkeepingHistoryRepository goalkeepingHistoryRepository;

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
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
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
        if (Boolean.FALSE.equals(isUserAuthorizedForSelf(id))) {
            throw new SecurityException(UNAUTHORIZED, HttpStatus.EXPECTATION_FAILED);
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
            roundList.sort(Comparator.comparing(GoalKeeperHistoryDto::getPlayerId));
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
}
