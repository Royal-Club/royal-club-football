package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipantPlayer;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.model.PlayerParticipationResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentWithPlayersResponse;
import com.bjit.royalclub.royalclubfootball.repository.TournamentParticipantPlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TournamentParticipantPlayerServiceImpl implements TournamentParticipantPlayerService {

    private final TournamentParticipantPlayerRepository participantPlayerRepository;
    private final TournamentRepository tournamentRepository;

    private TournamentWithPlayersResponse buildTournamentWithPlayersResponse(List<TournamentParticipantPlayer> tournamentParticipantPlayers) {
        TournamentParticipantPlayer firstEntry = tournamentParticipantPlayers.get(0);
        List<PlayerParticipationResponse> players = tournamentParticipantPlayers.stream()
                .map(player -> PlayerParticipationResponse.builder()
                        .playerId(player.getPlayerId())
                        .playerName(player.getPlayerName())
                        .employeeId(player.getPlayerEmployeeId())
                        .participationStatus(player.getParticipationStatus())
                        .comments(player.getComments())
                        .build())
                .toList();
        return new TournamentWithPlayersResponse(
                firstEntry.getTournamentId(),
                firstEntry.getTournamentName(),
                firstEntry.getTournamentDate(),
                players
        );
    }

    @Override
    public TournamentWithPlayersResponse getNextTournamentForParticipation(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        List<TournamentParticipantPlayer> participants = participantPlayerRepository.findAllByTournamentId(tournament.getId());
        return buildTournamentWithPlayersResponse(participants);
    }
}
