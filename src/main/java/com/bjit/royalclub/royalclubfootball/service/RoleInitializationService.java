package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.config.RoleProperties;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Role;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleInitializationService {

    private final RoleProperties roleProperties;
    private final PlayerRepository playerRepository;
    private final RoleRepository roleRepository;

    private static final String SUPERADMIN_ROLE = "SUPERADMIN";
    private static final String PLAYER_ROLE = "PLAYER";

    /**
     * Initialize superadmin role assignment when application starts
     * This reads the email from properties and assigns SUPERADMIN role to that player
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeSuperadminRole() {
        String superadminEmail = roleProperties.getSuperadminEmail();

        // Only proceed if superadmin email is configured
        if (superadminEmail == null || superadminEmail.trim().isEmpty()) {
            log.info("Superadmin email not configured. Skipping superadmin initialization.");
            return;
        }

        try {
            // Find the player with the superadmin email
            Optional<Player> playerOptional = playerRepository.findByEmail(superadminEmail);

            if (playerOptional.isEmpty()) {
                log.warn("Player with email '{}' not found. Cannot assign SUPERADMIN role.", superadminEmail);
                return;
            }

            Player player = playerOptional.get();

            // Get SUPERADMIN and PLAYER roles
            Optional<Role> superadminRoleOptional = roleRepository.findByName(SUPERADMIN_ROLE);
            Optional<Role> playerRoleOptional = roleRepository.findByName(PLAYER_ROLE);

            if (superadminRoleOptional.isEmpty() || playerRoleOptional.isEmpty()) {
                log.error("Required roles (SUPERADMIN or PLAYER) not found in database.");
                return;
            }

            Role superadminRole = superadminRoleOptional.get();
            Role playerRole = playerRoleOptional.get();

            // Check if player already has SUPERADMIN role
            boolean hasSuperadmin = player.getRoles().stream()
                    .anyMatch(r -> SUPERADMIN_ROLE.equals(r.getName()));

            if (hasSuperadmin) {
                log.info("Player with email '{}' already has SUPERADMIN role.", superadminEmail);
                return;
            }

            Set<Role> roles = player.getRoles();
            roles.add(superadminRole);
            roles.add(playerRole);
            player.setRoles(roles);

            playerRepository.save(player);
            log.info("Successfully assigned SUPERADMIN role to player with email '{}'", superadminEmail);

        } catch (Exception e) {
            log.error("Error during superadmin role initialization: {}", e.getMessage(), e);
        }
    }
}

