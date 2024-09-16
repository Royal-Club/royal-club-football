package com.bjit.royalclub.royalclubfootball.entity.account;

import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "ac_voucher_details")
@Getter
@Setter
public class AcVoucherDetail extends AuditBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String narration;

    private BigDecimal dr;

    private BigDecimal cr;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ac_chart_id")
    private AcChart acChart;

    @ManyToOne(optional = false)
    @JoinColumn(name = "voucher_id")
    private AcVoucher voucher;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;
}
