package com.bjit.royalclub.royalclubfootball.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MonthlyCostRequest {
    private Long costTypeId;
    private double amount;
    private LocalDate monthOfCost;
    private String description;
}
