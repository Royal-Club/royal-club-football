package com.bjit.royalclub.royalclubfootball.entity.account;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//@Entity
@Builder
@Table(name = "ac_voucher_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcVoucherDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // todo AcVoucher map
    private String narration;
    private Double dr;
    private Double cr;

    private String refNo;
}
