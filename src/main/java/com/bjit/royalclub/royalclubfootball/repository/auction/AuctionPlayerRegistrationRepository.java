package com.bjit.royalclub.royalclubfootball.repository.auction;

import com.bjit.royalclub.royalclubfootball.entity.auction.AuctionPlayerRegistration;
import com.bjit.royalclub.royalclubfootball.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionPlayerRegistrationRepository extends JpaRepository<AuctionPlayerRegistration, Long> {
    List<AuctionPlayerRegistration> findByTournamentId(Long tournamentId);
    List<AuctionPlayerRegistration> findByApprovalStatus(ApprovalStatus status);
    List<AuctionPlayerRegistration> findByTournamentIdAndApprovalStatus(Long tournamentId, ApprovalStatus status);
    boolean existsByEmailAndTournamentId(String email, Long tournamentId);
    boolean existsByEmployeeIdAndTournamentId(String employeeId, Long tournamentId);
}
