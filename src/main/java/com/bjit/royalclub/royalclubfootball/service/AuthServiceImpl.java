package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.constant.AuthConstants;
import com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Role;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.model.ChangePasswordRequest;
import com.bjit.royalclub.royalclubfootball.model.LoginRequest;
import com.bjit.royalclub.royalclubfootball.model.LoginResponse;
import com.bjit.royalclub.royalclubfootball.model.ResetPasswordRequest;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.INCORRECT_EMAIL;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PASSWORD_MISMATCH_EXCEPTION;
import static com.bjit.royalclub.royalclubfootball.security.util.SecurityUtil.getLoggedInPlayer;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    private final JWTUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        Player player = playerRepository
                .findByEmailAndIsActiveTrue(loginRequest.getEmail())
                .orElseThrow(() -> new PlayerServiceException(INCORRECT_EMAIL, HttpStatus.UNAUTHORIZED));
        if (!passwordEncoder.matches(loginRequest.getPassword(), player.getPassword())) {
            throw new PlayerServiceException(PASSWORD_MISMATCH_EXCEPTION, HttpStatus.UNAUTHORIZED);
        }
        String token = jwtUtil.generateToken(player.getEmail(),
                player.getRoles().stream().map(Role::getName).toList());

        Boolean resetPasswordNeeded = isResetPasswordNeeded(player);

        return LoginResponse.builder()
                .userId(player.getId())
                .username(player.getName())
                .email(player.getEmail())
                .roles(player.getRoles().stream().map(Role::getName).toList())
                .token(token)
                .resetPassword(resetPasswordNeeded)
                .build();
    }

    private Boolean isResetPasswordNeeded(Player player) {
        // Check 1: If lastPasswordChangeDate is null (new player or admin reset)
        if (player.getLastPasswordChangeDate() == null) {
            return true;
        }

        // Check 2: If lastPasswordChangeDate is older than 90 days
        LocalDateTime expiryDate = player.getLastPasswordChangeDate().plusDays(AuthConstants.PASSWORD_EXPIRY_DAYS);
        return LocalDateTime.now().isAfter(expiryDate);
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {

        Player loggedInPlayer = getLoggedInPlayer();

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), loggedInPlayer.getPassword())) {
            throw new PlayerServiceException(PASSWORD_MISMATCH_EXCEPTION, HttpStatus.UNAUTHORIZED);
        }

        // Check if new password is same as old password
        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), loggedInPlayer.getPassword())) {
            throw new PlayerServiceException(RestErrorMessageDetail.NEW_PASSWORD_SAME_AS_OLD, HttpStatus.BAD_REQUEST);
        }

        loggedInPlayer.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        loggedInPlayer.setLastPasswordChangeDate(LocalDateTime.now());
        playerRepository.save(loggedInPlayer);

    }

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {

        Player player = playerRepository.findByEmail(resetPasswordRequest.getEmail())
                .orElseThrow(() -> new PlayerServiceException(INCORRECT_EMAIL, HttpStatus.NOT_FOUND));

        player.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        player.setLastPasswordChangeDate(null);
        playerRepository.save(player);
    }

}
