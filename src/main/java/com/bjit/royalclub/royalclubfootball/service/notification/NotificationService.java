package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Channel-agnostic notification abstraction. The current implementation sends push via FCM;
 * email / WhatsApp adapters can implement this same contract later without touching callers.
 */
public interface NotificationService {

    /**
     * Send a notification to every registered device of the given players.
     *
     * @param data optional key/value payload delivered with the message (used by the app to deep-link).
     */
    void sendToPlayers(List<Player> players, String title, String body, Map<String, String> data);

    /**
     * Send a notification directly to a set of device tokens.
     */
    void sendToTokens(List<String> tokens, String title, String body, Map<String, String> data);
}
