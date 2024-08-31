package com.bjit.royalclub.royalclubfootball.security;

import org.springframework.stereotype.Component;

@Component
public class PublicEndpoints {

    public String[] getPublicGetEndpoints() {
        return new String[]{
                "/football-positions",
                "/players/{id}",
                "/tournaments/details",
                "/tournament-participants",
                "/venues",
                "/tournaments"
        };
    }

    public String[] getPublicPostEndpoints() {
        return new String[]{
                "/players/login",
                "/players"
        };
    }

    public String[] putPublicPostEndpoints() {
        return new String[]{
                "/auth/change-password"
        };
    }
}
