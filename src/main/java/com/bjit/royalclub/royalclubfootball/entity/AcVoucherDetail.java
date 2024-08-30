package com.bjit.royalclub.royalclubfootball.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

//@Entity
@Builder
@Table(name = "ac_voucher_details")
@Getter
@Setter
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
