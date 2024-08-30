package com.bjit.royalclub.royalclubfootball.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

//@Entity
@Builder
@Table(name = "ac_charts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AcChart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private String code;

//    private Long parentAccNo;
//private Long natureNo;


}
