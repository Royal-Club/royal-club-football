package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.PlayerRoleAssignmentRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerWithRolesResponse;
import com.bjit.royalclub.royalclubfootball.model.RoleRequest;
import com.bjit.royalclub.royalclubfootball.model.RoleResponse;
import com.bjit.royalclub.royalclubfootball.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.DELETE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.UPDATE_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    @PreAuthorize("hasAnyRole('SUPERADMIN')")
    @PostMapping
    public ResponseEntity<Object> createRole(@Valid @RequestBody RoleRequest roleRequest) {
        RoleResponse roleResponse = roleService.createRole(roleRequest);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK, roleResponse);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<Object> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, roles);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<Object> getRoleById(@PathVariable Long id) {
        RoleResponse roleResponse = roleService.getRoleById(id);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, roleResponse);
    }

    @PreAuthorize("hasAnyRole('SUPERADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return buildSuccessResponse(HttpStatus.OK, DELETE_OK);
    }

    @PreAuthorize("hasAnyRole('SUPERADMIN')")
    @PutMapping("/assign")
    public ResponseEntity<Object> assignPlayerRoles(
            @Valid @RequestBody PlayerRoleAssignmentRequest playerRoleAssignmentRequest) {
        List<PlayerWithRolesResponse> playerResponses = roleService.batchAssignRolesToPlayers(playerRoleAssignmentRequest);
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, playerResponses);
    }

    @PreAuthorize("hasAnyRole('SUPERADMIN')")
    @GetMapping("/players/all-with-roles")
    public ResponseEntity<Object> getAllPlayersWithRoles() {
        List<PlayerWithRolesResponse> players = roleService.getAllPlayersWithRoles();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, players);
    }

    @PreAuthorize("hasAnyRole('SUPERADMIN')")
    @GetMapping("/players/{playerId}/with-roles")
    public ResponseEntity<Object> getPlayerWithRoles(@PathVariable Long playerId) {
        PlayerWithRolesResponse playerResponse = roleService.getPlayerWithRoles(playerId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, playerResponse);
    }
}

