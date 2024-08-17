package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentCollectionRequest {
    @NotNull(message = "Player ID is required")
    private Long playerId;

    @Positive(message = "Amount must be greater than zero")
    private double amount;

    @NotNull(message = "Month of payment is required")
    private LocalDate monthOfPayment;

    private String description;
}
