package com.bjit.royalclub.royalclubfootball.config;

import com.bjit.royalclub.royalclubfootball.storage.playerphoto.PlayerPhotoLocalStorageProvider;
import com.bjit.royalclub.royalclubfootball.storage.playerphoto.PlayerPhotoR2StorageProvider;
import com.bjit.royalclub.royalclubfootball.storage.playerphoto.PlayerPhotoStorageProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlayerPhotoStorageConfig {

    @Bean
    @ConditionalOnProperty(name = "app.player-photo.storage-provider", havingValue = "local", matchIfMissing = true)
    public PlayerPhotoStorageProvider localPlayerPhotoStorageProvider(
            @Value("${app.player-photo.upload-dir:uploads/player-photos}") String uploadDir,
            @Value("${app.player-photo.base-url:http://localhost:9191}") String baseUrl) {
        return new PlayerPhotoLocalStorageProvider(uploadDir, baseUrl);
    }

    @Bean
    @ConditionalOnProperty(name = "app.player-photo.storage-provider", havingValue = "r2")
    public PlayerPhotoStorageProvider r2PlayerPhotoStorageProvider(
            @Value("${app.player-photo.r2.endpoint:}") String endpoint,
            @Value("${app.player-photo.r2.bucket:}") String bucket,
            @Value("${app.player-photo.r2.access-key:}") String accessKey,
            @Value("${app.player-photo.r2.secret-key:}") String secretKey,
            @Value("${app.player-photo.r2.presign-duration-minutes:60}") long presignDurationMinutes) {

        if (endpoint.isBlank() || bucket.isBlank() || accessKey.isBlank() || secretKey.isBlank()) {
            throw new IllegalStateException("R2 storage selected but one or more required R2 settings are missing");
        }

        return new PlayerPhotoR2StorageProvider(endpoint, bucket, accessKey, secretKey, presignDurationMinutes);
    }
}
