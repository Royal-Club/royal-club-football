package com.bjit.royalclub.royalclubfootball.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TournamentRoundResponse {

    private Long id;
    private Long tournamentId;
    private String tournamentName;
    private Integer roundNumber;
    private String roundName;
    private String roundType;
    private String advancementRule;
    private String status;
    private Integer sequenceOrder;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Nested data
    private List<RoundGroupResponse> groups;
    private List<TeamSimpleResponse> teams;
    private Integer totalMatches;
    private Integer completedMatches;

    /**
     * IDs of the rounds that feed into this one, derived from the bracket's
     * logic-node edges (source -> target). Empty/absent for an entry round.
     * Used to gate "Start Round" on the actual predecessors rather than the
     * adjacent sequence number, so parallel brackets (e.g. Cup vs Plate) stay
     * independent.
     */
    private List<Long> sourceRoundIds;
}
