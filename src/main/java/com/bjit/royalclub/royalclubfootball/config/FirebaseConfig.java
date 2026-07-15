package com.bjit.royalclub.royalclubfootball.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Initializes Firebase Cloud Messaging when credentials are configured.
 * <p>
 * Credentials may be supplied either as a file path ({@code firebase.credentials-path}) or as the raw
 * service-account JSON ({@code firebase.credentials-json}, convenient for env-var based deployments).
 * If neither is present the app still boots normally and {@link FirebaseMessaging} is left unavailable,
 * making push a no-op (see {@code FcmNotificationService}).
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials-path:}")
    private String credentialsPath;

    @Value("${firebase.credentials-json:}")
    private String credentialsJson;

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        try {
            GoogleCredentials credentials = loadCredentials();
            if (credentials == null) {
                log.warn("Firebase credentials not configured (firebase.credentials-path / "
                        + "firebase.credentials-json). Push notifications are DISABLED (no-op).");
                return null;
            }

            FirebaseApp app;
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();
                app = FirebaseApp.initializeApp(options);
                log.info("Firebase initialized; push notifications are ENABLED.");
            } else {
                app = FirebaseApp.getInstance();
            }
            return FirebaseMessaging.getInstance(app);
        } catch (IOException e) {
            log.error("Failed to initialize Firebase; push notifications DISABLED.", e);
            return null;
        }
    }

    private GoogleCredentials loadCredentials() throws IOException {
        if (StringUtils.hasText(credentialsJson)) {
            try (InputStream is = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))) {
                return GoogleCredentials.fromStream(is);
            }
        }
        if (StringUtils.hasText(credentialsPath)) {
            try (InputStream is = new FileInputStream(credentialsPath)) {
                return GoogleCredentials.fromStream(is);
            }
        }
        return null;
    }
}
