package com.bjit.royalclub.royalclubfootball.config;

import com.bjit.royalclub.royalclubfootball.storage.LocalStorageProvider;
import com.bjit.royalclub.royalclubfootball.storage.R2StorageProvider;
import com.bjit.royalclub.royalclubfootball.storage.StorageProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "app.storage.player.provider", havingValue = "local", matchIfMissing = true)
    public StorageProvider localStorageProvider(
            @Value("${app.storage.player.upload-dir:./uploads/player-profiles}") String uploadDir,
            @Value("${app.storage.player.base-url:http://localhost:${server.port}}") String baseUrl
    ) {
        return new LocalStorageProvider(uploadDir, baseUrl);
    }

    @Bean
    @ConditionalOnProperty(name = "app.storage.player.provider", havingValue = "r2")
    public StorageProvider r2StorageProvider(
            @Value("${app.storage.player.r2.endpoint}") String endpoint,
            @Value("${app.storage.player.r2.bucket}") String bucket,
            @Value("${app.storage.player.r2.access-key}") String accessKey,
            @Value("${app.storage.player.r2.secret-key}") String secretKey,
            @Value("${app.storage.player.r2.presign-duration-minutes:60}") long presignDurationMinutes
    ) {
        return new R2StorageProvider(endpoint, bucket, accessKey, secretKey, presignDurationMinutes);
    }
}
