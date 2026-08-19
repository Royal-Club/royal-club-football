package com.bjit.royalclub.royalclubfootball.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final TeamChatChannelInterceptor teamChatChannelInterceptor;
    private final TeamChatOutboundInterceptor teamChatOutboundInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/auction")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // A separate endpoint from the auction's, so the two have separate connections. Team chat
        // requires a signed-in member on CONNECT; the auction board does not, and folding them
        // together would mean either loosening this or breaking that.
        registry.addEndpoint("/ws/team-chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * Team chat authentication and per-room subscription checks.
     *
     * <p>Registered on the inbound channel because that is the only place a SUBSCRIBE frame can be
     * refused. The interceptor ignores every destination outside {@code /topic/team-chat/}, so the
     * auction topics are unaffected.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(teamChatChannelInterceptor);
    }

    /**
     * Re-checks each broadcast against the room it is bound for, on its way to one client.
     *
     * <p>The inbound check above runs when a client subscribes and never again, but a subscription
     * lives as long as the browser tab. Without this, removing a player from a squad would take away
     * their history, their ability to post and any future subscription, while the live feed carried
     * on into a session that had already lost the right to it.
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(teamChatOutboundInterceptor);
    }
}
