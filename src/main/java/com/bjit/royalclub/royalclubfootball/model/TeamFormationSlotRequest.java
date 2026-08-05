package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamFormationSlotRequest {

    /** Null leaves the slot empty — a captain may save a half-filled sheet. */
    private Long teamPlayerId;

    @NotNull(message = "Position group is required")
    private String positionGroup;

    private String slotLabel;

    @NotNull(message = "Slot x is required")
    @DecimalMin(value = "0.0", message = "Slot x must be within the pitch")
    @DecimalMax(value = "100.0", message = "Slot x must be within the pitch")
    private BigDecimal x;

    @NotNull(message = "Slot y is required")
    @DecimalMin(value = "0.0", message = "Slot y must be within the pitch")
    @DecimalMax(value = "100.0", message = "Slot y must be within the pitch")
    private BigDecimal y;

    /** False marks a bench place; bench order follows the list order. */
    private Boolean isStarter;
}
