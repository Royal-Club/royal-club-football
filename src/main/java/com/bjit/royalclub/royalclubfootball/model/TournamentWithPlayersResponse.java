package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentWithPlayersResponse {
    private Long tournamentId;
    private String tournamentName;
    private LocalDateTime tournamentDate;
    private Long totalParticipants;
    private List<PlayerParticipationResponse> players;
}
