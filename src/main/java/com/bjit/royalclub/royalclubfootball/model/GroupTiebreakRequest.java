package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Manual penalty-shootout tiebreak for a group. The admin supplies the team
 * IDs in their final finishing order (after the shootout) for the set of teams
 * that were level on every other criterion. The first ID gets tiebreakRank 1
 * (ranks highest), and so on.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupTiebreakRequest {

    @NotEmpty(message = "orderedTeamIds must contain at least one team")
    private List<Long> orderedTeamIds;
}
