package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentWithPlayersResponse {
    private Long tournamentId;
    private String tournamentName;
    private LocalDateTime tournamentDate;
    private List<PlayerParticipationResponse> players;
}
