package com.bjit.royalclub.royalclubfootball.util;

import com.bjit.royalclub.royalclubfootball.security.UserPrinciple;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Optional;

/**
 * Small read-only helpers over the security context. Endpoint authorisation
 * stays with {@code @PreAuthorize}; these are for shaping responses to the
 * caller, e.g. hiding drafts from players.
 */
public final class CurrentUserUtil {

    private CurrentUserUtil() {
    }

    public static Optional<Long> currentPlayerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrinciple principle)) {
            return Optional.empty();
        }
        return Optional.ofNullable(principle.getId());
    }

    /**
     * @param roles role names without the {@code ROLE_} prefix, e.g. {@code "ADMIN"}
     */
    public static boolean hasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> Arrays.stream(roles)
                        .anyMatch(role -> ("ROLE_" + role).equals(authority.getAuthority())));
    }
}
