package com.bjit.royalclub.royalclubfootball.entity;

import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * One message in a team's private room.
 *
 * <p>The room itself is not an entity - it is the {@link Team}, which already scopes to one
 * tournament. That is what makes each tournament's chat start empty without anything having to
 * reset it: new tournament, new teams, new rows.
 *
 * <p>Rows are destroyed when the tournament concludes, so nothing here is designed to survive:
 * there is no status, no soft delete, and no archive.
 */
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "team_chat_message")
public class TeamChatMessage extends AuditBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /**
     * Held as a relation rather than a copied name, so a player who changes their display name is
     * shown correctly on messages they already sent.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_player_id", nullable = false)
    private Player sender;

    /** Null when the message is a file with nothing typed alongside it. */
    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    @lombok.Builder.Default
    private List<TeamChatAttachment> attachments = new ArrayList<>();
}
