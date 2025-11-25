package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.config.RoleProperties;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Role;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.model.PlayerRoleAssignmentRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerWithRolesResponse;
import com.bjit.royalclub.royalclubfootball.model.RoleRequest;
import com.bjit.royalclub.royalclubfootball.model.RoleResponse;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PlayerRepository playerRepository;
    private final RoleProperties roleProperties;

    private static final String SUPERADMIN_ROLE = "SUPERADMIN";
    private static final String PLAYER_ROLE = "PLAYER";

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest roleRequest) {
        roleRepository.findByName(roleRequest.getName()).ifPresent(role -> {
            throw new PlayerServiceException("Role with name '" + roleRequest.getName() + "' already exists", HttpStatus.CONFLICT);
        });

        Role role = Role.builder()
                .name(roleRequest.getName())
                .build();
        Role savedRole = roleRepository.save(role);
        return convertToDto(savedRole);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new PlayerServiceException("Role not found", HttpStatus.NOT_FOUND));
        return convertToDto(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new PlayerServiceException("Role not found", HttpStatus.NOT_FOUND));

        if (role.getPlayers() != null && !role.getPlayers().isEmpty()) {
            throw new PlayerServiceException("Cannot delete role that is assigned to players", HttpStatus.BAD_REQUEST);
        }

        roleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<PlayerWithRolesResponse> batchAssignRolesToPlayers(
            PlayerRoleAssignmentRequest playerRoleAssignmentRequest) {
        Map<Long, Set<Long>> playerRoleMappings = playerRoleAssignmentRequest.getPlayerRoleMappings();

        // Pre-fetch all unique roles to avoid repeated queries
        Set<Long> allRoleIds = playerRoleMappings.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        Map<Long, Role> roleCache = new java.util.HashMap<>();
        for (Long roleId : allRoleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new PlayerServiceException("Role with ID " + roleId + " not found", HttpStatus.NOT_FOUND));
            roleCache.put(roleId, role);
        }

        // Assign different roles to each player
        List<Player> playersToUpdate = new ArrayList<>();
        for (Map.Entry<Long, Set<Long>> entry : playerRoleMappings.entrySet()) {
            Long playerId = entry.getKey();
            Set<Long> roleIds = entry.getValue();

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new PlayerServiceException("Player with ID " + playerId + " not found", HttpStatus.NOT_FOUND));

            Set<Role> rolesToAssign = roleIds.stream()
                    .map(roleCache::get)
                    .collect(Collectors.toSet());

            // Validate role assignment (protect SUPERADMIN and ensure PLAYER role exists)
            validateRoleAssignment(player, rolesToAssign);

            player.setRoles(rolesToAssign);
            playersToUpdate.add(player);
        }

        // Save all in a single batch
        List<Player> updatedPlayers = playerRepository.saveAll(playersToUpdate);

        return updatedPlayers.stream()
                .map(this::convertPlayerToDto)
                .toList();
    }

    @Override
    public List<PlayerWithRolesResponse> getAllPlayersWithRoles() {
        return playerRepository.findAll()
                .stream()
                .map(this::convertPlayerToDto)
                .toList();
    }

    @Override
    public PlayerWithRolesResponse getPlayerWithRoles(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        return convertPlayerToDto(player);
    }

    /**
     * Validate role assignment:
     * 1. No player can have empty roles (at least one role required)
     * 2. All players must have at least PLAYER role
     * 3. Superadmin user (from config) cannot lose SUPERADMIN role
     */
    private void validateRoleAssignment(Player player, Set<Role> newRoles) {
        // Rule 1: No player can have empty roles
        if (newRoles == null || newRoles.isEmpty()) {
            throw new PlayerServiceException("Player (" + player.getEmail() + ") must have at least one role. Cannot assign empty roles.", HttpStatus.BAD_REQUEST);
        }

        // Rule 2: All players must have at least PLAYER role
        boolean hasPlayerRole = newRoles.stream()
                .anyMatch(r -> PLAYER_ROLE.equals(r.getName()));
        if (!hasPlayerRole) {
            throw new PlayerServiceException("Player (" + player.getEmail() + ") must have at least PLAYER role", HttpStatus.BAD_REQUEST);
        }

        // Rule 3: If this is the configured superadmin, cannot remove SUPERADMIN role
        if (roleProperties.getSuperadminEmail() != null &&
                !roleProperties.getSuperadminEmail().isEmpty() &&
                roleProperties.getSuperadminEmail().equalsIgnoreCase(player.getEmail())) {

            boolean hasSuperAdmin = player.getRoles().stream()
                    .anyMatch(r -> SUPERADMIN_ROLE.equals(r.getName()));

            if (hasSuperAdmin) {
                boolean superAdminInNewRoles = newRoles.stream()
                        .anyMatch(r -> SUPERADMIN_ROLE.equals(r.getName()));
                if (!superAdminInNewRoles) {
                    throw new PlayerServiceException("Cannot remove SUPERADMIN role from superadmin user (" + player.getEmail() + ")", HttpStatus.BAD_REQUEST);
                }
            }
        }
    }

    private RoleResponse convertToDto(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }

    private PlayerWithRolesResponse convertPlayerToDto(Player player) {
        Set<RoleResponse> roleResponses = player.getRoles() != null
                ? player.getRoles().stream()
                .map(this::convertToDto)
                .collect(Collectors.toSet())
                : new HashSet<>();

        return PlayerWithRolesResponse.builder()
                .id(player.getId())
                .name(player.getName())
                .email(player.getEmail())
                .mobileNo(player.getMobileNo())
                .skypeId(player.getSkypeId())
                .employeeId(player.getEmployeeId())
                .isActive(player.isActive())
                .playingPosition(player.getPosition())
                .roles(roleResponses)
                .build();
    }
}

