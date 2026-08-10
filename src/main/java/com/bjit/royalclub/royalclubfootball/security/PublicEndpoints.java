package com.bjit.royalclub.royalclubfootball.security;

import org.springframework.stereotype.Component;

@Component
public class PublicEndpoints {

    public String[] getPublicGetEndpoints() {
        return new String[]{
                "/football-positions",
                "/players/{id}",
                "/files/view-url",
                "/files/local/**",
                "/tournaments/details",
                "/tournament-participants",
                "/venues",
                "/tournaments",
                "/tournaments/sessions",
                "/tournaments/list",
                // RSVP email links: the signed token authenticates the request, not a session.
                "/rsvp/preview"
        };
    }

    public String[] getPublicPostEndpoints() {
        return new String[]{
                "/auth/login",
                "/players",
                "/files/presign",
                "/auction/tournaments/*/register",
                "/rsvp/vote"
        };
    }

    public String[] putPublicPostEndpoints() {
        return new String[]{
                "/auth/change-password",
                "/files/local/**"
        };
    }
}
