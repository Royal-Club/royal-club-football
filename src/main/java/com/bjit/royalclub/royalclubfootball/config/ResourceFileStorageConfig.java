package com.bjit.royalclub.royalclubfootball.config;

import com.bjit.royalclub.royalclubfootball.storage.resourcefile.ResourceFileLocalStorageProvider;
import com.bjit.royalclub.royalclubfootball.storage.resourcefile.ResourceFileR2StorageProvider;
import com.bjit.royalclub.royalclubfootball.storage.resourcefile.ResourceFileStorageProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResourceFileStorageConfig {

    @Bean
    @ConditionalOnProperty(name = "app.resource-file.storage-provider", havingValue = "local", matchIfMissing = true)
    public ResourceFileStorageProvider localResourceFileStorageProvider(
            @Value("${app.resource-file.upload-dir:uploads/resources}") String uploadDir,
            @Value("${app.resource-file.base-url:http://localhost:9191}") String baseUrl) {
        return new ResourceFileLocalStorageProvider(uploadDir, baseUrl);
    }

    @Bean
    @ConditionalOnProperty(name = "app.resource-file.storage-provider", havingValue = "r2")
    public ResourceFileStorageProvider r2ResourceFileStorageProvider(
            @Value("${app.resource-file.r2.endpoint:}") String endpoint,
            @Value("${app.resource-file.r2.bucket:}") String bucket,
            @Value("${app.resource-file.r2.access-key:}") String accessKey,
            @Value("${app.resource-file.r2.secret-key:}") String secretKey,
            @Value("${app.resource-file.r2.presign-duration-minutes:60}") long presignDurationMinutes) {

        if (endpoint.isBlank() || bucket.isBlank() || accessKey.isBlank() || secretKey.isBlank()) {
            throw new IllegalStateException("R2 storage selected but one or more required R2 settings are missing");
        }

        return new ResourceFileR2StorageProvider(endpoint, bucket, accessKey, secretKey, presignDurationMinutes);
    }
}
