package com.bjit.royalclub.royalclubfootball.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdvancedTeamsResponse {

    private Long sourceRoundId;
    private String sourceRoundName;
    private Long targetRoundId;
    private String targetRoundName;
    private Integer teamsAdvanced;
    private List<TeamAdvancementInfo> teams;

    @Builder
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TeamAdvancementInfo {
        private Long teamId;
        private String teamName;
        private String fromGroup;
        private Integer position;
        private Integer points;
        private Integer assignedToSeed;
        private String advancementReason;
    }
}
