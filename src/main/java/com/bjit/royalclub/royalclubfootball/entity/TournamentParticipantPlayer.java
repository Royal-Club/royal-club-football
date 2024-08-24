package com.bjit.royalclub.royalclubfootball.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Immutable
@IdClass(TournamentParticipantId.class)
@Table(name = "tournament_participant_players")
public class TournamentParticipantPlayer {

    @Id
    @Column(name = "tournament_id")
    private Long tournamentId;

    @Column(name = "tournament_name")
    private String tournamentName;

    @Column(name = "tournament_date")
    private LocalDateTime tournamentDate;
    @Id
    @Column(name = "player_id")
    private Long playerId;

    @Column(name = "player_name")
    private String playerName;

    @Column(name = "player_employee_id")
    private String playerEmployeeId;

    @Column(name = "participation_status")
    private Boolean participationStatus;
}
