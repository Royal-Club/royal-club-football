package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlayerUpdateRequest {
    @NotBlank(message = "Name is mandatory")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;
    @NotBlank(message = "Employee ID is mandatory")
    @Size(max = 255, message = "Employee ID must be less than 255 characters")
    private String employeeId;
    @NotBlank(message = "skype ID is mandatory")
    @Size(max = 255, message = "Skype ID must be less than 255 characters")
    private String skypeId;

    @Size(max = 255, message = "Mobile No must be less than 255 characters")
    private String mobileNo;
    private boolean isActive;
}
