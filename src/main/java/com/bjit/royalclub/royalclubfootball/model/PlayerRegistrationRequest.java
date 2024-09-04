package com.bjit.royalclub.royalclubfootball.model;


import com.bjit.royalclub.royalclubfootball.enums.FootballPosition;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlayerRegistrationRequest {

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
    @Size(max = 255, message = "skype ID must be less than 255 characters")
    private String skypeId;
    @Size(max = 255, message = "mobile No must be less than 255 characters")
    private String mobileNo;
    /*TODO("Password will be strong. Need to set a rule/pattern for this")*/
    @Size(max = 255, message = "Password No must be less than 255 characters")
    private String password;
    @NotNull(message = "Position is mandatory")
    private FootballPosition playingPosition;
}