package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.PasswordResetConfirmRequest;
import com.bjit.royalclub.royalclubfootball.model.PasswordResetResponse;

/**
 * Emailed password recovery, for the member who cannot sign in at all.
 * <p>
 * Distinct from {@link AuthService#changePassword} (needs the old password) and
 * {@link AuthService#resetPassword} (an admin typing a new one) - here the signed link in the
 * member's mailbox is the only credential.
 */
public interface PasswordResetService {

    /**
     * Self-service, from the login page - the only way a reset link is ever sent. Answers the same
     * way for an unknown address.
     */
    PasswordResetResponse requestReset(String email);

    /** Read-only check so the page knows whether to show the form or an explanation. */
    PasswordResetResponse validate(String token);

    PasswordResetResponse confirm(PasswordResetConfirmRequest request);
}
