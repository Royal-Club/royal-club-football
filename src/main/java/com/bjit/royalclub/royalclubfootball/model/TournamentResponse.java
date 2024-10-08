package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.TournamentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TournamentResponse {
    private Long id;
    private String name;
    private LocalDateTime tournamentDate;
    private String venueName;
    private boolean activeStatus;

    private TournamentStatus tournamentStatus;
    private List<TournamentTeamResponse> teams;
}
