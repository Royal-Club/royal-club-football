package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.FootballPosition;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class PlayerResponse {
    private Long id;
    private String name;
    private String email;
    private String mobileNo;
    private String skypeId;
    private String employeeId;
    private String fullName;
    private boolean isActive;
    private FootballPosition playingPosition;
    private Set<RoleResponse> roles;
    private String photoKey;
    private String photoUrl;
}
