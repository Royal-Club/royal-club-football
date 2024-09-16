package com.bjit.royalclub.royalclubfootball.entity.account;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ac_collections")
public class AcCollection extends AuditBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transactionId", length = 30, unique = true, nullable = false)
    private String transactionId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "collection_players",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private Set<Player> players = new HashSet<>();

    @Column(nullable = false)
    private double amount;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Column(name = "month_of_payment", nullable = false)
    private LocalDate monthOfPayment;

    @Column(name = "description")
    private String description;

    @Column(name = "is_paid", nullable = false)
    private boolean isPaid;

    // One-to-one relationship with AcVoucher
    @OneToOne(mappedBy = "collection",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private AcVoucher voucher;
}
