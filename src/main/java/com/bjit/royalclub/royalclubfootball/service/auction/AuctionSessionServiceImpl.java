package com.bjit.royalclub.royalclubfootball.service.auction;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.TeamPlayer;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.auction.*;
import com.bjit.royalclub.royalclubfootball.enums.*;
import com.bjit.royalclub.royalclubfootball.exception.ResourceNotFoundException;
import com.bjit.royalclub.royalclubfootball.model.auction.*;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamPlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import com.bjit.royalclub.royalclubfootball.repository.auction.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionSessionServiceImpl implements AuctionSessionService {

    private final AuctionSessionRepository sessionRepository;
    private final AuctionPlayerRepository auctionPlayerRepository;
    private final AuctionBidRepository bidRepository;
    private final TeamBudgetRepository teamBudgetRepository;
    private final AuctionSettingsRepository settingsRepository;
    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public AuctionSessionResponse getSession(Long tournamentId) {
        return sessionRepository.findByTournamentId(tournamentId)
                .map(this::mapSessionToResponse)
                .orElse(AuctionSessionResponse.builder()
                        .tournamentId(tournamentId)
                        .status(AuctionSessionStatus.NOT_STARTED)
                        .build());
    }

    @Override
    @Transactional
    public AuctionSessionResponse startAuction(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found: " + tournamentId));

        if (!tournament.isAuctionMode()) {
            throw new IllegalStateException("Tournament is not in auction mode");
        }

        Optional<AuctionSession> existing = sessionRepository.findByTournamentId(tournamentId);
        if (existing.isPresent() && existing.get().getStatus() == AuctionSessionStatus.RUNNING) {
            throw new IllegalStateException("Auction is already running");
        }

        AuctionSession session;
        if (existing.isPresent()) {
            session = existing.get();
            session.setStatus(AuctionSessionStatus.RUNNING);
            session.setStartedAt(LocalDateTime.now());
        } else {
            session = AuctionSession.builder()
                    .tournament(tournament)
                    .status(AuctionSessionStatus.RUNNING)
                    .roundNumber(1)
                    .startedAt(LocalDateTime.now())
                    .build();
        }

        session = sessionRepository.save(session);

        broadcastState(tournamentId, "AUCTION_STARTED", mapSessionToResponse(session));
        return mapSessionToResponse(session);
    }

    @Override
    @Transactional
    public AuctionSessionResponse pauseAuction(Long tournamentId) {
        AuctionSession session = getRunningSession(tournamentId);
        session.setStatus(AuctionSessionStatus.PAUSED);
        session.setPausedAt(LocalDateTime.now());
        session = sessionRepository.save(session);

        broadcastState(tournamentId, "AUCTION_PAUSED", mapSessionToResponse(session));
        return mapSessionToResponse(session);
    }

    @Override
    @Transactional
    public AuctionSessionResponse resumeAuction(Long tournamentId) {
        AuctionSession session = sessionRepository.findByTournamentId(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (session.getStatus() != AuctionSessionStatus.PAUSED) {
            throw new IllegalStateException("Auction is not paused");
        }

        session.setStatus(AuctionSessionStatus.RUNNING);

        // Restore remaining timer time instead of resetting to full duration
        if (session.getCurrentTimerEndsAt() != null && session.getCurrentAuctionPlayer() != null
                && session.getPausedAt() != null) {
            long secondsRemaining = java.time.temporal.ChronoUnit.SECONDS.between(
                    session.getPausedAt(), session.getCurrentTimerEndsAt());
            if (secondsRemaining > 0) {
                session.setCurrentTimerEndsAt(LocalDateTime.now().plusSeconds(secondsRemaining));
            } else {
                // Timer had already expired when paused — clear it
                session.setCurrentTimerEndsAt(null);
            }
        }
        session.setPausedAt(null);

        session = sessionRepository.save(session);
        broadcastState(tournamentId, "AUCTION_RESUMED", mapSessionToResponse(session));
        return mapSessionToResponse(session);
    }

    @Override
    @Transactional
    public AuctionSessionResponse endAuction(Long tournamentId) {
        AuctionSession session = sessionRepository.findByTournamentId(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        // Reset current ON_AUCTION player if any
        if (session.getCurrentAuctionPlayer() != null
                && session.getCurrentAuctionPlayer().getStatus() == AuctionPlayerStatus.ON_AUCTION) {
            AuctionPlayer current = session.getCurrentAuctionPlayer();
            current.setStatus(AuctionPlayerStatus.UNSOLD);
            current.setCurrentBid(null);
            current.setCurrentHighestTeam(null);
            auctionPlayerRepository.save(current);
        }

        session.setStatus(AuctionSessionStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
        session.setCurrentAuctionPlayer(null);
        session.setCurrentTimerEndsAt(null);
        session = sessionRepository.save(session);

        broadcastState(tournamentId, "AUCTION_ENDED", mapSessionToResponse(session));
        return mapSessionToResponse(session);
    }

    @Override
    @Transactional
    public AuctionSessionResponse nextPlayer(Long tournamentId) {
        AuctionSession session = getRunningSession(tournamentId);
        AuctionSettings settings = getOrCreateDefaultSettings(tournamentId);

        // Fix any orphaned ON_AUCTION players (not the current session player)
        resetOrphanedOnAuctionPlayers(session, tournamentId);

        // Find next available player by sequence order
        Optional<AuctionPlayer> nextPlayer = auctionPlayerRepository
                .findFirstByTournamentIdAndStatusOrderBySequenceOrderAsc(tournamentId, AuctionPlayerStatus.AVAILABLE);

        if (nextPlayer.isEmpty()) {
            throw new IllegalStateException("No more available players in the pool");
        }

        return putPlayerOnAuction(session, nextPlayer.get(), settings);
    }

    @Override
    @Transactional
    public AuctionSessionResponse nextPlayerRandom(Long tournamentId) {
        AuctionSession session = getRunningSession(tournamentId);
        AuctionSettings settings = getOrCreateDefaultSettings(tournamentId);

        // Fix any orphaned ON_AUCTION players (not the current session player)
        resetOrphanedOnAuctionPlayers(session, tournamentId);

        List<AuctionPlayer> available = auctionPlayerRepository
                .findByTournamentIdAndStatus(tournamentId, AuctionPlayerStatus.AVAILABLE);

        if (available.isEmpty()) {
            throw new IllegalStateException("No more available players in the pool");
        }

        Random random = new Random();
        AuctionPlayer randomPlayer = available.get(random.nextInt(available.size()));

        return putPlayerOnAuction(session, randomPlayer, settings);
    }

    @Override
    @Transactional
    public AuctionSessionResponse skipPlayer(Long tournamentId) {
        AuctionSession session = getRunningSession(tournamentId);

        if (session.getCurrentAuctionPlayer() == null) {
            throw new IllegalStateException("No player currently on auction");
        }

        AuctionPlayer current = session.getCurrentAuctionPlayer();
        current.setStatus(AuctionPlayerStatus.AVAILABLE);
        current.setCurrentBid(null);
        current.setCurrentHighestTeam(null);
        auctionPlayerRepository.save(current);

        session.setCurrentAuctionPlayer(null);
        session.setCurrentTimerEndsAt(null);
        session = sessionRepository.save(session);

        broadcastState(tournamentId, "PLAYER_SKIPPED", mapSessionToResponse(session));
        return mapSessionToResponse(session);
    }

    @Override
    @Transactional
    public AuctionSessionResponse markSold(Long tournamentId) {
        AuctionSession session = getRunningSession(tournamentId);

        if (session.getCurrentAuctionPlayer() == null) {
            throw new IllegalStateException("No player currently on auction");
        }

        AuctionPlayer auctionPlayer = session.getCurrentAuctionPlayer();

        if (auctionPlayer.getCurrentBid() == null || auctionPlayer.getCurrentHighestTeam() == null) {
            throw new IllegalStateException("No bids placed for this player");
        }

        return executeSale(session, auctionPlayer, tournamentId);
    }

    @Override
    @Transactional
    public AuctionSessionResponse markUnsold(Long tournamentId) {
        AuctionSession session = getRunningSession(tournamentId);

        if (session.getCurrentAuctionPlayer() == null) {
            throw new IllegalStateException("No player currently on auction");
        }

        AuctionPlayer auctionPlayer = session.getCurrentAuctionPlayer();
        auctionPlayer.setStatus(AuctionPlayerStatus.UNSOLD);
        auctionPlayer.setCurrentBid(null);
        auctionPlayer.setCurrentHighestTeam(null);
        auctionPlayerRepository.save(auctionPlayer);

        session.setCurrentAuctionPlayer(null);
        session.setCurrentTimerEndsAt(null);
        session = sessionRepository.save(session);

        broadcastState(tournamentId, "PLAYER_UNSOLD", buildPlayerResponse(auctionPlayer));
        return mapSessionToResponse(session);
    }

    @Override
    @Transactional
    public AuctionSessionResponse undoLastSale(Long tournamentId) {
        Optional<AuctionBid> lastWinning = bidRepository.findLastWinningBid(tournamentId);
        if (lastWinning.isEmpty()) {
            throw new IllegalStateException("No previous sale to undo");
        }

        AuctionBid winningBid = lastWinning.get();
        AuctionPlayer auctionPlayer = winningBid.getAuctionPlayer();
        Team team = winningBid.getTeam();

        // Revert team budget
        TeamBudget budget = teamBudgetRepository.findByTournamentIdAndTeamId(tournamentId, team.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        budget.setRemainingBudget(budget.getRemainingBudget() + winningBid.getBidAmount());
        budget.setTotalSpent(budget.getTotalSpent() - winningBid.getBidAmount());
        budget.setPlayersBought(budget.getPlayersBought() - 1);
        teamBudgetRepository.save(budget);

        // Revert player status
        auctionPlayer.setStatus(AuctionPlayerStatus.AVAILABLE);
        auctionPlayer.setSoldToTeam(null);
        auctionPlayer.setFinalPrice(null);
        auctionPlayer.setCurrentBid(null);
        auctionPlayer.setCurrentHighestTeam(null);
        auctionPlayerRepository.save(auctionPlayer);

        // Mark bid as not winning
        winningBid.setIsWinning(false);
        bidRepository.save(winningBid);

        // Remove the TeamPlayer entry that was created during sale
        teamPlayerRepository.findByTeamIdAndPlayerId(team.getId(), auctionPlayer.getPlayer().getId())
                .ifPresent(teamPlayerRepository::delete);

        AuctionSession session = sessionRepository.findByTournamentId(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        broadcastState(tournamentId, "SALE_UNDONE", buildPlayerResponse(auctionPlayer));
        return mapSessionToResponse(session);
    }

    @Override
    @Transactional
    public AuctionSessionResponse restartBidding(Long tournamentId) {
        AuctionSession session = getRunningSession(tournamentId);

        if (session.getCurrentAuctionPlayer() == null) {
            throw new IllegalStateException("No player currently on auction");
        }

        AuctionPlayer auctionPlayer = session.getCurrentAuctionPlayer();
        auctionPlayer.setCurrentBid(null);
        auctionPlayer.setCurrentHighestTeam(null);
        auctionPlayerRepository.save(auctionPlayer);

        AuctionSettings settings = getOrCreateDefaultSettings(tournamentId);
        session.setCurrentTimerEndsAt(LocalDateTime.now().plusSeconds(settings.getAuctionTimerSeconds()));
        session = sessionRepository.save(session);

        broadcastState(tournamentId, "BIDDING_RESTARTED", mapSessionToResponse(session));
        return mapSessionToResponse(session);
    }

    @Override
    @Transactional
    public AuctionSessionResponse startUnsoldRound(Long tournamentId) {
        AuctionSession session = sessionRepository.findByTournamentId(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        // Reset current ON_AUCTION player if any
        if (session.getCurrentAuctionPlayer() != null
                && session.getCurrentAuctionPlayer().getStatus() == AuctionPlayerStatus.ON_AUCTION) {
            AuctionPlayer current = session.getCurrentAuctionPlayer();
            current.setStatus(AuctionPlayerStatus.UNSOLD);
            current.setCurrentBid(null);
            current.setCurrentHighestTeam(null);
            auctionPlayerRepository.save(current);
        }

        // Move all unsold players back to available
        List<AuctionPlayer> unsoldPlayers = auctionPlayerRepository
                .findByTournamentIdAndStatus(tournamentId, AuctionPlayerStatus.UNSOLD);

        if (unsoldPlayers.isEmpty()) {
            throw new IllegalStateException("No unsold players to re-auction");
        }

        int nextRound = session.getRoundNumber() + 1;
        for (AuctionPlayer player : unsoldPlayers) {
            player.setStatus(AuctionPlayerStatus.AVAILABLE);
            player.setAuctionRound(nextRound);
        }
        auctionPlayerRepository.saveAll(unsoldPlayers);

        session.setRoundNumber(nextRound);
        session.setStatus(AuctionSessionStatus.RUNNING);
        session.setCurrentAuctionPlayer(null);
        session.setCurrentTimerEndsAt(null);
        session = sessionRepository.save(session);

        broadcastState(tournamentId, "UNSOLD_ROUND_STARTED", mapSessionToResponse(session));
        return mapSessionToResponse(session);
    }

    @Override
    @Transactional
    public BidResponse placeBid(Long tournamentId, BidRequest request, Long bidderUserId) {
        AuctionSession session = getRunningSession(tournamentId);
        AuctionSettings settings = getOrCreateDefaultSettings(tournamentId);

        if (session.getCurrentAuctionPlayer() == null) {
            throw new IllegalStateException("No player currently on auction");
        }

        // Check timer
        if (session.getCurrentTimerEndsAt() != null && LocalDateTime.now().isAfter(session.getCurrentTimerEndsAt())) {
            throw new IllegalStateException("Auction timer has expired for this player");
        }

        AuctionPlayer auctionPlayer = auctionPlayerRepository.findByIdForUpdate(session.getCurrentAuctionPlayer().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Auction player not found"));

        if (auctionPlayer.getStatus() != AuctionPlayerStatus.ON_AUCTION) {
            throw new IllegalStateException("Player is not currently on auction");
        }

        // Validate team budget
        TeamBudget budget = teamBudgetRepository.findByTournamentIdAndTeamIdForUpdate(tournamentId, request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team budget not found"));

        Player bidder = playerRepository.findById(bidderUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Bidder not found"));
        boolean isAdminBidder = bidder.getRoles().stream()
            .anyMatch(role -> PlayerRole.ADMIN.name().equals(role.getName()) || PlayerRole.SUPERADMIN.name().equals(role.getName()));

        if (!isAdminBidder && !budget.getOwner().getId().equals(bidderUserId)) {
            throw new IllegalStateException("You are not the owner of this team");
        }

        // Validate bid amount
        int requiredMinBid;
        if (auctionPlayer.getCurrentBid() == null) {
            requiredMinBid = auctionPlayer.getBasePrice();
        } else {
            requiredMinBid = auctionPlayer.getCurrentBid() + settings.getBidIncrement();
        }

        if (request.getBidAmount() < requiredMinBid) {
            throw new IllegalStateException("Bid must be at least " + requiredMinBid);
        }

        if (request.getBidAmount() > budget.getRemainingBudget()) {
            throw new IllegalStateException("Insufficient budget. Remaining: " + budget.getRemainingBudget());
        }

        // Check squad size
        if (budget.getPlayersBought() >= settings.getMaxSquadSize()) {
            throw new IllegalStateException("Team has reached maximum squad size");
        }

        // Place the bid
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        AuctionBid bid = AuctionBid.builder()
                .auctionSession(session)
                .tournament(session.getTournament())
                .auctionPlayer(auctionPlayer)
                .team(team)
                .bidderUser(bidder)
                .bidAmount(request.getBidAmount())
                .bidTime(LocalDateTime.now())
                .isWinning(false)
                .build();
        bid = bidRepository.save(bid);

        // Update auction player current bid
        auctionPlayer.setCurrentBid(request.getBidAmount());
        auctionPlayer.setCurrentHighestTeam(team);
        auctionPlayerRepository.save(auctionPlayer);

        // Timer extension logic
        if (session.getCurrentTimerEndsAt() != null) {
            long secondsRemaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), session.getCurrentTimerEndsAt());
            if (secondsRemaining <= settings.getExtendIfBidWithinLastSeconds()) {
                session.setCurrentTimerEndsAt(
                        session.getCurrentTimerEndsAt().plusSeconds(settings.getTimerExtensionSeconds()));
                sessionRepository.save(session);
            }
        }

        BidResponse bidResponse = mapBidToResponse(bid);

        // Broadcast bid
        Map<String, Object> bidPayload = new HashMap<>();
        bidPayload.put("bid", bidResponse);
        bidPayload.put("remainingSeconds", calculateRemainingSeconds(session));
        broadcastState(tournamentId, "NEW_BID", bidPayload);

        return bidResponse;
    }

    @Override
    public List<BidResponse> getBidsForPlayer(Long tournamentId, Long auctionPlayerId) {
        return bidRepository.findByAuctionPlayerIdOrderByBidTimeAsc(auctionPlayerId)
                .stream().map(this::mapBidToResponse).collect(Collectors.toList());
    }

    @Override
    public List<BidResponse> getAllBids(Long tournamentId) {
        return bidRepository.findByTournamentIdOrderByBidTimeDesc(tournamentId)
                .stream().map(this::mapBidToResponse).collect(Collectors.toList());
    }

    @Override
    public AuctionDashboardResponse getDashboard(Long tournamentId) {
        AuctionSession session = sessionRepository.findByTournamentId(tournamentId).orElse(null);

        List<AuctionPlayer> allPlayers = auctionPlayerRepository.findByTournamentIdOrderBySequenceOrderAsc(tournamentId);
        List<AuctionPlayer> soldPlayers = allPlayers.stream()
                .filter(p -> p.getStatus() == AuctionPlayerStatus.SOLD).toList();
        List<AuctionPlayer> unsoldPlayers = allPlayers.stream()
                .filter(p -> p.getStatus() == AuctionPlayerStatus.UNSOLD).toList();
        List<AuctionPlayer> availablePlayers = allPlayers.stream()
                .filter(p -> p.getStatus() == AuctionPlayerStatus.AVAILABLE).toList();

        AuctionPlayerResponse currentPlayer = null;
        List<BidResponse> currentPlayerBids = new ArrayList<>();
        if (session != null && session.getCurrentAuctionPlayer() != null) {
            currentPlayer = buildPlayerResponse(session.getCurrentAuctionPlayer());
            currentPlayerBids = bidRepository.findByAuctionPlayerIdOrderByBidTimeAsc(session.getCurrentAuctionPlayer().getId())
                    .stream().map(this::mapBidToResponse).collect(Collectors.toList());
        }

        List<TeamBudgetResponse> teamBudgets = teamBudgetRepository.findByTournamentId(tournamentId)
                .stream().map(this::mapBudgetToResponse).collect(Collectors.toList());

        AuctionStatsResponse stats = calculateStats(soldPlayers, tournamentId);

        return AuctionDashboardResponse.builder()
                .session(session != null ? mapSessionToResponse(session) : null)
                .currentPlayer(currentPlayer)
                .currentPlayerBids(currentPlayerBids)
                .teamBudgets(teamBudgets)
                .soldPlayers(soldPlayers.stream().map(this::buildPlayerResponse).collect(Collectors.toList()))
                .unsoldPlayers(unsoldPlayers.stream().map(this::buildPlayerResponse).collect(Collectors.toList()))
                .statistics(stats)
                .totalPlayers((long) allPlayers.size())
                .soldCount((long) soldPlayers.size())
                .unsoldCount((long) unsoldPlayers.size())
                .remainingCount((long) availablePlayers.size())
                .build();
    }

    @Override
    public AuctionResultResponse getResults(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));

        List<TeamBudget> budgets = teamBudgetRepository.findByTournamentId(tournamentId);
        List<AuctionPlayer> allPlayers = auctionPlayerRepository.findByTournamentIdOrderBySequenceOrderAsc(tournamentId);

        List<TeamSquadResponse> teamSquads = budgets.stream().map(budget -> {
            List<AuctionPlayer> teamPlayers = allPlayers.stream()
                    .filter(p -> p.getSoldToTeam() != null && p.getSoldToTeam().getId().equals(budget.getTeam().getId()))
                    .toList();

            return TeamSquadResponse.builder()
                    .teamId(budget.getTeam().getId())
                    .teamName(budget.getTeam().getTeamName())
                    .ownerName(budget.getOwner().getName())
                    .totalBudget(budget.getTotalBudget())
                    .totalSpent(budget.getTotalSpent())
                    .remainingBudget(budget.getRemainingBudget())
                    .playerCount(teamPlayers.size())
                    .players(teamPlayers.stream().map(this::buildPlayerResponse).collect(Collectors.toList()))
                    .build();
        }).collect(Collectors.toList());

        List<AuctionPlayer> unsold = allPlayers.stream()
                .filter(p -> p.getStatus() == AuctionPlayerStatus.UNSOLD).toList();

        List<AuctionPlayer> sold = allPlayers.stream()
                .filter(p -> p.getStatus() == AuctionPlayerStatus.SOLD).toList();

        return AuctionResultResponse.builder()
                .tournamentId(tournamentId)
                .tournamentName(tournament.getName())
                .teamSquads(teamSquads)
                .unsoldPlayers(unsold.stream().map(this::buildPlayerResponse).collect(Collectors.toList()))
                .statistics(calculateStats(sold, tournamentId))
                .build();
    }

    // === Private helpers ===

    private AuctionSettings getOrCreateDefaultSettings(Long tournamentId) {
        return settingsRepository.findByTournamentId(tournamentId).orElseGet(() -> {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tournament not found: " + tournamentId));
            AuctionSettings defaults = AuctionSettings.builder()
                    .tournament(tournament)
                    .teamBudget(100000)
                    .minSquadSize(5)
                    .maxSquadSize(15)
                    .auctionTimerSeconds(60)
                    .bidIncrement(1000)
                    .unsoldReauctionEnabled(true)
                    .timerExtensionSeconds(15)
                    .extendIfBidWithinLastSeconds(15)
                    .auctionStatus(AuctionStatus.NOT_STARTED)
                    .build();
            return settingsRepository.save(defaults);
        });
    }

    private AuctionSession getRunningSession(Long tournamentId) {
        AuctionSession session = sessionRepository.findByTournamentId(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (session.getStatus() != AuctionSessionStatus.RUNNING) {
            throw new IllegalStateException("Auction is not running. Current status: " + session.getStatus());
        }
        return session;
    }

    private void resetOrphanedOnAuctionPlayers(AuctionSession session, Long tournamentId) {
        List<AuctionPlayer> onAuction = auctionPlayerRepository
                .findByTournamentIdAndStatus(tournamentId, AuctionPlayerStatus.ON_AUCTION);
        Long currentPlayerId = session.getCurrentAuctionPlayer() != null
                ? session.getCurrentAuctionPlayer().getId() : null;
        for (AuctionPlayer orphan : onAuction) {
            if (!orphan.getId().equals(currentPlayerId)) {
                log.warn("Resetting orphaned ON_AUCTION player id={} back to AVAILABLE", orphan.getId());
                orphan.setStatus(AuctionPlayerStatus.AVAILABLE);
                orphan.setCurrentBid(null);
                orphan.setCurrentHighestTeam(null);
                auctionPlayerRepository.save(orphan);
            }
        }
    }

    private AuctionSessionResponse putPlayerOnAuction(AuctionSession session, AuctionPlayer player, AuctionSettings settings) {
        // Reset previous player if still ON_AUCTION (prevent orphaning)
        if (session.getCurrentAuctionPlayer() != null
                && session.getCurrentAuctionPlayer().getStatus() == AuctionPlayerStatus.ON_AUCTION) {
            AuctionPlayer previous = session.getCurrentAuctionPlayer();
            previous.setStatus(AuctionPlayerStatus.AVAILABLE);
            previous.setCurrentBid(null);
            previous.setCurrentHighestTeam(null);
            auctionPlayerRepository.save(previous);
        }

        player.setStatus(AuctionPlayerStatus.ON_AUCTION);
        player.setCurrentBid(null);
        player.setCurrentHighestTeam(null);
        auctionPlayerRepository.save(player);

        session.setCurrentAuctionPlayer(player);
        session.setCurrentTimerEndsAt(LocalDateTime.now().plusSeconds(settings.getAuctionTimerSeconds()));
        session = sessionRepository.save(session);

        broadcastState(session.getTournament().getId(), "NEXT_PLAYER", buildPlayerResponse(player));
        return mapSessionToResponse(session);
    }

    private AuctionSessionResponse executeSale(AuctionSession session, AuctionPlayer auctionPlayer, Long tournamentId) {
        Team winningTeam = auctionPlayer.getCurrentHighestTeam();
        int finalPrice = auctionPlayer.getCurrentBid();

        // Update player
        auctionPlayer.setStatus(AuctionPlayerStatus.SOLD);
        auctionPlayer.setSoldToTeam(winningTeam);
        auctionPlayer.setFinalPrice(finalPrice);
        auctionPlayerRepository.save(auctionPlayer);

        // Update budget
        TeamBudget budget = teamBudgetRepository.findByTournamentIdAndTeamIdForUpdate(tournamentId, winningTeam.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        budget.setRemainingBudget(budget.getRemainingBudget() - finalPrice);
        budget.setTotalSpent(budget.getTotalSpent() + finalPrice);
        budget.setPlayersBought(budget.getPlayersBought() + 1);
        teamBudgetRepository.save(budget);

        // Mark winning bid
        Optional<AuctionBid> highestBid = bidRepository.findHighestBidForPlayer(auctionPlayer.getId());
        highestBid.ifPresent(bid -> {
            bid.setIsWinning(true);
            bidRepository.save(bid);
        });

        // Add to team squad
        TeamPlayer teamPlayer = TeamPlayer.builder()
                .team(winningTeam)
                .player(auctionPlayer.getPlayer())
                .playingPosition(auctionPlayer.getPlayer().getPosition())
                .teamPlayerRole(TeamPlayerRole.PLAYER)
                .isCaptain(false)
                .build();
        teamPlayerRepository.save(teamPlayer);

        // Clear session current player
        session.setCurrentAuctionPlayer(null);
        session.setCurrentTimerEndsAt(null);
        session = sessionRepository.save(session);

        Map<String, Object> soldPayload = new HashMap<>();
        soldPayload.put("player", buildPlayerResponse(auctionPlayer));
        soldPayload.put("teamName", winningTeam.getTeamName());
        soldPayload.put("finalPrice", finalPrice);
        soldPayload.put("teamRemainingBudget", budget.getRemainingBudget());
        broadcastState(tournamentId, "PLAYER_SOLD", soldPayload);

        return mapSessionToResponse(session);
    }

    @Override
    @Transactional
    public void notifyTimerExpired(Long tournamentId) {
        Optional<AuctionSession> sessionOpt = sessionRepository.findByTournamentIdAndStatus(
                tournamentId, AuctionSessionStatus.RUNNING);
        if (sessionOpt.isEmpty()) return;

        AuctionSession session = sessionOpt.get();
        if (session.getCurrentTimerEndsAt() == null || session.getCurrentAuctionPlayer() == null) return;
        if (LocalDateTime.now().isBefore(session.getCurrentTimerEndsAt())) return;

        // Clear the timer but keep the player ON_AUCTION — admin must manually sell or mark unsold
        session.setCurrentTimerEndsAt(null);
        sessionRepository.save(session);

        broadcastState(tournamentId, "TIMER_EXPIRED", buildPlayerResponse(session.getCurrentAuctionPlayer()));
    }

    private Long calculateRemainingSeconds(AuctionSession session) {
        if (session.getCurrentTimerEndsAt() == null) return null;
        long remaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), session.getCurrentTimerEndsAt());
        return Math.max(0, remaining);
    }

    private AuctionStatsResponse calculateStats(List<AuctionPlayer> soldPlayers, Long tournamentId) {
        if (soldPlayers.isEmpty()) {
            return AuctionStatsResponse.builder().build();
        }

        AuctionPlayer mostExpensive = soldPlayers.stream()
                .max(Comparator.comparingInt(AuctionPlayer::getFinalPrice)).orElse(null);
        AuctionPlayer cheapest = soldPlayers.stream()
                .min(Comparator.comparingInt(AuctionPlayer::getFinalPrice)).orElse(null);
        int totalSpent = soldPlayers.stream().mapToInt(AuctionPlayer::getFinalPrice).sum();
        int avgPrice = totalSpent / soldPlayers.size();

        return AuctionStatsResponse.builder()
                .mostExpensivePlayerName(mostExpensive != null ? mostExpensive.getPlayer().getName() : null)
                .mostExpensivePrice(mostExpensive != null ? mostExpensive.getFinalPrice() : null)
                .cheapestSoldPlayerName(cheapest != null ? cheapest.getPlayer().getName() : null)
                .cheapestSoldPrice(cheapest != null ? cheapest.getFinalPrice() : null)
                .averageSalePrice(avgPrice)
                .totalMoneySpent(totalSpent)
                .build();
    }

    private void broadcastState(Long tournamentId, String type, Object payload) {
        AuctionWebSocketMessage message = AuctionWebSocketMessage.builder()
                .type(type)
                .tournamentId(tournamentId)
                .payload(payload)
                .build();
        messagingTemplate.convertAndSend("/topic/auction/" + tournamentId, message);
    }

    private AuctionSessionResponse mapSessionToResponse(AuctionSession session) {
        AuctionPlayerResponse currentPlayer = null;
        if (session.getCurrentAuctionPlayer() != null) {
            currentPlayer = buildPlayerResponse(session.getCurrentAuctionPlayer());
        }

        return AuctionSessionResponse.builder()
                .id(session.getId())
                .tournamentId(session.getTournament().getId())
                .status(session.getStatus())
                .currentPlayer(currentPlayer)
                .roundNumber(session.getRoundNumber())
                .startedAt(session.getStartedAt())
                .currentTimerEndsAt(session.getCurrentTimerEndsAt())
                .remainingSeconds(calculateRemainingSeconds(session))
                .build();
    }

    private AuctionPlayerResponse buildPlayerResponse(AuctionPlayer ap) {
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

    private BidResponse mapBidToResponse(AuctionBid bid) {
        return BidResponse.builder()
                .id(bid.getId())
                .auctionPlayerId(bid.getAuctionPlayer().getId())
                .teamId(bid.getTeam().getId())
                .teamName(bid.getTeam().getTeamName())
                .bidderUserId(bid.getBidderUser().getId())
                .bidderName(bid.getBidderUser().getName())
                .bidAmount(bid.getBidAmount())
                .bidTime(bid.getBidTime())
                .isWinning(bid.getIsWinning())
                .build();
    }

    private TeamBudgetResponse mapBudgetToResponse(TeamBudget budget) {
        return TeamBudgetResponse.builder()
                .id(budget.getId())
                .tournamentId(budget.getTournament().getId())
                .teamId(budget.getTeam().getId())
                .teamName(budget.getTeam().getTeamName())
                .ownerId(budget.getOwner().getId())
                .ownerName(budget.getOwner().getName())
                .totalBudget(budget.getTotalBudget())
                .remainingBudget(budget.getRemainingBudget())
                .totalSpent(budget.getTotalSpent())
                .playersBought(budget.getPlayersBought())
                .build();
    }

    @Override
    @Transactional
    public AuctionSessionResponse selectPlayerForAuction(Long tournamentId, Long playerId) {
        AuctionSession session = getRunningSession(tournamentId);
        AuctionSettings settings = getOrCreateDefaultSettings(tournamentId);

        // Fetch the player by ID
        AuctionPlayer player = auctionPlayerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found"));

        // Ensure the player is either AVAILABLE or UNSOLD
        if (player.getStatus() != AuctionPlayerStatus.AVAILABLE && player.getStatus() != AuctionPlayerStatus.UNSOLD) {
            throw new IllegalStateException("Player is not available for auction");
        }

        // Reset the status of the current auction player if necessary
        resetOrphanedOnAuctionPlayers(session, tournamentId);

        // Put the selected player on auction
        return putPlayerOnAuction(session, player, settings);
    }
}
