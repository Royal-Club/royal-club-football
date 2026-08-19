package com.bjit.royalclub.royalclubfootball.config;

import com.bjit.royalclub.royalclubfootball.storage.teamchat.TeamChatFileLocalStorageProvider;
import com.bjit.royalclub.royalclubfootball.storage.teamchat.TeamChatFileR2StorageProvider;
import com.bjit.royalclub.royalclubfootball.storage.teamchat.TeamChatFileStorageProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TeamChatFileStorageConfig {

    @Bean
    @ConditionalOnProperty(name = "app.team-chat-file.storage-provider", havingValue = "local", matchIfMissing = true)
    public TeamChatFileStorageProvider localTeamChatFileStorageProvider(
            @Value("${app.team-chat-file.upload-dir:uploads/team-chat}") String uploadDir,
            @Value("${app.team-chat-file.base-url:http://localhost:9191}") String baseUrl) {
        return new TeamChatFileLocalStorageProvider(uploadDir, baseUrl);
    }

    @Bean
    @ConditionalOnProperty(name = "app.team-chat-file.storage-provider", havingValue = "r2")
    public TeamChatFileStorageProvider r2TeamChatFileStorageProvider(
            @Value("${app.team-chat-file.r2.endpoint:}") String endpoint,
            @Value("${app.team-chat-file.r2.bucket:}") String bucket,
            @Value("${app.team-chat-file.r2.access-key:}") String accessKey,
            @Value("${app.team-chat-file.r2.secret-key:}") String secretKey,
            @Value("${app.team-chat-file.r2.presign-duration-minutes:60}") long presignDurationMinutes) {

        if (endpoint.isBlank() || bucket.isBlank() || accessKey.isBlank() || secretKey.isBlank()) {
            throw new IllegalStateException("R2 storage selected but one or more required R2 settings are missing");
        }

        return new TeamChatFileR2StorageProvider(endpoint, bucket, accessKey, secretKey, presignDurationMinutes);
    }
}
