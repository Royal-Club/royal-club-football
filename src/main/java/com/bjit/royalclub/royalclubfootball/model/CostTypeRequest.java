package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CostTypeRequest {
    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Description is mandatory")
    private String description;
}
