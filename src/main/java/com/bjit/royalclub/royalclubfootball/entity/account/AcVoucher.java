package com.bjit.royalclub.royalclubfootball.entity.account;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "ac_vouchers")
@Getter
@Setter
public class AcVoucher extends AuditBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20, nullable = false)
    private String code;

    @Column(length = 500)
    private String narration;

    @Column(name = "voucher_date", nullable = false)
    private LocalDate voucherDate;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "post_flag", nullable = false)
    private boolean postFlag;

    @ManyToOne
    @JoinColumn(name = "posted_by")
    private Player postedBy;

    @Column(name = "post_date")
    private LocalDate postDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "voucher_type_id", nullable = false)
    private AcVoucherType voucherType;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AcVoucherDetail> details;

    @OneToOne
    @JoinColumn(name = "collection_id", unique = true)
    private AcCollection collection;

    @OneToOne
    @JoinColumn(name = "payment_id", unique = true)
    private AcBillPayment billPayment;

}
