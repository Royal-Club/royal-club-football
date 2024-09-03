package com.bjit.royalclub.royalclubfootball.model.account;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AcChartResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private AcChartResponse parent;
    private Long parentNo;
    private AcNatureResponse nature;
    private Long natureNo;
    private boolean isActive;

}
