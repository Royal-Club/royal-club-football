package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.model.PlayerRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    @Override
    public void registerPlayer(PlayerRegistrationRequest registrationRequest) {
        /*TODO("Need to Check already player register or now with the mail or employeeId")*/
        Player player = Player.builder()
                .email(registrationRequest.getEmail())
                .name(registrationRequest.getName())
                .employeeId(registrationRequest.getEmployeeId())
                .isActive(true)
                .createdDate(LocalDateTime.now())
                .build();
        playerRepository.save(player);
    }
}
