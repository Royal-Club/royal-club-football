package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.exception.PasswordResetTokenException;
import com.bjit.royalclub.royalclubfootball.model.ChangePasswordRequest;
import com.bjit.royalclub.royalclubfootball.model.ForgotPasswordRequest;
import com.bjit.royalclub.royalclubfootball.model.LoginRequest;
import com.bjit.royalclub.royalclubfootball.model.LoginResponse;
import com.bjit.royalclub.royalclubfootball.model.PasswordResetConfirmRequest;
import com.bjit.royalclub.royalclubfootball.model.PasswordResetResponse;
import com.bjit.royalclub.royalclubfootball.model.ResetPasswordRequest;
import com.bjit.royalclub.royalclubfootball.model.TokenRefreshRequest;
import com.bjit.royalclub.royalclubfootball.model.TokenRefreshResponse;
import com.bjit.royalclub.royalclubfootball.service.AuthService;
import com.bjit.royalclub.royalclubfootball.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Supplier;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.LOGIN_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.UPDATE_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildResponse;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return buildSuccessResponse(HttpStatus.OK, LOGIN_OK, loginResponse);
    }

    /**
     * Public: reached precisely when the access token is no longer good, so it cannot itself require
     * one. The refresh token in the body is the credential.
     */
    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authService.refresh(request.getRefreshToken());
        return buildSuccessResponse(HttpStatus.OK, LOGIN_OK, response);
    }

    /**
     * Public and idempotent: a device signing out may well be holding an expired access token, and
     * a sign-out that fails for that reason would strand a live refresh token on the server.
     */
    @PostMapping("/logout")
    public ResponseEntity<Object> logout(@Valid @RequestBody TokenRefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return buildResponse(HttpStatus.OK, UPDATE_OK);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Object> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        authService.changePassword(changePasswordRequest);
        return buildResponse(HttpStatus.OK, UPDATE_OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PutMapping("reset-password")
    public ResponseEntity<Object> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        authService.resetPassword(resetPasswordRequest);
        return buildResponse(HttpStatus.OK, UPDATE_OK);
    }

    /**
     * Public: the member cannot sign in, so there is nobody to authenticate. Answers identically
     * whether or not the address belongs to an account.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return respond(() -> passwordResetService.requestReset(request.getEmail()));
    }

    /**
     * Public, read-only: tells the reset page whether the link in the URL is worth showing a form
     * for. Has no side effect, because mail scanners follow links in transit.
     */
    @GetMapping("/password-reset/validate")
    public ResponseEntity<Object> validateResetLink(@RequestParam String token) {
        return respond(() -> passwordResetService.validate(token));
    }

    /** Public: the signed token in the body is the credential. */
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Object> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        return respond(() -> passwordResetService.confirm(request));
    }

    /**
     * A dead or forged link is a normal outcome of an emailed flow, not a server error: it is
     * answered with a status the page can render rather than an error the browser has to interpret.
     */
    private ResponseEntity<Object> respond(Supplier<PasswordResetResponse> action) {
        try {
            return buildSuccessResponse(HttpStatus.OK, FETCH_OK, action.get());
        } catch (PasswordResetTokenException e) {
            return buildSuccessResponse(HttpStatus.OK, FETCH_OK, PasswordResetResponse.builder()
                    .status(e.getStatus())
                    .message(e.getMessage())
                    .build());
        }
    }
}
