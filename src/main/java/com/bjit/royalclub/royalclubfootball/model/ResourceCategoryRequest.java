package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCategoryRequest {

    @NotBlank(message = "category name is required.")
    @Size(max = 120, message = "category name must be 120 characters or fewer.")
    private String name;

    @Size(max = 120, message = "bangla category name must be 120 characters or fewer.")
    private String nameBn;

    @Size(max = 500, message = "description must be 500 characters or fewer.")
    private String description;

    @Size(max = 60, message = "icon must be 60 characters or fewer.")
    private String icon;

    private Integer sortOrder;

    private Boolean active;
}
