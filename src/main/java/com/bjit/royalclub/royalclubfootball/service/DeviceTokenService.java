package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.DeviceTokenRequest;

public interface DeviceTokenService {

    /**
     * Register (or re-point) an FCM device token for the currently logged-in player.
     * If the token already exists it is re-assigned to the current player; otherwise a new row is created.
     */
    void registerToken(DeviceTokenRequest request);

    /**
     * Remove a device token (e.g. on logout). No-op if the token does not exist.
     */
    void unregisterToken(String token);
}
