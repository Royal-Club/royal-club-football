package com.bjit.royalclub.royalclubfootball.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CostTypeResponse {
    private Long id;
    private String name;
    private String description;
    private boolean isActive;
}
