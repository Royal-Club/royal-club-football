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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Set<Long> sendToPlayers(List<Player> players, String title, String body, Map<String, String> data) {
        if (players == null || players.isEmpty()) {
            return Set.of();
        }
        List<Long> playerIds = players.stream().map(Player::getId).toList();
        List<PlayerDeviceToken> devices = deviceTokenRepository.findAllByPlayerIdIn(playerIds);

        if (devices.isEmpty()) {
            // Nobody in this batch has the app installed and registered. Saying so plainly matters:
            // the caller would otherwise record a delivery for every one of them and charge each a
            // reminder they were never sent.
            log.warn("Push '{}' not sent: none of the {} player(s) have a registered device.",
                    title, players.size());
            return Set.of();
        }

        // Kept so per-token results from FCM can be attributed back to the player who owns them.
        Map<String, Long> ownerByToken = devices.stream()
                .collect(Collectors.toMap(PlayerDeviceToken::getToken,
                        device -> device.getPlayer().getId(),
                        (first, second) -> first));

        Set<String> acceptedTokens = sendAndReportAccepted(List.copyOf(ownerByToken.keySet()), title, body, data);

        Set<Long> reached = acceptedTokens.stream().map(ownerByToken::get).collect(Collectors.toSet());
        int missed = players.size() - reached.size();
        if (missed > 0) {
            log.warn("Push '{}': {} of {} player(s) were not reached on any device.",
                    title, missed, players.size());
        }
        return reached;
    }

    @Override
    public void sendToTokens(List<String> tokens, String title, String body, Map<String, String> data) {
        sendAndReportAccepted(tokens, title, body, data);
    }

    /**
     * Sends to every token and reports back the ones FCM accepted.
     *
     * @return the accepted tokens - empty when Firebase is unconfigured, the batch call failed, or
     * every token was rejected. Callers must be able to tell "delivered" from "attempted".
     */
    private Set<String> sendAndReportAccepted(List<String> tokens, String title, String body,
                                              Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) {
            return Set.of();
        }

        FirebaseMessaging messaging = firebaseMessagingProvider.getIfAvailable();
        if (messaging == null) {
            log.warn("Firebase not configured; skipping push of '{}' to {} device(s).", title, tokens.size());
            return Set.of();
        }

        Map<String, String> payload = data == null ? Map.of() : data;
        Set<String> accepted = new HashSet<>();

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

                List<SendResponse> results = response.getResponses();
                for (int i = 0; i < results.size() && i < batch.size(); i++) {
                    if (results.get(i).isSuccessful()) {
                        accepted.add(batch.get(i));
                    }
                }
                log.info("Push '{}': {} sent, {} failed.", title, response.getSuccessCount(),
                        response.getFailureCount());
            } catch (FirebaseMessagingException e) {
                // The whole batch is unaccounted for; none of it may be treated as delivered.
                log.error("Failed to send push '{}' to a batch of {} device(s).", title, batch.size(), e);
            }
        }
        return accepted;
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
