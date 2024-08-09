package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.PlayerRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerResponse;

import java.util.List;

public interface PlayerService {
    void registerPlayer(PlayerRegistrationRequest registrationRequest);

    List<PlayerResponse> getAllPlayers();
}
