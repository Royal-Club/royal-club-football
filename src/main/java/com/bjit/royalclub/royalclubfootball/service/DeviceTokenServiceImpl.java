package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.PlayerDeviceToken;
import com.bjit.royalclub.royalclubfootball.model.DeviceTokenRequest;
import com.bjit.royalclub.royalclubfootball.repository.PlayerDeviceTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.bjit.royalclub.royalclubfootball.security.util.SecurityUtil.getLoggedInPlayer;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private final PlayerDeviceTokenRepository deviceTokenRepository;

    @Override
    @Transactional
    public void registerToken(DeviceTokenRequest request) {
        Player player = getLoggedInPlayer();

        PlayerDeviceToken deviceToken = deviceTokenRepository.findByToken(request.getToken())
                .orElseGet(() -> PlayerDeviceToken.builder()
                        .token(request.getToken())
                        .build());

        // Re-point the token to the current player (a device may be reused by another logged-in player)
        deviceToken.setPlayer(player);
        deviceToken.setPlatform(request.getPlatform());
        deviceTokenRepository.save(deviceToken);

        log.info("Registered device token for player {} on platform {}", player.getId(), request.getPlatform());
    }

    @Override
    @Transactional
    public void unregisterToken(String token) {
        deviceTokenRepository.deleteByToken(token);
        log.info("Unregistered device token");
    }
}
