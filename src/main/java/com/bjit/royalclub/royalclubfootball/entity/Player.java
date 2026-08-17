package com.bjit.royalclub.royalclubfootball.entity;

import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import com.bjit.royalclub.royalclubfootball.enums.FootballPosition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "players", uniqueConstraints = {@UniqueConstraint(columnNames = {"email", "employee_id"})})
public class Player extends AuditBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "employee_id", nullable = false, unique = true)
    private String employeeId;
    @Column(name = "skype_id", nullable = false, unique = true)
    private String skypeId;

    @Column(name = "mobile_no")
    private String mobileNo;
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    @Enumerated(EnumType.STRING)
    @Column(name = "playing_position", nullable = false)
    private FootballPosition position;

    /**
     * Whether this player takes part in the goalkeeper rotation. Kept apart from {@link #position}
     * on purpose - position is the outfield role someone is listed under, not a statement about
     * whether they are willing or able to go in goal.
     */
    @Column(name = "gk_eligible", nullable = false)
    @Builder.Default
    private boolean gkEligible = true;

    private String password;

    @Column(name = "last_password_change_date")
    private LocalDateTime lastPasswordChangeDate;

    @Column(name = "profile_photo", length = 500)
    private String profilePhoto;

    @Column(name = "photo_key")
    private String photoKey;

    /**
     * When this player last <em>replaced</em> a photo. Null means they never have - either no photo
     * at all, or still on their first - and a first upload is deliberately free, so null always
     * passes the rolling-window check.
     *
     * @see com.bjit.royalclub.royalclubfootball.service.PlayerPhotoQuotaService
     */
    @Column(name = "photo_updated_at")
    private LocalDateTime photoUpdatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "players_roles", joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;
}
