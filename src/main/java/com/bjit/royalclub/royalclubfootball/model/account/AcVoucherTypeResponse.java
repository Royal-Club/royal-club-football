package com.bjit.royalclub.royalclubfootball.model.account;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AcVoucherTypeResponse {
    private Long id;
    private String name;
    private String alias;
    private String description;
    private boolean isDefault;
}
