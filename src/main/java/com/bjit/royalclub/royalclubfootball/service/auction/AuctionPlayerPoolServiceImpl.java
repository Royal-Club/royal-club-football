package com.bjit.royalclub.royalclubfootball.service.auction;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.auction.AuctionPlayer;
import com.bjit.royalclub.royalclubfootball.entity.auction.AuctionPlayerRegistration;
import com.bjit.royalclub.royalclubfootball.enums.ApprovalStatus;
import com.bjit.royalclub.royalclubfootball.enums.AuctionPlayerStatus;
import com.bjit.royalclub.royalclubfootball.enums.AuctionPlayerType;
import com.bjit.royalclub.royalclubfootball.exception.ResourceNotFoundException;
import com.bjit.royalclub.royalclubfootball.model.auction.AuctionPlayerRequest;
import com.bjit.royalclub.royalclubfootball.model.auction.AuctionPlayerResponse;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import com.bjit.royalclub.royalclubfootball.repository.auction.AuctionPlayerRegistrationRepository;
import com.bjit.royalclub.royalclubfootball.repository.auction.AuctionPlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionPlayerPoolServiceImpl implements AuctionPlayerPoolService {

    private final AuctionPlayerRepository auctionPlayerRepository;
    private final PlayerRepository playerRepository;
    private final TournamentRepository tournamentRepository;
    private final AuctionPlayerRegistrationRepository registrationRepository;

    @Override
    public List<AuctionPlayerResponse> getPlayerPool(Long tournamentId) {
        return auctionPlayerRepository.findByTournamentIdOrderBySequenceOrderAsc(tournamentId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AuctionPlayerResponse addExistingPlayer(Long tournamentId, AuctionPlayerRequest request) {
        if (request.getPlayerId() == null) {
            throw new IllegalStateException("playerId is required when adding an existing player");
        }
        if (request.getBasePrice() == null) {
            throw new IllegalStateException("basePrice is required");
        }

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found: " + tournamentId));

        if (!tournament.isAuctionMode()) {
            throw new IllegalStateException("Tournament is not in auction mode");
        }

        Player player = playerRepository.findById(request.getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + request.getPlayerId()));

        if (auctionPlayerRepository.existsByTournamentIdAndPlayerId(tournamentId, request.getPlayerId())) {
            throw new IllegalStateException("Player already in auction pool for this tournament");
        }

        AuctionPlayer auctionPlayer = AuctionPlayer.builder()
                .tournament(tournament)
                .player(player)
                .playerType(AuctionPlayerType.EXISTING)
                .category(request.getCategory())
                .basePrice(request.getBasePrice())
                .status(AuctionPlayerStatus.AVAILABLE)
                .auctionRound(1)
                .sequenceOrder(request.getSequenceOrder())
                .build();

        auctionPlayer = auctionPlayerRepository.save(auctionPlayer);
        return mapToResponse(auctionPlayer);
    }

    @Override
    @Transactional
    public AuctionPlayerResponse addFromRegistration(Long tournamentId, Long registrationId, AuctionPlayerRequest request) {
        if (request.getBasePrice() == null) {
            throw new IllegalStateException("basePrice is required");
        }

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found: " + tournamentId));

        AuctionPlayerRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found: " + registrationId));

        if (registration.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Registration is not approved");
        }

        if (registration.getCreatedPlayer() == null) {
            throw new IllegalStateException("No player record created for this registration");
        }

        Player player = registration.getCreatedPlayer();

        if (auctionPlayerRepository.existsByTournamentIdAndPlayerId(tournamentId, player.getId())) {
            throw new IllegalStateException("Player already in auction pool");
        }

        AuctionPlayer auctionPlayer = AuctionPlayer.builder()
                .tournament(tournament)
                .player(player)
                .playerType(AuctionPlayerType.OUTSIDE)
                .category(request.getCategory())
                .basePrice(request.getBasePrice())
                .status(AuctionPlayerStatus.AVAILABLE)
                .auctionRound(1)
                .sequenceOrder(request.getSequenceOrder())
                .build();

        auctionPlayer = auctionPlayerRepository.save(auctionPlayer);
        return mapToResponse(auctionPlayer);
    }

    @Override
    @Transactional
    public AuctionPlayerResponse updatePlayer(Long tournamentId, Long auctionPlayerId, AuctionPlayerRequest request) {
        AuctionPlayer auctionPlayer = auctionPlayerRepository.findById(auctionPlayerId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction player not found: " + auctionPlayerId));

        if (!auctionPlayer.getTournament().getId().equals(tournamentId)) {
            throw new IllegalStateException("Player does not belong to this tournament");
        }

        if (auctionPlayer.getStatus() == AuctionPlayerStatus.SOLD) {
            throw new IllegalStateException("Cannot update a sold player");
        }

        if (request.getBasePrice() != null) {
            auctionPlayer.setBasePrice(request.getBasePrice());
        }
        if (request.getCategory() != null) {
            auctionPlayer.setCategory(request.getCategory());
        }
        if (request.getSequenceOrder() != null) {
            auctionPlayer.setSequenceOrder(request.getSequenceOrder());
        }

        auctionPlayer = auctionPlayerRepository.save(auctionPlayer);
        return mapToResponse(auctionPlayer);
    }

    @Override
    @Transactional
    public void removePlayer(Long tournamentId, Long auctionPlayerId) {
        AuctionPlayer auctionPlayer = auctionPlayerRepository.findById(auctionPlayerId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction player not found: " + auctionPlayerId));

        if (!auctionPlayer.getTournament().getId().equals(tournamentId)) {
            throw new IllegalStateException("Player does not belong to this tournament");
        }

        if (auctionPlayer.getStatus() == AuctionPlayerStatus.SOLD) {
            throw new IllegalStateException("Cannot remove a sold player");
        }

        auctionPlayer.setStatus(AuctionPlayerStatus.WITHDRAWN);
        auctionPlayerRepository.save(auctionPlayer);
    }

    private AuctionPlayerResponse mapToResponse(AuctionPlayer ap) {
        return AuctionPlayerResponse.builder()
                .id(ap.getId())
                .tournamentId(ap.getTournament().getId())
                .playerId(ap.getPlayer().getId())
                .playerName(ap.getPlayer().getName())
                .playerEmail(ap.getPlayer().getEmail())
                .playingPosition(ap.getPlayer().getPosition())
                .playerType(ap.getPlayerType())
                .category(ap.getCategory())
                .basePrice(ap.getBasePrice())
                .currentBid(ap.getCurrentBid())
                .currentHighestTeamId(ap.getCurrentHighestTeam() != null ? ap.getCurrentHighestTeam().getId() : null)
                .currentHighestTeamName(ap.getCurrentHighestTeam() != null ? ap.getCurrentHighestTeam().getTeamName() : null)
                .soldToTeamId(ap.getSoldToTeam() != null ? ap.getSoldToTeam().getId() : null)
                .soldToTeamName(ap.getSoldToTeam() != null ? ap.getSoldToTeam().getTeamName() : null)
                .finalPrice(ap.getFinalPrice())
                .status(ap.getStatus())
                .auctionRound(ap.getAuctionRound())
                .playerRating(ap.getPlayerRating())
                .sequenceOrder(ap.getSequenceOrder())
                .build();
    }
}
