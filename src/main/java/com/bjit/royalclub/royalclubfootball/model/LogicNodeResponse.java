package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogicNodeResponse {

    private Long id;
    private Long tournamentId;
    private String tournamentName;
    private String nodeName;
    private String nodeType;
    private Long sourceRoundId;
    private String sourceRoundName;
    private Long sourceGroupId;
    private String sourceGroupName;
    private Long targetRoundId;
    private String targetRoundName;
    private String ruleConfig;
    private Integer priorityOrder;
    private Boolean isActive;
    private Boolean autoExecute;
    private Integer executionCount;
    private LocalDateTime lastExecutedAt;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}

