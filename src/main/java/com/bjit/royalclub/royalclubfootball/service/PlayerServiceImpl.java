package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.model.PlayerRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerResponse;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    @Override
    public void registerPlayer(PlayerRegistrationRequest registrationRequest) {
        if (playerRepository.findByEmail(registrationRequest.getEmail()).isPresent()) {
            throw new PlayerServiceException(RestErrorMessageDetail.PLAYER_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }
//        playerRepository.findByEmail(registrationRequest.getEmail())
//                .orElseThrow(() -> new PlayerServiceException(RestErrorMessageDetail.PLAYER_ALREADY_EXISTS,
//                        HttpStatus.CONFLICT));
        Player player = Player.builder()
                .email(registrationRequest.getEmail())
                .name(registrationRequest.getName())
                .employeeId(registrationRequest.getEmployeeId())
                .mobileNo(registrationRequest.getMobileNo())
                .skypeId(registrationRequest.getSkypeId())
                .isActive(registrationRequest.isActive())
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

    private PlayerResponse convertToDto(Player player) {
        return PlayerResponse.builder()
                .id(player.getId())
                .name(player.getName())
                .email(player.getEmail())
                .mobileNo(player.getMobileNo())
                .skypeId(player.getSkypeId())
                .isActive(player.isActive())
                .build();
    }
}
