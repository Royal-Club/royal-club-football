package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamLogoUploadResponse {
    private String key;
    private String url;
    private String uploadUrl;
    private Long expiresInSeconds;
}
