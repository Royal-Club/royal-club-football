package com.bjit.royalclub.royalclubfootball.service.auction;

import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.auction.AuctionSettings;
import com.bjit.royalclub.royalclubfootball.enums.AuctionStatus;
import com.bjit.royalclub.royalclubfootball.exception.ResourceNotFoundException;
import com.bjit.royalclub.royalclubfootball.model.auction.AuctionSettingsRequest;
import com.bjit.royalclub.royalclubfootball.model.auction.AuctionSettingsResponse;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import com.bjit.royalclub.royalclubfootball.repository.auction.AuctionSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuctionSettingsServiceImpl implements AuctionSettingsService {

    private final AuctionSettingsRepository auctionSettingsRepository;
    private final TournamentRepository tournamentRepository;

    @Override
    public AuctionSettingsResponse getSettings(Long tournamentId) {
        return auctionSettingsRepository.findByTournamentId(tournamentId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public AuctionSettingsResponse createSettings(Long tournamentId, AuctionSettingsRequest request) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found: " + tournamentId));

        if (auctionSettingsRepository.existsByTournamentId(tournamentId)) {
            throw new IllegalStateException("Auction settings already exist for this tournament");
        }

        AuctionSettings settings = AuctionSettings.builder()
                .tournament(tournament)
                .teamBudget(request.getTeamBudget())
                .minSquadSize(request.getMinSquadSize())
                .maxSquadSize(request.getMaxSquadSize())
                .auctionTimerSeconds(request.getAuctionTimerSeconds())
                .bidIncrement(request.getBidIncrement())
                .unsoldReauctionEnabled(request.getUnsoldReauctionEnabled() != null ? request.getUnsoldReauctionEnabled() : true)
                .timerExtensionSeconds(request.getTimerExtensionSeconds() != null ? request.getTimerExtensionSeconds() : 15)
                .extendIfBidWithinLastSeconds(request.getExtendIfBidWithinLastSeconds() != null ? request.getExtendIfBidWithinLastSeconds() : 15)
                .minRoleRequirements(request.getMinRoleRequirements())
                .auctionStatus(AuctionStatus.NOT_STARTED)
                .build();

        settings = auctionSettingsRepository.save(settings);
        return mapToResponse(settings);
    }

    @Override
    @Transactional
    public AuctionSettingsResponse updateSettings(Long tournamentId, AuctionSettingsRequest request) {
        AuctionSettings settings = auctionSettingsRepository.findByTournamentId(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction settings not found for tournament: " + tournamentId));

        settings.setTeamBudget(request.getTeamBudget());
        settings.setMinSquadSize(request.getMinSquadSize());
        settings.setMaxSquadSize(request.getMaxSquadSize());
        settings.setAuctionTimerSeconds(request.getAuctionTimerSeconds());
        settings.setBidIncrement(request.getBidIncrement());
        if (request.getUnsoldReauctionEnabled() != null) {
            settings.setUnsoldReauctionEnabled(request.getUnsoldReauctionEnabled());
        }
        if (request.getTimerExtensionSeconds() != null) {
            settings.setTimerExtensionSeconds(request.getTimerExtensionSeconds());
        }
        if (request.getExtendIfBidWithinLastSeconds() != null) {
            settings.setExtendIfBidWithinLastSeconds(request.getExtendIfBidWithinLastSeconds());
        }
        settings.setMinRoleRequirements(request.getMinRoleRequirements());

        settings = auctionSettingsRepository.save(settings);
        return mapToResponse(settings);
    }

    private AuctionSettingsResponse mapToResponse(AuctionSettings settings) {
        return AuctionSettingsResponse.builder()
                .id(settings.getId())
                .tournamentId(settings.getTournament().getId())
                .teamBudget(settings.getTeamBudget())
                .minSquadSize(settings.getMinSquadSize())
                .maxSquadSize(settings.getMaxSquadSize())
                .auctionTimerSeconds(settings.getAuctionTimerSeconds())
                .bidIncrement(settings.getBidIncrement())
                .unsoldReauctionEnabled(settings.getUnsoldReauctionEnabled())
                .timerExtensionSeconds(settings.getTimerExtensionSeconds())
                .extendIfBidWithinLastSeconds(settings.getExtendIfBidWithinLastSeconds())
                .minRoleRequirements(settings.getMinRoleRequirements())
                .auctionStatus(settings.getAuctionStatus())
                .build();
    }
}
