package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "email is required")
    @Email(message = "a valid email is required")
    private String email;
}
