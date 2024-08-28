package com.bjit.royalclub.royalclubfootball.security;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.security.UserPrinciple;
import com.bjit.royalclub.royalclubfootball.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final PlayerService playerService;

    @Override
    public UserDetails loadUserByUsername(String userName) {
        Player player = playerService.findByEmail(userName);
        return UserPrinciple.create(player);
    }
}
