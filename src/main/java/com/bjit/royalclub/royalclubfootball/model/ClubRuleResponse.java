package com.bjit.royalclub.royalclubfootball.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClubRuleResponse {
    private Long id;
    private String description;
}
