package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.GoalkeeperStatsResponse;
import com.bjit.royalclub.royalclubfootball.model.LatestTournamentWithParticipantsResponse;
import com.bjit.royalclub.royalclubfootball.model.PlayerParticipationResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentParticipantRequest;

import java.util.List;

public interface TournamentParticipantService {
    void saveOrUpdateTournamentParticipant(TournamentParticipantRequest tournamentParticipantRequest);

    List<PlayerParticipationResponse> playersToBeSelectedForTeam(Long tournamentId);

    List<GoalkeeperStatsResponse> goalkeeperStatsResponse(Long tournamentId);

    LatestTournamentWithParticipantsResponse getLatestTournamentWithParticipants();

}
