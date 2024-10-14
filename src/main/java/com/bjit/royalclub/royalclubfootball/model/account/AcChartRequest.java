package com.bjit.royalclub.royalclubfootball.model.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for creating or updating an AcChart.
 */
@Data
@Builder
public class AcChartRequest {

    @NotBlank(message = "Name is required.")
    private String name;

    @NotBlank(message = "Code is required.")
    private String code;

    private String description;

    @NotNull(message = "Nature ID is required.")
    private Long natureId;

    private Long parentId;
}
