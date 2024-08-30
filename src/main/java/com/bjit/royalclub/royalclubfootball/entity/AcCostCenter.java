package com.bjit.royalclub.royalclubfootball.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

//@Entity
@Builder
@Table(name = "ac_cost_centers")
@Getter
@Setter
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
