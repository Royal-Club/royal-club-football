package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "oldPassword is mandatory")
    private String oldPassword;
    @NotBlank(message = "newPassword is mandatory")
    private String newPassword;
}
