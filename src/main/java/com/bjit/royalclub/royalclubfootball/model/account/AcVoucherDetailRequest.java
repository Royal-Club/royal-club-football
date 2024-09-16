package com.bjit.royalclub.royalclubfootball.model.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AcVoucherDetailRequest {

    @PositiveOrZero(message = "Credit amount must be zero or positive")
    private BigDecimal cr;

    @PositiveOrZero(message = "Debit amount must be zero or positive")
    private BigDecimal dr;

    private String narration;

    @NotNull(message = "Account chart ID is required")
    private Long acChartId;

    @NotNull(message = "Voucher ID is required")
    private Long voucherId;

    @Size(max = 100, message = "Reference number cannot exceed 100 characters")
    private String referenceNo;

}
