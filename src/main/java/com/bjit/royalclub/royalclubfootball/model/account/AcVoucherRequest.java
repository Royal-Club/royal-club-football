package com.bjit.royalclub.royalclubfootball.model.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcCollection;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AcVoucherRequest {

    @Size(max = 500)
    private String narration;

    @NotNull(message = "Voucher date is required")
    private LocalDate voucherDate;

    private boolean postFlag;

    @NotNull(message = "Voucher type ID is required")
    private Long voucherTypeId;

    @NotEmpty(message = "Voucher details cannot be empty")
    private List<AcVoucherDetailRequest> details;

    private AcCollection collection;

}
