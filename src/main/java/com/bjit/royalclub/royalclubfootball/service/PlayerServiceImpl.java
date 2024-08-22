package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.model.PlayerRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerResponse;
import com.bjit.royalclub.royalclubfootball.model.PlayerUpdateRequest;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.util.StringUtils.normalizeString;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    @Override
    public void registerPlayer(PlayerRegistrationRequest registrationRequest) {

        playerRepository.findByEmail(registrationRequest.getEmail())
                .ifPresent(player -> {
                    throw new PlayerServiceException(RestErrorMessageDetail.PLAYER_ALREADY_EXISTS, HttpStatus.CONFLICT);
                });
        Player player = Player.builder()
                .email(registrationRequest.getEmail())
                .name(registrationRequest.getName())
                .employeeId(registrationRequest.getEmployeeId())
                .mobileNo(registrationRequest.getMobileNo())
                .skypeId(registrationRequest.getSkypeId())
                .position(registrationRequest.getPlayingPosition())
                /*this will be open API so, admin will activate it*/
                .isActive(false)
                .createdDate(LocalDateTime.now())
                .build();
        playerRepository.save(player);
    }

    @Override
    public List<PlayerResponse> getAllPlayers() {
        List<Player> players = playerRepository.findAll();
        return players.stream()
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
    public void updatePlayerStatus(Long id, boolean active) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        player.setActive(active);
        player.setUpdatedDate(LocalDateTime.now());
        playerRepository.save(player);
    }

    @Override
    public PlayerResponse updatePlayer(Long id, PlayerUpdateRequest updateRequest) {
        Player player;
        player = playerRepository.findById(id)
                .orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        player.setEmail(updateRequest.getEmail());
        player.setName(updateRequest.getName());
        player.setEmployeeId(updateRequest.getEmployeeId());
        player.setMobileNo(normalizeString(updateRequest.getMobileNo()));
        player.setSkypeId(updateRequest.getSkypeId());
        player.setPosition(updateRequest.getPlayingPosition());
        player.setUpdatedDate(LocalDateTime.now());
        player = playerRepository.save(player);
        return convertToDto(player);
    }

    private PlayerResponse convertToDto(Player player) {
        return PlayerResponse.builder()
                .id(player.getId())
                .name(player.getName())
                .email(player.getEmail())
                .mobileNo(player.getMobileNo())
                .skypeId(player.getSkypeId())
                .employeeId(player.getEmployeeId())
                .playingPosition(player.getPosition())
                .isActive(player.isActive())
                .build();
    }
}
