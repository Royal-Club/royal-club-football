package com.bjit.royalclub.royalclubfootball.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ac_charts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AcChart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 250, nullable = false, unique = true)
    private String name;

    @Column(length = 50, nullable = false, unique = true)
    private String code;

    @Column(length = 250, nullable = false, unique = true)
    private String description;

    @ManyToOne
    @JoinColumn(name = "parent_no")
    private AcChart acChart;

    @ManyToOne
    @JoinColumn(name = "nature_no")
    private AcNature acNature;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = false;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

}
