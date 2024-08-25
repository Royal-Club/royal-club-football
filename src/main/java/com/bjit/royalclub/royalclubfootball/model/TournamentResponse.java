package com.bjit.royalclub.royalclubfootball.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY) // This will exclude fields with null or empty values
public class TournamentResponse {
    private Long id;
    private String tournamentName;
    private LocalDateTime tournamentDate;
    private String venueName;
    private boolean activeStatus;
    private List<TournamentTeamResponse> teams;
}
