package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentListResponse {
    private Long id;
    private String name;
    private String tournamentDate; // Format: dd-MM-yyyy (e.g., "25-12-2025")
}

