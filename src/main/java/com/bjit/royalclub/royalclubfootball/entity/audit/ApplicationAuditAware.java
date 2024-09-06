package com.bjit.royalclub.royalclubfootball.entity.audit;

import com.bjit.royalclub.royalclubfootball.security.UserPrinciple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Slf4j
public class ApplicationAuditAware implements AuditorAware<Long> {
    private static final Long SYSTEM_USER_ID = 0L; // Define system user ID

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken
        ) {
            // Log that the system user is being used for auditing
            log.info("No authenticated user found. Using SYSTEM_USER_ID for auditing.");
            return Optional.of(SYSTEM_USER_ID);
        }

        UserPrinciple userPrincipal = (UserPrinciple) authentication.getPrincipal();
        return Optional.ofNullable(userPrincipal.getId());
    }
}
