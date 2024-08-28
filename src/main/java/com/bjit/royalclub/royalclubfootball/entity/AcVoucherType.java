package com.bjit.royalclub.royalclubfootball.entity;

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
@Table(name = "ac_voucher_types")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcVoucherType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String alias;

}
