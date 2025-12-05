package com.bjit.royalclub.royalclubfootball.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TournamentRoundResponse {

    private Long id;
    private Long tournamentId;
    private String tournamentName;
    private Integer roundNumber;
    private String roundName;
    private String roundType;
    private String roundFormat;
    private String advancementRule;
    private String status;
    private Integer sequenceOrder;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Nested data
    private List<RoundGroupResponse> groups;
    private List<TeamSimpleResponse> teams;
    private Integer totalMatches;
    private Integer completedMatches;
}
