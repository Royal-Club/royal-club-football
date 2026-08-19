package com.bjit.royalclub.royalclubfootball.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers who is still entitled to receive a room live.
 *
 * <p>This is the only thing standing between a player removed from a squad and the rest of that
 * squad's conversation: every other route into the room re-checks the database, but a subscription
 * is authorised once and then lasts as long as the browser tab. If this regresses nothing breaks
 * visibly - the chat keeps working for everyone, including the person who should no longer be in it.
 */
class TeamChatSessionRegistryTest {

    private static final String SESSION = "session-1";
    private static final String OTHER_SESSION = "session-2";
    private static final String SUBSCRIPTION = "sub-0";
    private static final Long PLAYER = 7L;
    private static final Long OTHER_PLAYER = 8L;
    private static final Long TEAM = 42L;
    private static final Long OTHER_TEAM = 43L;

    private TeamChatSessionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new TeamChatSessionRegistry();
    }

    @Test
    void deliversToAnAuthorisedSubscription() {
        registry.subscribed(SESSION, PLAYER, SUBSCRIPTION, TEAM);

        assertThat(registry.mayReceive(SESSION, TEAM)).isTrue();
    }

    @Test
    void deliversNothingToASessionThatNeverSubscribed() {
        assertThat(registry.mayReceive(SESSION, TEAM)).isFalse();
    }

    @Test
    void deliversNothingForARoomTheSessionDidNotSubscribeTo() {
        registry.subscribed(SESSION, PLAYER, SUBSCRIPTION, TEAM);

        assertThat(registry.mayReceive(SESSION, OTHER_TEAM)).isFalse();
    }

    /** The point of the whole class: removal has to reach a socket that is already open. */
    @Test
    void stopsDeliveringOnceThePlayerIsRevoked() {
        registry.subscribed(SESSION, PLAYER, SUBSCRIPTION, TEAM);

        registry.revoke(PLAYER, TEAM);

        assertThat(registry.mayReceive(SESSION, TEAM)).isFalse();
    }

    @Test
    void revokesEverySessionThatPlayerHasOpen() {
        registry.subscribed(SESSION, PLAYER, SUBSCRIPTION, TEAM);
        registry.subscribed(OTHER_SESSION, PLAYER, "sub-1", TEAM);

        registry.revoke(PLAYER, TEAM);

        assertThat(registry.mayReceive(SESSION, TEAM)).isFalse();
        assertThat(registry.mayReceive(OTHER_SESSION, TEAM)).isFalse();
    }

    @Test
    void leavesTheRestOfTheSquadAlone() {
        registry.subscribed(SESSION, PLAYER, SUBSCRIPTION, TEAM);
        registry.subscribed(OTHER_SESSION, OTHER_PLAYER, SUBSCRIPTION, TEAM);

        registry.revoke(PLAYER, TEAM);

        assertThat(registry.mayReceive(OTHER_SESSION, TEAM)).isTrue();
    }

    /** Being dropped from one squad says nothing about another tournament running alongside it. */
    @Test
    void leavesThePlayerOtherRoomsAlone() {
        registry.subscribed(SESSION, PLAYER, SUBSCRIPTION, TEAM);
        registry.subscribed(SESSION, PLAYER, "sub-1", OTHER_TEAM);

        registry.revoke(PLAYER, TEAM);

        assertThat(registry.mayReceive(SESSION, TEAM)).isFalse();
        assertThat(registry.mayReceive(SESSION, OTHER_TEAM)).isTrue();
    }

    @Test
    void forgetsOnlyTheSubscriptionThatWasCancelled() {
        registry.subscribed(SESSION, PLAYER, SUBSCRIPTION, TEAM);
        registry.subscribed(SESSION, PLAYER, "sub-1", OTHER_TEAM);

        registry.unsubscribed(SESSION, SUBSCRIPTION);

        assertThat(registry.mayReceive(SESSION, TEAM)).isFalse();
        assertThat(registry.mayReceive(SESSION, OTHER_TEAM)).isTrue();
    }

    /** A closed tab must not leave an entry behind for the lifetime of the process. */
    @Test
    void forgetsASessionThatDisconnects() {
        registry.subscribed(SESSION, PLAYER, SUBSCRIPTION, TEAM);

        registry.onSessionDisconnect(disconnectEvent(SESSION));

        assertThat(registry.mayReceive(SESSION, TEAM)).isFalse();
    }

    private SessionDisconnectEvent disconnectEvent(String sessionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(
                org.springframework.messaging.simp.stomp.StompCommand.DISCONNECT);
        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);
        return new SessionDisconnectEvent(
                this,
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders()),
                sessionId,
                org.springframework.web.socket.CloseStatus.NORMAL);
    }
}
