package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetConfirmRequest {

    @NotBlank(message = "token is required")
    private String token;

    /**
     * Strength is checked in the service rather than with a bean-validation pattern, so a weak
     * password comes back as a rendered status on the reset page instead of a field-error payload.
     */
    @NotBlank(message = "newPassword is mandatory")
    private String newPassword;
}
