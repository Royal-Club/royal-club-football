package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.ChangePasswordRequest;
import com.bjit.royalclub.royalclubfootball.model.LoginRequest;
import com.bjit.royalclub.royalclubfootball.model.LoginResponse;
import com.bjit.royalclub.royalclubfootball.model.ResetPasswordRequest;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);

    void changePassword(ChangePasswordRequest changePasswordRequest);

    void resetPassword(ResetPasswordRequest resetPasswordRequest);
}
