package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchEventUpdateRequest {

    @NotNull(message = "Event time is mandatory")
    @PositiveOrZero(message = "Event time must be zero or positive")
    private Integer eventTime;

    private String description;

    private String details;
}
