package com.bjit.royalclub.royalclubfootball.entity;

import com.bjit.royalclub.royalclubfootball.enums.AcNatureType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ac_natures")
@Getter
@Setter
public class AcNature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 20)
    private String name;

    @Column(nullable = false, updatable = false, length = 2)
    private Integer code;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false, updatable = false)
    private AcNatureType type;

    private Integer slNo;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

}
