package com.bjit.royalclub.royalclubfootball.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamSimpleResponse {

    private Long id;
    private String teamName;
    private Boolean isPlaceholder;
    private String placeholderName;
    private Integer seedPosition;
    private String assignmentType;
}
