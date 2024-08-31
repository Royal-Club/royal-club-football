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

import java.util.ArrayList;
import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_PARTICIPANT_YET;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TournamentParticipantPlayerServiceImpl implements TournamentParticipantPlayerService {

    private final TournamentParticipantPlayerRepository participantPlayerRepository;
    private final TournamentRepository tournamentRepository;

    private TournamentWithPlayersResponse buildTournamentWithPlayersResponse(List<TournamentParticipantPlayer>
                                                                                     tournamentParticipantPlayers) {
        if (tournamentParticipantPlayers == null || tournamentParticipantPlayers.isEmpty()) {
            throw new TournamentServiceException(PLAYER_IS_NOT_PARTICIPANT_YET, HttpStatus.NOT_FOUND);
        }
        TournamentParticipantPlayer firstEntry = tournamentParticipantPlayers.get(0);
        List<PlayerParticipationResponse> players = new ArrayList<>();

        long totalParticipants = tournamentParticipantPlayers.stream()
                .peek(player -> players.add(PlayerParticipationResponse.builder()
                        .playerId(player.getPlayerId())
                        .playerName(player.getPlayerName())
                        .employeeId(player.getPlayerEmployeeId())
                        .participationStatus(player.getParticipationStatus())
                        .tournamentParticipantId(player.getTournamentParticipantId())
                        .comments(player.getComments())
                        .build()))
                .filter(player -> Boolean.TRUE.equals(player.getParticipationStatus()))
                .count();
        return TournamentWithPlayersResponse.builder()
                .tournamentId(firstEntry.getTournamentId())
                .tournamentName(firstEntry.getTournamentName())
                .tournamentDate(firstEntry.getTournamentDate())
                .totalParticipants(totalParticipants)
                .players(players)
                .build();
    }

    @Override
    public TournamentWithPlayersResponse getNextTournamentForParticipation(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        List<TournamentParticipantPlayer> participants = participantPlayerRepository.findAllByTournamentId(tournament.getId());
        return buildTournamentWithPlayersResponse(participants);
    }
}
