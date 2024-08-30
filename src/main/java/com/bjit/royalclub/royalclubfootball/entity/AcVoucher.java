package com.bjit.royalclub.royalclubfootball.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

//@Entity
@Builder
@Table(name = "ac_vouchers")
@Getter
@Setter
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
