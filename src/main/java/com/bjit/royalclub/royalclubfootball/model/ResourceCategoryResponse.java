package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCategoryResponse {
    private Long id;
    private String name;
    private String nameBn;
    private String slug;
    private String description;
    private String icon;
    private Integer sortOrder;
    private boolean active;
    /**
     * Number of resources the caller is allowed to see in this category.
     */
    private long resourceCount;
}
