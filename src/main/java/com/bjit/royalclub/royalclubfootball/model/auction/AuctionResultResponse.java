package com.bjit.royalclub.royalclubfootball.model.auction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionResultResponse {
    private Long tournamentId;
    private String tournamentName;
    private List<TeamSquadResponse> teamSquads;
    private List<AuctionPlayerResponse> unsoldPlayers;
    private AuctionStatsResponse statistics;
}
