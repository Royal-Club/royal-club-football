package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.PositionGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamFormationSlotResponse {

    private Long id;
    private Integer slotIndex;
    private PositionGroup positionGroup;
    private String slotLabel;
    private BigDecimal x;
    private BigDecimal y;
    private Boolean isStarter;

    /** Null when the captain has left the slot open. */
    private Long teamPlayerId;
    private Long playerId;
    private String playerName;
    private Integer jerseyNumber;
    private String playingPosition;
    private Boolean isCaptain;
    private String photoKey;
    private String photoUrl;
}
