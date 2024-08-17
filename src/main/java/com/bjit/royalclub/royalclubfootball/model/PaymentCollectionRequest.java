package com.bjit.royalclub.royalclubfootball.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentCollectionRequest {
    private Long id;
    private Long playerId;
    private double amount;
    private LocalDate paymentMonth;
    private String description;
    private LocalDateTime collectionDate;
}
