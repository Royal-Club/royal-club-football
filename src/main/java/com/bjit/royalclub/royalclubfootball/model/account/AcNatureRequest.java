package com.bjit.royalclub.royalclubfootball.model.account;

import com.bjit.royalclub.royalclubfootball.enums.AcNatureType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AcNatureRequest {

    @NotBlank(message = "Name is required.")
    private String name;

    @NotNull(message = "Code is required.")
    private Integer code;

    private String description;

    @NotNull(message = "Type is required.")
    private AcNatureType type;

    private Integer slNo;
}
