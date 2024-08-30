package com.bjit.royalclub.royalclubfootball.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TournamentParticipantId implements Serializable {

    private Long tournamentId;
    private Long playerId;

}
