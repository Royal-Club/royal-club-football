package com.bjit.royalclub.royalclubfootball.controller.auction;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.model.auction.BidRequest;
import com.bjit.royalclub.royalclubfootball.model.auction.BidResponse;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.service.auction.AuctionSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuctionWebSocketController {

    private final AuctionSessionService sessionService;
    private final PlayerRepository playerRepository;

    @MessageMapping("/auction/{tournamentId}/bid")
    public void handleBid(
            @DestinationVariable Long tournamentId,
            @Payload BidRequest bidRequest,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            Principal principal = headerAccessor.getUser();
            if (principal == null) {
                log.warn("Unauthenticated WebSocket bid attempt");
                return;
            }

            Player player = playerRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            BidResponse response = sessionService.placeBid(tournamentId, bidRequest, player.getId());
            log.info("Bid placed via WebSocket: tournament={}, team={}, amount={}",
                    tournamentId, bidRequest.getTeamId(), bidRequest.getBidAmount());
        } catch (Exception e) {
            log.error("WebSocket bid error: {}", e.getMessage());
        }
    }
}
