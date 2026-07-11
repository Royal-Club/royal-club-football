package com.bjit.royalclub.royalclubfootball.model.auction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamBudgetRequest {
    @NotNull
    private Long teamId;
    @NotNull
    private Long ownerId;
    @NotNull
    @Min(1)
    private Integer totalBudget;
}
