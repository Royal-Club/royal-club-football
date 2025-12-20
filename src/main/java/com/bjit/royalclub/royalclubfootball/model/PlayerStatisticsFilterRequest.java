package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.FootballPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatisticsFilterRequest {

    private Long tournamentId;         // Filter by tournament participation (null = all players; provided = only players in teams for that tournament)
    private FootballPosition position;  // Filter by player position
    private String sortBy;              // Sort field: goals, assists, goalsAssists, matches
    private String sortOrder;           // ASC or DESC
    private Integer limit;              // Limit results
    private Integer offset;             // Pagination offset

    // Default values
    public String getSortBy() {
        return sortBy != null ? sortBy : "goalsAssists";
    }

    public String getSortOrder() {
        return sortOrder != null ? sortOrder.toUpperCase() : "DESC";
    }

    public Integer getLimit() {
        return limit != null ? limit : 100;
    }

    public Integer getOffset() {
        return offset != null ? offset : 0;
    }
}
