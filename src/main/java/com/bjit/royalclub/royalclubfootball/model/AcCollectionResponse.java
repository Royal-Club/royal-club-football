package com.bjit.royalclub.royalclubfootball.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class AcCollectionResponse {
    private Long id;
    private String transactionId;
    private LocalDate monthOfPayment;
    private double amount;
    private double totalAmount;
    private boolean isPaid;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Set<PlayerResponse> players;
    private String allPayersName;
    private Long voucherId;
    private String voucherCode;

}
