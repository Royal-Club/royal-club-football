package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.PlayerDeviceToken;
import com.bjit.royalclub.royalclubfootball.repository.PlayerDeviceTokenRepository;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Firebase Cloud Messaging implementation of {@link NotificationService}.
 * If Firebase is not configured, {@link FirebaseMessaging} is unavailable and every send is a logged no-op,
 * so the rest of the reminder pipeline keeps working (rows are still recorded by the caller).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FcmNotificationService implements NotificationService {

    /** FCM caps a multicast send at 500 tokens per request. */
    private static final int FCM_MULTICAST_LIMIT = 500;

    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;
    private final PlayerDeviceTokenRepository deviceTokenRepository;

    @Override
    public void sendToPlayers(List<Player> players, String title, String body, Map<String, String> data) {
        if (players == null || players.isEmpty()) {
            return;
        }
        List<Long> playerIds = players.stream().map(Player::getId).toList();
        List<String> tokens = deviceTokenRepository.findAllByPlayerIdIn(playerIds).stream()
                .map(PlayerDeviceToken::getToken)
                .toList();
        sendToTokens(tokens, title, body, data);
    }

    @Override
    public void sendToTokens(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        FirebaseMessaging messaging = firebaseMessagingProvider.getIfAvailable();
        if (messaging == null) {
            log.warn("Firebase not configured; skipping push of '{}' to {} device(s).", title, tokens.size());
            return;
        }

        Map<String, String> payload = data == null ? Map.of() : data;

        for (int start = 0; start < tokens.size(); start += FCM_MULTICAST_LIMIT) {
            List<String> batch = tokens.subList(start, Math.min(start + FCM_MULTICAST_LIMIT, tokens.size()));
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .putAllData(payload)
                    .addAllTokens(batch)
                    .build();
            try {
                BatchResponse response = messaging.sendEachForMulticast(message);
                cleanupStaleTokens(batch, response);
                log.info("Push '{}': {} sent, {} failed.", title, response.getSuccessCount(),
                        response.getFailureCount());
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send push '{}' to a batch of {} device(s).", title, batch.size(), e);
            }
        }
    }

    /**
     * Delete tokens that FCM reports as permanently invalid so we stop wasting sends on dead devices.
     */
    private void cleanupStaleTokens(List<String> batch, BatchResponse response) {
        List<SendResponse> responses = response.getResponses();
        List<String> staleTokens = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (sendResponse.isSuccessful() || sendResponse.getException() == null) {
                continue;
            }
            MessagingErrorCode errorCode = sendResponse.getException().getMessagingErrorCode();
            if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                staleTokens.add(batch.get(i));
            }
        }
        staleTokens.forEach(token -> {
            deviceTokenRepository.deleteByToken(token);
            log.info("Removed stale device token.");
        });
    }
}
