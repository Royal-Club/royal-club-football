package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.model.GoalKeeperHistoryDto;
import com.bjit.royalclub.royalclubfootball.model.PlayerRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerResponse;
import com.bjit.royalclub.royalclubfootball.model.PlayerUpdateRequest;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PlayerService {
    @Transactional
    void registerPlayer(PlayerRegistrationRequest registrationRequest);

    List<PlayerResponse> getAllPlayers();

    PlayerResponse getPlayerById(Long id);

    Player getPlayerEntity(Long id);

    PlayerResponse getPlayerResponse(Player player);

    Set<PlayerResponse> getPlayerResponses(Set<Player> players);

    @Transactional
    void updatePlayerStatus(Long id, boolean active);

    @Transactional
    PlayerResponse updatePlayer(Long id, PlayerUpdateRequest updateRequest);

    Player findByEmail(String userName);

    Map<Integer, List<GoalKeeperHistoryDto>> goalKeepingHistory();

}
