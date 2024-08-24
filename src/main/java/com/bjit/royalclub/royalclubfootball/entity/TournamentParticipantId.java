package com.bjit.royalclub.royalclubfootball.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class TournamentParticipantId implements Serializable {

    private Long tournamentId;
    private Long playerId;

}
