package com.bjit.royalclub.royalclubfootball.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GoalKeeperQueueResponseDto {
    private Long tournamentId;
    private String tournamentName;
    private LocalDateTime tournamentDate;
    private List<GoalKeeperPriorityDto> goalKeeperPriorityQueue;

    /** Tournaments treated as a rest period - anyone who kept goal in them is held back. */
    private Integer cooldownTournaments;

    /**
     * How much of the ledger rests on real data. The accrual side is only as trustworthy as the
     * goalkeeping history behind it: a tournament whose keepers were never entered looks exactly
     * like one that genuinely needed none, so it is reported rather than quietly absorbed.
     */
    private LedgerCoverage ledgerCoverage;

    @Data
    @Builder
    public static class LedgerCoverage {
        /** Past tournaments any ranked player attended - the ledger's span. */
        private Integer tournamentsConsidered;
        /** Of those, how many had at least one keeper recorded. */
        private Integer tournamentsWithRecordedKeepers;
        /** Of those, how many fell back to the configured slot estimate. */
        private Integer tournamentsEstimated;
        /** False when estimation is off, so unrecorded tournaments accrued nothing at all. */
        private Boolean estimatingMissingSlots;
    }
}
