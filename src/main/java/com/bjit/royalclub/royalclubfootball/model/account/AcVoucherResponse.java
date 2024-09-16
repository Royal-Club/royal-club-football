package com.bjit.royalclub.royalclubfootball.model.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcCollection;
import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucherType;
import com.bjit.royalclub.royalclubfootball.model.AcCollectionResponse;
import com.bjit.royalclub.royalclubfootball.model.PlayerResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AcVoucherResponse {


    private Long id;

    private String code;

    private String narration;

    private LocalDate voucherDate;

    private BigDecimal amount;

    private boolean postFlag;

    private PlayerResponse postedBy;

    private LocalDate postDate;

    private AcVoucherType voucherType;

    private List<AcVoucherDetailResponse> details;

    private AcCollectionResponse collection;

}
