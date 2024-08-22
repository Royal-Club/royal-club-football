package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.FootballPosition;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerResponse {
    private Long id;
    private String name;
    private String email;
    private String mobileNo;
    private String skypeId;
    private String employeeId;
    private boolean isActive;
    private FootballPosition playingPosition;
}
