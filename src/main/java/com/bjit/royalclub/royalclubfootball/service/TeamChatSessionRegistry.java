package com.bjit.royalclub.royalclubfootball.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Which live socket sessions are still entitled to which chat rooms.
 *
 * <p>Membership is checked when a client subscribes, but a subscription outlives that check: nothing
 * re-examines it as messages are broadcast down it. A player dropped from a squad after the line-up
 * was published therefore kept receiving everything the squad said, for as long as their tab stayed
 * open - they could no longer post, or load history, or fetch a file, because every one of those
 * goes back through {@link TeamChatAccessService}, but the live feed kept arriving.
 *
 * <p>Re-running the membership query for every message to every subscriber would close that, at the
 * cost of a database round trip per recipient per message on the busiest path in the feature. This
 * holds the answer in memory instead: the subscription is recorded once, when it is authorised, and
 * struck off the moment the player is removed. The outbound check is then a map lookup.
 *
 * <p>Entries are bounded by the number of connected clients and are removed when a session ends,
 * whether it disconnects cleanly or drops.
 */
@Slf4j
@Component
public class TeamChatSessionRegistry {

    /** Keyed by STOMP session id, which for this transport is also the WebSocket session id. */
    private final Map<String, SessionEntry> sessions = new ConcurrentHashMap<>();

    /** One connected client: who it belongs to, and the rooms it is currently listening to. */
    private static final class SessionEntry {
        private final Long playerId;
        /**
         * Team id per subscription id.
         *
         * <p>Keyed by subscription rather than holding a plain set of team ids because UNSUBSCRIBE
         * names only the subscription; without this there would be no way to tell which room a
         * client had just stopped listening to.
         */
        private final Map<String, Long> teamBySubscription = new ConcurrentHashMap<>();

        private SessionEntry(Long playerId) {
            this.playerId = playerId;
        }
    }

    /** Records a subscription that has already passed the membership check. */
    public void subscribed(String sessionId, Long playerId, String subscriptionId, Long teamId) {
        if (sessionId == null || playerId == null || subscriptionId == null || teamId == null) {
            return;
        }
        sessions.computeIfAbsent(sessionId, id -> new SessionEntry(playerId))
                .teamBySubscription.put(subscriptionId, teamId);
    }

    /** Forgets one subscription; the session may still hold others. */
    public void unsubscribed(String sessionId, String subscriptionId) {
        if (sessionId == null || subscriptionId == null) {
            return;
        }
        SessionEntry entry = sessions.get(sessionId);
        if (entry != null) {
            entry.teamBySubscription.remove(subscriptionId);
        }
    }

    /**
     * Whether this session may still be sent messages from this room.
     *
     * <p>The question the outbound interceptor asks of every broadcast frame, so it does no more
     * than read two maps.
     */
    public boolean mayReceive(String sessionId, Long teamId) {
        SessionEntry entry = sessions.get(sessionId);
        return entry != null && entry.teamBySubscription.containsValue(teamId);
    }

    /**
     * Cuts a player off from one room across every session they have open.
     *
     * <p>Called when they are removed from the squad. Their client is not asked to stop listening -
     * it is simply no longer sent anything, which is the difference between enforcing this and
     * hoping the browser co-operates. Their socket stays up and their other rooms, if any, are
     * untouched.
     */
    public void revoke(Long playerId, Long teamId) {
        if (playerId == null || teamId == null) {
            return;
        }
        sessions.values().stream()
                .filter(entry -> Objects.equals(entry.playerId, playerId))
                .forEach(entry -> entry.teamBySubscription.values()
                        .removeIf(subscribed -> Objects.equals(subscribed, teamId)));
        log.info("Revoked live team chat delivery for player {} in team {}.", playerId, teamId);
    }

    /**
     * Drops everything held for a session that has gone away.
     *
     * <p>An event listener rather than a DISCONNECT frame in the inbound interceptor: a browser that
     * is closed, loses its network or is put to sleep never sends one, and those sessions would
     * otherwise accumulate for the lifetime of the process.
     */
    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        sessions.remove(event.getSessionId());
    }
}
