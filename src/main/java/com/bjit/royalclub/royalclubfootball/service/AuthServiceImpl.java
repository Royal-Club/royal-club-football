package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Role;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.model.ChangePasswordRequest;
import com.bjit.royalclub.royalclubfootball.model.LoginRequest;
import com.bjit.royalclub.royalclubfootball.model.LoginResponse;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        return LoginResponse.builder()
                .userId(player.getId())
                .username(player.getName())
                .email(player.getEmail())
                .roles(player.getRoles().stream().map(Role::getName).toList())
                .token(token)
                .build();
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {

        Player loggedInPlayer = getLoggedInPlayer();

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), loggedInPlayer.getPassword())) {
            throw new PlayerServiceException(PASSWORD_MISMATCH_EXCEPTION, HttpStatus.UNAUTHORIZED);
        }
        loggedInPlayer.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        playerRepository.save(loggedInPlayer);

    }
}
