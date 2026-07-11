package com.bjit.royalclub.royalclubfootball.model.auction;

import com.bjit.royalclub.royalclubfootball.enums.AuctionSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionSessionResponse {
    private Long id;
    private Long tournamentId;
    private AuctionSessionStatus status;
    private AuctionPlayerResponse currentPlayer;
    private Integer roundNumber;
    private LocalDateTime startedAt;
    private LocalDateTime currentTimerEndsAt;
    private Long remainingSeconds;
}
