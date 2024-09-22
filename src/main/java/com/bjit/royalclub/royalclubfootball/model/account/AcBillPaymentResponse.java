package com.bjit.royalclub.royalclubfootball.model.account;

import com.bjit.royalclub.royalclubfootball.model.CostTypeResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AcBillPaymentResponse {

    private Long id;

    private String code;

    private BigDecimal amount;

    private LocalDate paymentDate;

    private String description;

    private CostTypeResponse costType;

    private Long voucherId;

    private String voucherCode;

    private boolean isPaid;

}
