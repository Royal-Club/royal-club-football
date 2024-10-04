package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotNull(message = "email is required")
    private String email;
    @NotBlank(message = "newPassword is mandatory")
    private String newPassword;
}
