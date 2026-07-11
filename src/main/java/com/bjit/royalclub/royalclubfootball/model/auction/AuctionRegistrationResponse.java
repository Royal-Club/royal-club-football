package com.bjit.royalclub.royalclubfootball.model.auction;

import com.bjit.royalclub.royalclubfootball.enums.ApprovalStatus;
import com.bjit.royalclub.royalclubfootball.enums.AvailabilityStatus;
import com.bjit.royalclub.royalclubfootball.enums.FootballPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionRegistrationResponse {
    private Long id;
    private Long tournamentId;
    private String tournamentName;
    private String name;
    private String email;
    private String employeeId;
    private String skypeId;
    private String mobileNo;
    private FootballPosition playingPosition;
    private String battingStyle;
    private String bowlingStyle;
    private String previousExperience;
    private AvailabilityStatus availabilityStatus;
    private String profilePhoto;
    private String emergencyContact;
    private Integer preferredBasePrice;
    private ApprovalStatus approvalStatus;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private LocalDateTime createdDate;
    private Long createdPlayerId;
    private boolean inAuctionPool;
}
