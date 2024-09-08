package com.bjit.royalclub.royalclubfootball.model.account;

import com.bjit.royalclub.royalclubfootball.enums.AcNatureType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AcNatureResponse {
    private Long id;
    private String name;
    private Integer code;
    private AcNatureType type;
    private int slNo;
}
