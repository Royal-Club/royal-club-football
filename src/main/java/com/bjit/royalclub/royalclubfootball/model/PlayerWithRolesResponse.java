package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.FootballPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerWithRolesResponse {
    private Long id;
    private String name;
    private String email;
    private String mobileNo;
    private String skypeId;
    private String employeeId;
    private boolean isActive;
    private FootballPosition playingPosition;
    private Set<RoleResponse> roles;
}

