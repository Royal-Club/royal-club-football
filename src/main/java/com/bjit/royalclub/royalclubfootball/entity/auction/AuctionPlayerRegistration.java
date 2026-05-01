package com.bjit.royalclub.royalclubfootball.entity.auction;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import com.bjit.royalclub.royalclubfootball.enums.ApprovalStatus;
import com.bjit.royalclub.royalclubfootball.enums.AvailabilityStatus;
import com.bjit.royalclub.royalclubfootball.enums.FootballPosition;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "auction_player_registrations")
public class AuctionPlayerRegistration extends AuditBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "skype_id")
    private String skypeId;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "playing_position", nullable = false)
    private FootballPosition playingPosition;

    @Column(name = "batting_style")
    private String battingStyle;

    @Column(name = "bowling_style")
    private String bowlingStyle;

    @Column(name = "previous_experience", columnDefinition = "TEXT")
    private String previousExperience;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false)
    private AvailabilityStatus availabilityStatus;

    @Column(name = "profile_photo")
    private String profilePhoto;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "preferred_base_price")
    private Integer preferredBasePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Player approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "created_player_id")
    private Player createdPlayer;
}
