package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VenueRegistrationRequest {
    @NotBlank(message = "Venue name is required.")
    @Size(max = 100, message = "Venue name must not exceed 100 characters.")
    private String name;

    @NotBlank(message = "Venue address is required.")
    @Size(max = 255, message = "Venue address must not exceed 255 characters.")
    private String address;

    @Size(max = 500, message = "Map link must not exceed 500 characters.")
    @Pattern(regexp = "^\\s*$|^https?://\\S+$", message = "Map link must be a valid http:// or https:// URL.")
    private String mapUrl;
}
