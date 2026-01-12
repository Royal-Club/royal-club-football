package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.enums.PrizeType;
import com.bjit.royalclub.royalclubfootball.model.TournamentPrizeRequest;
import com.bjit.royalclub.royalclubfootball.model.TournamentPrizeResponse;

import java.util.List;

public interface TournamentPrizeService {

    TournamentPrizeResponse createPrize(TournamentPrizeRequest request);

    TournamentPrizeResponse updatePrize(Long prizeId, TournamentPrizeRequest request);

    void deletePrize(Long prizeId);

    TournamentPrizeResponse getPrizeById(Long prizeId);

    List<TournamentPrizeResponse> getAllPrizesByTournament(Long tournamentId);

    List<TournamentPrizeResponse> getPrizesByTournamentAndType(Long tournamentId, PrizeType prizeType);

    List<TournamentPrizeResponse> getPrizesByTeam(Long tournamentId, Long teamId);

    List<TournamentPrizeResponse> getPrizesByPlayer(Long tournamentId, Long playerId);
}
