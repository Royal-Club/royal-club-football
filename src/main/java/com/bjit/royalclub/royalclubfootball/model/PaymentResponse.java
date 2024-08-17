package com.bjit.royalclub.royalclubfootball.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PaymentResponse {
    private Long playerId;
    private String playerName;
    private LocalDate paymentMonth;
    private double amount;
}
