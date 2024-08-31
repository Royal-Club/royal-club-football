package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.model.PlayerRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerResponse;
import com.bjit.royalclub.royalclubfootball.model.PlayerUpdateRequest;
import jakarta.transaction.Transactional;

import java.util.List;

public interface PlayerService {
    @Transactional
    void registerPlayer(PlayerRegistrationRequest registrationRequest);

    List<PlayerResponse> getAllPlayers();

    PlayerResponse getPlayerById(Long id);

    @Transactional
    void updatePlayerStatus(Long id, boolean active);

    @Transactional
    PlayerResponse updatePlayer(Long id, PlayerUpdateRequest updateRequest);

    Player findByEmail(String userName);

}
