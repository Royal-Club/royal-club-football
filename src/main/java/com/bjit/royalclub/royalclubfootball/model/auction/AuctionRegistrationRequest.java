package com.bjit.royalclub.royalclubfootball.model.auction;

import com.bjit.royalclub.royalclubfootball.enums.AvailabilityStatus;
import com.bjit.royalclub.royalclubfootball.enums.FootballPosition;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionRegistrationRequest {
    @NotNull
    private Long tournamentId;
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String employeeId;
    private String skypeId;
    private String mobileNo;
    @NotNull
    private FootballPosition playingPosition;
    private String battingStyle;
    private String bowlingStyle;
    private String previousExperience;
    private AvailabilityStatus availabilityStatus;
    private String profilePhoto;
    private String emergencyContact;
    private Integer preferredBasePrice;
}
