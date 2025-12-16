package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchOrderUpdateRequest {

    @NotNull(message = "Match orders are required")
    private List<MatchOrderItem> matchOrders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchOrderItem {
        @NotNull(message = "Match ID is required")
        private Long matchId;

        @NotNull(message = "Match order is required")
        private Integer matchOrder;
    }
}

