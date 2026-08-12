package com.bjit.royalclub.royalclubfootball.exception;

import com.bjit.royalclub.royalclubfootball.enums.PasswordResetStatus;
import lombok.Getter;

/**
 * Thrown when a password-reset link cannot be honoured. Carries the status so the public endpoint
 * can tell an expired link from a spent one or a forged one, rather than collapsing all three into
 * a single "invalid link" that leaves the member with nothing to act on.
 */
@Getter
public class PasswordResetTokenException extends RuntimeException {

    private final PasswordResetStatus status;

    public PasswordResetTokenException(PasswordResetStatus status, String message) {
        super(message);
        this.status = status;
    }
}
