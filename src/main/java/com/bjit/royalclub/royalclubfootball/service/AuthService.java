package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.ChangePasswordRequest;
import com.bjit.royalclub.royalclubfootball.model.LoginRequest;
import com.bjit.royalclub.royalclubfootball.model.LoginResponse;
import com.bjit.royalclub.royalclubfootball.model.ResetPasswordRequest;
import com.bjit.royalclub.royalclubfootball.model.TokenRefreshResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);

    /**
     * Trades a refresh token for a new access token, rotating the refresh token in the process.
     *
     * @throws com.bjit.royalclub.royalclubfootball.exception.SecurityException with 401 when the
     *                                                                         session can no longer be renewed and the member has to sign in again.
     */
    TokenRefreshResponse refresh(String refreshToken);

    /** Ends the session behind this refresh token. Idempotent. */
    void logout(String refreshToken);

    void changePassword(ChangePasswordRequest changePasswordRequest);

    void resetPassword(ResetPasswordRequest resetPasswordRequest);
}
