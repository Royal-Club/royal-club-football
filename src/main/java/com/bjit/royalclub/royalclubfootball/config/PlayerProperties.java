package com.bjit.royalclub.royalclubfootball.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "player")
public class PlayerProperties {
    private String defaultPassword;

    @PostConstruct
    public void validate() {
        if (defaultPassword == null || defaultPassword.isBlank()) {
            throw new IllegalStateException("Configuration property 'player.default-password' must be set and non-blank");
        }
    }
}
