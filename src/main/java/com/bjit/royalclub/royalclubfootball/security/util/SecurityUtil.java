package com.bjit.royalclub.royalclubfootball.security.util;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.exception.SecurityException;
import com.bjit.royalclub.royalclubfootball.security.UserPrinciple;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_FOUND;

public class SecurityUtil {

    private SecurityUtil() {
    }

    public static UserPrinciple getLoggedInUser() {
        return (UserPrinciple) getAuthentication().getPrincipal();
    }

    public static Long getLoggedInUserId() {
        return getLoggedInUser().getId();
    }

    public static Player getLoggedInPlayer() {
        return getLoggedInUser().getPlayer();
    }

    public static Boolean isUserAuthorizedForSelf(Long id) {
        return id.equals(getLoggedInUserId());
    }

    /**
     * True when the signed-in player holds any of the named roles.
     * <p>
     * Reads the roles off the principal rather than the authorities so callers pass plain names
     * ("ADMIN") without the ROLE_ prefix Spring adds.
     */
    public static boolean hasAnyRole(String... roleNames) {
        Player player = getLoggedInPlayer();
        if (player == null || player.getRoles() == null) {
            return false;
        }
        Set<String> wanted = Set.of(roleNames);
        return player.getRoles().stream().anyMatch(role -> wanted.contains(role.getName()));
    }

    /**
     * Whoever runs a match day: the people allowed to record an answer for another player and to
     * keep editing once voting is locked.
     */
    public static boolean isTournamentManager() {
        return hasAnyRole("ADMIN", "SUPERADMIN", "COORDINATOR");
    }

    private static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrinciple)) {
            throw new SecurityException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return authentication;
    }
}
