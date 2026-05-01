package com.bjit.royalclub.royalclubfootball.repository.auction;

import com.bjit.royalclub.royalclubfootball.entity.auction.AuctionSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionSettingsRepository extends JpaRepository<AuctionSettings, Long> {
    Optional<AuctionSettings> findByTournamentId(Long tournamentId);
    boolean existsByTournamentId(Long tournamentId);
}
