package com.bjit.royalclub.royalclubfootball.model.account;

import com.bjit.royalclub.royalclubfootball.model.PlayerResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class AcCollectionResponse {
    private Long id;
    private String transactionId;
    private LocalDate monthOfPayment;
    private BigDecimal amount;
    private BigDecimal totalAmount;
    private boolean isPaid;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Set<PlayerResponse> players;
    private String allPayersName;
    private Long voucherId;
    private String voucherCode;

}
