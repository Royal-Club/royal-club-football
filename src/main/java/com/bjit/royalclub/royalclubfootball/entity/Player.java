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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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

    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "players_roles", joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;
}
