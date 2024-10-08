package com.bjit.royalclub.royalclubfootball.entity.account;

import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import com.bjit.royalclub.royalclubfootball.enums.AcTransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ac_voucher_types")
@Getter
@Setter
public class AcVoucherType extends AuditBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String name;

    @Column(length = 2, nullable = false, updatable = false)
    private String alias;

    @Enumerated(EnumType.STRING)
    @Column(name = "ac_transaction_type", nullable = false, updatable = false)
    private AcTransactionType acTransactionType;

    private String description;
    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

}
