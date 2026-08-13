package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.model.GoalKeeperHistoryDto;
import com.bjit.royalclub.royalclubfootball.model.GoalKeeperQueueResponseDto;
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

    Set<Player> getPlayerEntities(java.util.Collection<Long> ids);

    PlayerResponse getPlayerResponse(Player player);

    Set<PlayerResponse> getPlayerResponses(Set<Player> players);

    @Transactional
    void updatePlayerStatus(Long id, boolean active);

    @Transactional
    PlayerResponse updatePlayer(Long id, PlayerUpdateRequest updateRequest);

    /**
     * Points a player at an already-uploaded photo, without touching the rest of their profile.
     * <p>
     * Exists because {@link PlayerUpdateRequest} makes name, email, employee ID, Skype ID and
     * position mandatory: a client that only wants to change a photo would otherwise have to resend
     * the whole profile, and would silently drop any field it did not know about.
     */
    PlayerResponse updatePhoto(Long id, String photoKey);

    Player findByEmail(String userName);

    Map<Integer, List<GoalKeeperHistoryDto>> goalKeepingHistory();

    int countActivePlayers();

    List<GoalKeeperHistoryDto> getGoalKeeperHistoryByLoggedInUser();

    GoalKeeperQueueResponseDto getGoalKeeperPriorityQueue(Long tournamentId);

}
