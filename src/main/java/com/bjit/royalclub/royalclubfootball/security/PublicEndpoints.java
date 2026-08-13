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
                "/rsvp/preview",
                // Password reset links: reached by a member who cannot sign in at all.
                "/auth/password-reset/validate"
        };
    }

    public String[] getPublicPostEndpoints() {
        return new String[]{
                "/auth/login",
                // Renewal and sign-out authenticate with the refresh token in the body, and are
                // reached precisely when the access token is no longer good.
                "/auth/refresh",
                "/auth/logout",
                "/players",
                "/files/presign",
                "/auction/tournaments/*/register",
                "/rsvp/vote",
                "/auth/forgot-password",
                "/auth/password-reset/confirm"
        };
    }

    public String[] putPublicPostEndpoints() {
        return new String[]{
                "/auth/change-password",
                "/files/local/**"
        };
    }
}
