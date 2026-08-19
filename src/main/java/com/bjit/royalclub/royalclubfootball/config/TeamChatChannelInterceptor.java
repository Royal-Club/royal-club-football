package com.bjit.royalclub.royalclubfootball.config;

import com.bjit.royalclub.royalclubfootball.security.CustomUserDetailsService;
import com.bjit.royalclub.royalclubfootball.security.UserPrinciple;
import com.bjit.royalclub.royalclubfootball.service.TeamChatAccessService;
import com.bjit.royalclub.royalclubfootball.service.TeamChatSessionRegistry;
import com.bjit.royalclub.royalclubfootball.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Authenticates the team chat socket and decides who may listen to which room.
 *
 * <p>The REST side of the chat is protected by {@link TeamChatAccessService}, but a STOMP
 * subscription never passes through a controller: without this, anyone who could open the socket
 * could subscribe to {@code /topic/team-chat/{anyTeamId}} and read the other side's room live while
 * being refused its history over HTTP. Broadcasting is exactly as sensitive as reading, so the same
 * membership rule is applied here.
 *
 * <p>Two commands are intercepted:
 * <ul>
 *   <li><b>CONNECT</b> carries the bearer token as a STOMP header - a browser cannot set
 *       {@code Authorization} on a WebSocket handshake, so the credential travels in the first STOMP
 *       frame instead. A connection without a good one is refused outright.</li>
 *   <li><b>SUBSCRIBE</b> is where membership is checked, per destination. It cannot be done once at
 *       connect time: one connection may subscribe to any number of destinations.</li>
 *   <li><b>UNSUBSCRIBE</b> carries no destination, only the subscription id, which is why the
 *       registry keys rooms by subscription rather than holding a bare set of team ids.</li>
 * </ul>
 *
 * <p>Passing here is not the end of it. A subscription lasts as long as the tab does, so an
 * authorised one is recorded in {@link TeamChatSessionRegistry} and re-checked on the way out by
 * {@link TeamChatOutboundInterceptor} - otherwise a player removed from the squad would go on
 * receiving the room live, having been locked out of every other way into it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TeamChatChannelInterceptor implements ChannelInterceptor {

    private static final String TEAM_CHAT_DESTINATION_PREFIX = "/topic/team-chat/";

    private final JWTUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final TeamChatAccessService accessService;
    private final TeamChatSessionRegistry sessionRegistry;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            authorizeSubscription(accessor);
        } else if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
            sessionRegistry.unsubscribed(accessor.getSessionId(), accessor.getSubscriptionId());
        }
        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String token = bearerToken(accessor);
        if (token == null) {
            throw new IllegalArgumentException("Sign in to join the team chat");
        }
        String email = jwtUtil.emailIfValid(token);
        if (email == null) {
            throw new IllegalArgumentException("Your session has expired. Please sign in again.");
        }

        UserPrinciple principal = (UserPrinciple) userDetailsService.loadUserByUsername(email);
        // Stored on the STOMP session rather than in the SecurityContext: the context is bound to a
        // thread, and later frames on this connection arrive on whichever thread the broker picks.
        accessor.setUser(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(TEAM_CHAT_DESTINATION_PREFIX)) {
            // Not a chat destination - the auction topics keep whatever rules they already had.
            return;
        }

        Long teamId = teamIdFrom(destination);
        Long playerId = playerIdOf(accessor);
        if (teamId == null || playerId == null || !accessService.canSubscribe(teamId, playerId)) {
            log.warn("Refused team chat subscription to {} for player {}.", destination, playerId);
            throw new IllegalArgumentException("This chat is only open to the players in this team");
        }

        // Recorded only once the check above has passed, and before the broker can fan anything out
        // to this subscription, so the outbound interceptor never sees an authorised listener it
        // does not know about.
        sessionRegistry.subscribed(
                accessor.getSessionId(), playerId, accessor.getSubscriptionId(), teamId);
    }

    private Long teamIdFrom(String destination) {
        try {
            return Long.parseLong(destination.substring(TEAM_CHAT_DESTINATION_PREFIX.length()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long playerIdOf(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken authentication
                && authentication.getPrincipal() instanceof UserPrinciple principal) {
            return principal.getId();
        }
        return null;
    }

    private String bearerToken(StompHeaderAccessor accessor) {
        List<String> values = accessor.getNativeHeader("Authorization");
        if (values == null || values.isEmpty()) {
            return null;
        }
        String header = values.get(0);
        return header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
    }
}
