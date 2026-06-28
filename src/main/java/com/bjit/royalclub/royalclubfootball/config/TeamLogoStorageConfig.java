package com.bjit.royalclub.royalclubfootball.config;

import com.bjit.royalclub.royalclubfootball.storage.teamlogo.TeamLogoLocalStorageProvider;
import com.bjit.royalclub.royalclubfootball.storage.teamlogo.TeamLogoR2StorageProvider;
import com.bjit.royalclub.royalclubfootball.storage.teamlogo.TeamLogoStorageProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TeamLogoStorageConfig {

    @Bean
    @ConditionalOnProperty(name = "app.team-logo.storage-provider", havingValue = "local", matchIfMissing = true)
    public TeamLogoStorageProvider localTeamLogoStorageProvider(
            @Value("${app.team-logo.upload-dir:uploads/team-logos}") String uploadDir,
            @Value("${app.team-logo.base-url:http://localhost:9191}") String baseUrl) {
        return new TeamLogoLocalStorageProvider(uploadDir, baseUrl);
    }

    @Bean
    @ConditionalOnProperty(name = "app.team-logo.storage-provider", havingValue = "r2")
    public TeamLogoStorageProvider r2TeamLogoStorageProvider(
            @Value("${app.team-logo.r2.endpoint:}") String endpoint,
            @Value("${app.team-logo.r2.bucket:}") String bucket,
            @Value("${app.team-logo.r2.access-key:}") String accessKey,
            @Value("${app.team-logo.r2.secret-key:}") String secretKey,
            @Value("${app.team-logo.r2.presign-duration-minutes:60}") long presignDurationMinutes) {

        if (endpoint.isBlank() || bucket.isBlank() || accessKey.isBlank() || secretKey.isBlank()) {
            throw new IllegalStateException("R2 storage selected but one or more required R2 settings are missing");
        }

        return new TeamLogoR2StorageProvider(endpoint, bucket, accessKey, secretKey, presignDurationMinutes);
    }
}
