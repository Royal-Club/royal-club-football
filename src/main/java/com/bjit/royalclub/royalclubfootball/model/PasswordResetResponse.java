package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.PasswordResetStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * What the public reset pages render. Every step of the flow answers with one of these, including
 * the failures, because a dead link is a normal outcome here rather than a server error.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PasswordResetResponse {

    private final PasswordResetStatus status;

    /** Set only once a link has been verified, so the page can greet the right member. */
    private final String playerName;

    private final String message;
}
