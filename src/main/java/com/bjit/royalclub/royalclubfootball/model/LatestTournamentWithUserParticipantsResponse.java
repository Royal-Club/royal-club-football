package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class LatestTournamentWithUserParticipantsResponse {
    private TournamentResponse tournament;
    private int totalParticipant;
    private int remainParticipant;
    private int totalPlayer;
    private boolean isUserParticipated;

}