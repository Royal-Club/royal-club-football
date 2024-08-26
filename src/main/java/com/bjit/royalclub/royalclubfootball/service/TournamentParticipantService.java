package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.GoalkeeperStatsResponse;
import com.bjit.royalclub.royalclubfootball.model.PlayerParticipationResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentParticipantRequest;

import java.util.List;

public interface TournamentParticipantService {
    void updateTournamentParticipant(TournamentParticipantRequest tournamentParticipantRequest);

    List<PlayerParticipationResponse> playersToBeSelectedForTeam(Long tournamentId);

    List<GoalkeeperStatsResponse> goalkeeperStatsResponse(Long tournamentId);
}
