package com.bjit.royalclub.royalclubfootball.entity;

import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import com.bjit.royalclub.royalclubfootball.enums.ParticipationSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tournament_participant", uniqueConstraints = {@UniqueConstraint(columnNames = {"tournament_id", "player_id"})})
public class TournamentParticipant extends AuditBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "participation_status", nullable = false)
    private boolean participationStatus;
    private String comments;

    /**
     * Where the answer came from. Null on rows written before this was tracked.
     * <p>
     * Chiefly this keeps an AUTO_LOCK No apart from a real one, so locking does not erase the
     * club's record of who never replied and unlocking knows which rows it created.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "participation_source", length = 20)
    private ParticipationSource participationSource;

}
