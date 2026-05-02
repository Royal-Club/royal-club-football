package com.bjit.royalclub.royalclubfootball.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignedUploadResponse {
    private final String uploadUrl;
    private final String key;
    private final long expiresInSeconds;
}
