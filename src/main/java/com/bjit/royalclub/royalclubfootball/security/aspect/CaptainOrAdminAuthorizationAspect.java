package com.bjit.royalclub.royalclubfootball.security.aspect;

import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.security.annotation.RequiresCaptainOrAdmin;
import com.bjit.royalclub.royalclubfootball.security.util.SecurityUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CaptainOrAdminAuthorizationAspect {

    @Before("@annotation(requiresCaptainOrAdmin)")
    public void validateCaptainOrAdmin(JoinPoint joinPoint, RequiresCaptainOrAdmin requiresCaptainOrAdmin) {
        var loggedInUser = SecurityUtil.getLoggedInUser();
        boolean isAdmin = loggedInUser.getAuthorities()
                .stream()
                .anyMatch(auth -> auth instanceof SimpleGrantedAuthority && auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new TournamentServiceException(
                    requiresCaptainOrAdmin.value(),
                    HttpStatus.FORBIDDEN
            );
        }
    }
}
