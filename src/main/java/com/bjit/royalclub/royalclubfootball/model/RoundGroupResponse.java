package com.bjit.royalclub.royalclubfootball.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoundGroupResponse {

    private Long id;
    private Long roundId;
    private String roundName;
    private String groupName;
    private String groupFormat;
    private String advancementRule;
    private Integer maxTeams;
    private String status;

    // Nested data
    private List<TeamSimpleResponse> teams;
    private List<GroupStandingResponse> standings;
    private Integer totalMatches;
    private Integer completedMatches;
}
