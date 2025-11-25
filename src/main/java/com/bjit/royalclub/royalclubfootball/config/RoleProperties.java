package com.bjit.royalclub.royalclubfootball.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class RoleProperties {
    @Value("${player.superadmin-email:}")
    private String superadminEmail;
}

