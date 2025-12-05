package com.bjit.royalclub.royalclubfootball.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TournamentStructureResponse {

    private Long tournamentId;
    private String tournamentName;
    private String sportType;
    private String tournamentType;
    private String status;
    private List<TournamentRoundResponse> rounds;
    private Integer totalRounds;
    private Integer totalMatches;
    private Integer completedMatches;
}
