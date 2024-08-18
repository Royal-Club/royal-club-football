package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MonthlyCostRequest {
    @NotNull(message = "Cost Type ID is required")
    private Long costTypeId;

    @Positive(message = "Amount must be greater than zero")
    private double amount;

    @NotNull(message = "Month of Cost is required")
    private LocalDate monthOfCost;
    private String description;
}
