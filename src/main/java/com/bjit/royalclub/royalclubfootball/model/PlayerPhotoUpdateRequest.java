package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerPhotoUpdateRequest {

    @NotBlank(message = "Photo key is mandatory")
    @Size(max = 500, message = "Photo key must be less than 500 characters")
    private String photoKey;
}
