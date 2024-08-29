package com.bjit.royalclub.royalclubfootball.util;


import com.bjit.royalclub.royalclubfootball.exception.SecurityException;
import com.bjit.royalclub.royalclubfootball.security.UserPrinciple;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

    private static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrinciple)) {
            throw new SecurityException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return authentication;
    }
}
