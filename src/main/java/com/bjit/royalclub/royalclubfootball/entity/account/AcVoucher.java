package com.bjit.royalclub.royalclubfootball.entity.account;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

//@Entity
@Builder
@Table(name = "ac_vouchers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String code;

    private String narration;

    private LocalDate date;


    private Double amount;

    private boolean postFlag;

//    private Integer vtypeNo;

//    private Long paidTo;
//    private Long receiveFrom;


}
