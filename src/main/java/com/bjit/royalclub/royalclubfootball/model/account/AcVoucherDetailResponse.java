package com.bjit.royalclub.royalclubfootball.model.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AcVoucherDetailResponse {

    private Long id;

    private String narration;

    private BigDecimal dr;

    private BigDecimal cr;

    private AcChartResponse acChart;

    private AcVoucherResponse voucher;

    private String referenceNo;
}
