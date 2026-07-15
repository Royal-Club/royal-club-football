package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.PlayerDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerDeviceTokenRepository extends JpaRepository<PlayerDeviceToken, Long> {

    Optional<PlayerDeviceToken> findByToken(String token);

    List<PlayerDeviceToken> findAllByPlayerId(Long playerId);

    List<PlayerDeviceToken> findAllByPlayerIdIn(List<Long> playerIds);

    void deleteByToken(String token);
}
