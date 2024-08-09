package com.bjit.royalclub.royalclubfootball.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerResponse {
    private Long id;
    private String name;
    private String email;
}
