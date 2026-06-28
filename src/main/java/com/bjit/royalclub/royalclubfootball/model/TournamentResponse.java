package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.TournamentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TournamentResponse {
    private Long id;
    private String name;
    private String title;
    private String season;
    private String description;
    private String rules;
    private String roadmapImageUrl;
    private LocalDateTime tournamentDate;
    private String venueName;
    private boolean activeStatus;
    private boolean defaultTournament;

    private TournamentStatus tournamentStatus;
    private List<TournamentTeamResponse> teams;

    // Fixture system fields
    private String sportType;
    private String tournamentType;
    private Integer groupCount;

    // Auction - always include in JSON (NON_EMPTY would suppress false for primitives)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private boolean auctionMode;
}
