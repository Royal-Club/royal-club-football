package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkMatchRequest {

    @NotEmpty(message = "Matches list cannot be empty")
    private List<ManualMatchRequest> matches;
}
