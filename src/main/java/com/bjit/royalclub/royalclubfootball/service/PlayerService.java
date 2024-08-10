package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.PlayerRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface PlayerService {
    @Transactional
    void registerPlayer(PlayerRegistrationRequest registrationRequest);

    List<PlayerResponse> getAllPlayers();
}
