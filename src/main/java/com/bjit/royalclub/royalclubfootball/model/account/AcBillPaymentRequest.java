package com.bjit.royalclub.royalclubfootball.model.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AcBillPaymentRequest{

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private LocalDate paymentDate;

    @Size(max = 500)
    private String description;

    @NotNull
    private Long costTypeId;

    @NotNull
    private boolean isPaid;

}
