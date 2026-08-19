package com.bjit.royalclub.royalclubfootball.config;

import com.bjit.royalclub.royalclubfootball.service.TeamChatSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * The last check before a chat message reaches a listening client.
 *
 * <p>{@link TeamChatChannelInterceptor} decides who may subscribe, but that decision is made once
 * and a subscription lasts as long as the tab stays open. Removing a player from a squad closed
 * every other door - history, posting, files, and any future subscription - while leaving the live
 * feed running into a browser that was no longer entitled to it. This is the door on that feed.
 *
 * <p>Registered on the outbound channel, where the broker has already fanned a broadcast out into
 * one message per subscriber, each stamped with the session it is bound for. Returning null aborts
 * that one delivery and leaves every other recipient untouched.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TeamChatOutboundInterceptor implements ChannelInterceptor {

    private static final String TEAM_CHAT_DESTINATION_PREFIX = "/topic/team-chat/";

    private final TeamChatSessionRegistry sessionRegistry;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        SimpMessageHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, SimpMessageHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(TEAM_CHAT_DESTINATION_PREFIX)) {
            // Everything else on this channel - CONNECTED, receipts, errors, the auction topics -
            // is none of this interceptor's business.
            return message;
        }

        Long teamId = teamIdFrom(destination);
        String sessionId = accessor.getSessionId();
        if (teamId == null || sessionId == null) {
            // Withheld rather than passed on. A frame addressed to a chat room that cannot be tied
            // to a room or a recipient is not something to forward on the assumption it is
            // harmless; if this ever fires it is a bug, and a silent chat is a far better way to
            // find out about it than a leak nobody sees.
            log.warn("Dropping an unattributable team chat frame for destination {}.", destination);
            return null;
        }

        if (!sessionRegistry.mayReceive(sessionId, teamId)) {
            log.debug("Withheld a team chat message from session {} for team {}.", sessionId, teamId);
            return null;
        }
        return message;
    }

    private Long teamIdFrom(String destination) {
        try {
            return Long.parseLong(destination.substring(TEAM_CHAT_DESTINATION_PREFIX.length()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
