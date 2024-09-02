package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PaginatedTournamentResponse {
    private List<TournamentResponse> tournaments;
    private long totalCount;
}
