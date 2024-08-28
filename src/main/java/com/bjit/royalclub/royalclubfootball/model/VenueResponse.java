package com.bjit.royalclub.royalclubfootball.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VenueResponse {
    private Long id;
    private String name;
    private String address;
    private boolean active;
}
