package com.bjit.royalclub.royalclubfootball.entity.account;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Entity
@Builder
@Table(name = "ac_cost_centers")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AcCostCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String costName;
    private String description;

//    private Long accNo;


}
