package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.PlayerRoleAssignmentRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerWithRolesResponse;
import com.bjit.royalclub.royalclubfootball.model.RoleRequest;
import com.bjit.royalclub.royalclubfootball.model.RoleResponse;

import java.util.List;

public interface RoleService {

    RoleResponse createRole(RoleRequest roleRequest);

    List<RoleResponse> getAllRoles();

    RoleResponse getRoleById(Long id);

    void deleteRole(Long id);

    List<PlayerWithRolesResponse> batchAssignRolesToPlayers(PlayerRoleAssignmentRequest playerRoleAssignmentRequest);

    List<PlayerWithRolesResponse> getAllPlayersWithRoles();

    PlayerWithRolesResponse getPlayerWithRoles(Long playerId);
}

