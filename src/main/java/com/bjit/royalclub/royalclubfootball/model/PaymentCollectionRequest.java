package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class PaymentCollectionRequest {

    @NotEmpty(message = "Player ID is required")
    private Set<Long> playerIds;

    @Positive(message = "Amount must be greater than zero")
    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotNull(message = "Month of payment is required")
    private LocalDate monthOfPayment;

    private String description;
}
