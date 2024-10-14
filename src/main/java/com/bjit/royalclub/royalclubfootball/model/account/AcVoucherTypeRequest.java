package com.bjit.royalclub.royalclubfootball.model.account;

import com.bjit.royalclub.royalclubfootball.enums.AcTransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AcVoucherTypeRequest {

    @NotBlank(message = "Name is required.")
    private String name;

    @NotBlank(message = "Alias is required.")
    private String alias;

    private String description;

    @NotNull(message = "Transaction type is required.")
    private AcTransactionType acTransactionType;

    private boolean isDefault;
}
