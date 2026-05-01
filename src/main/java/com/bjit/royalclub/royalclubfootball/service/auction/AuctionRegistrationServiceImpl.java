package com.bjit.royalclub.royalclubfootball.service.auction;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Role;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.auction.AuctionPlayer;
import com.bjit.royalclub.royalclubfootball.entity.auction.AuctionPlayerRegistration;
import com.bjit.royalclub.royalclubfootball.enums.ApprovalStatus;
import com.bjit.royalclub.royalclubfootball.enums.AuctionPlayerCategory;
import com.bjit.royalclub.royalclubfootball.enums.AuctionPlayerStatus;
import com.bjit.royalclub.royalclubfootball.enums.AuctionPlayerType;
import com.bjit.royalclub.royalclubfootball.enums.AvailabilityStatus;
import com.bjit.royalclub.royalclubfootball.enums.PlayerRole;
import com.bjit.royalclub.royalclubfootball.exception.ResourceNotFoundException;
import com.bjit.royalclub.royalclubfootball.model.auction.AuctionRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.auction.AuctionRegistrationResponse;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.RoleRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import com.bjit.royalclub.royalclubfootball.repository.auction.AuctionPlayerRegistrationRepository;
import com.bjit.royalclub.royalclubfootball.repository.auction.AuctionPlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionRegistrationServiceImpl implements AuctionRegistrationService {

    private final AuctionPlayerRegistrationRepository registrationRepository;
    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuctionPlayerRepository auctionPlayerRepository;

    @Value("${player.default-password}")
    private String defaultPassword;

    @Override
    @Transactional
    public AuctionRegistrationResponse register(AuctionRegistrationRequest request) {
        Tournament tournament = tournamentRepository.findById(request.getTournamentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found: " + request.getTournamentId()));

        if (!tournament.isAuctionMode()) {
            throw new IllegalStateException("Tournament is not in auction mode");
        }

        if (registrationRepository.existsByEmailAndTournamentId(request.getEmail(), request.getTournamentId())) {
            throw new IllegalStateException("Already registered with this email for this tournament");
        }

        if (registrationRepository.existsByEmployeeIdAndTournamentId(request.getEmployeeId(), request.getTournamentId())) {
            throw new IllegalStateException("Already registered with this employee ID for this tournament");
        }

        AuctionPlayerRegistration registration = AuctionPlayerRegistration.builder()
                .tournament(tournament)
                .name(request.getName())
                .email(request.getEmail())
                .employeeId(request.getEmployeeId())
                .skypeId(request.getSkypeId())
                .mobileNo(request.getMobileNo())
                .playingPosition(request.getPlayingPosition())
                .battingStyle(request.getBattingStyle())
                .bowlingStyle(request.getBowlingStyle())
                .previousExperience(request.getPreviousExperience())
                .availabilityStatus(request.getAvailabilityStatus() != null ? request.getAvailabilityStatus() : AvailabilityStatus.AVAILABLE)
                .profilePhoto(request.getProfilePhoto())
                .emergencyContact(request.getEmergencyContact())
                .preferredBasePrice(request.getPreferredBasePrice())
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        registration = registrationRepository.save(registration);
        return mapToResponse(registration);
    }

    @Override
    @Transactional
    public AuctionRegistrationResponse quickRegisterExistingPlayer(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found: " + tournamentId));

        if (!tournament.isAuctionMode()) {
            throw new IllegalStateException("Tournament is not in auction mode");
        }

        // Get current logged-in player
        Long currentUserId = getCurrentUserId();
        Player player = playerRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found"));

        // Check if already registered
        if (registrationRepository.existsByEmailAndTournamentId(player.getEmail(), tournamentId)) {
            throw new IllegalStateException("You are already registered for this auction");
        }

        // Auto-register with APPROVED status (existing players don't need admin approval)
        AuctionPlayerRegistration registration = AuctionPlayerRegistration.builder()
                .tournament(tournament)
                .name(player.getName())
                .email(player.getEmail())
                .employeeId(player.getEmployeeId())
                .skypeId(player.getSkypeId())
                .mobileNo(player.getMobileNo())
                .playingPosition(player.getPosition())
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .approvalStatus(ApprovalStatus.APPROVED)
                .approvedAt(LocalDateTime.now())
                .createdPlayer(player)
                .build();

        registration = registrationRepository.save(registration);
        return mapToResponse(registration);
    }

    @Override
    public List<AuctionRegistrationResponse> getRegistrations(Long tournamentId, String status) {
        List<AuctionPlayerRegistration> registrations;
        if (tournamentId != null && status != null) {
            registrations = registrationRepository.findByTournamentIdAndApprovalStatus(
                    tournamentId, ApprovalStatus.valueOf(status));
        } else if (tournamentId != null) {
            registrations = registrationRepository.findByTournamentId(tournamentId);
        } else if (status != null) {
            registrations = registrationRepository.findByApprovalStatus(ApprovalStatus.valueOf(status));
        } else {
            registrations = registrationRepository.findAll();
        }
        return registrations.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public AuctionRegistrationResponse getRegistration(Long id) {
        AuctionPlayerRegistration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found: " + id));
        return mapToResponse(registration);
    }

    @Override
    @Transactional
    public AuctionRegistrationResponse approve(Long id) {
        AuctionPlayerRegistration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found: " + id));

        if (registration.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Registration is not in pending state");
        }

        Long currentUserId = getCurrentUserId();
        Player approver = playerRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        // Create player record if not already existing
        Player existingPlayer = playerRepository.findByEmail(registration.getEmail()).orElse(null);
        if (existingPlayer == null) {
            Role outsidePlayerRole = roleRepository.findByName(PlayerRole.OUTSIDE_PLAYER.name())
                    .orElseThrow(() -> new ResourceNotFoundException("OUTSIDE_PLAYER role not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(outsidePlayerRole);

            existingPlayer = Player.builder()
                    .name(registration.getName())
                    .email(registration.getEmail())
                    .employeeId(registration.getEmployeeId())
                    .skypeId(registration.getSkypeId() != null ? registration.getSkypeId() : registration.getEmployeeId())
                    .mobileNo(registration.getMobileNo())
                    .position(registration.getPlayingPosition())
                    .password(passwordEncoder.encode(defaultPassword))
                    .isActive(true)
                    .roles(roles)
                    .build();
            existingPlayer = playerRepository.save(existingPlayer);
        }

        registration.setApprovalStatus(ApprovalStatus.APPROVED);
        registration.setApprovedBy(approver);
        registration.setApprovedAt(LocalDateTime.now());
        registration.setCreatedPlayer(existingPlayer);
        registrationRepository.save(registration);

        return mapToResponse(registration);
    }

    @Override
    @Transactional
    public AuctionRegistrationResponse reject(Long id, String reason) {
        AuctionPlayerRegistration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found: " + id));

        if (registration.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Registration is not in pending state");
        }

        registration.setApprovalStatus(ApprovalStatus.REJECTED);
        registration.setRejectionReason(reason);
        registrationRepository.save(registration);

        return mapToResponse(registration);
    }

    @Override
    @Transactional
    public AuctionRegistrationResponse undoReject(Long id) {
        AuctionPlayerRegistration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found: " + id));

        if (registration.getApprovalStatus() != ApprovalStatus.REJECTED) {
            throw new IllegalStateException("Registration is not rejected");
        }

        registration.setApprovalStatus(ApprovalStatus.PENDING);
        registration.setRejectionReason(null);
        registrationRepository.save(registration);

        return mapToResponse(registration);
    }

    @Override
    @Transactional
    public AuctionRegistrationResponse approveAndAddToPool(Long id, AuctionPlayerCategory category, Integer basePrice) {
        if (basePrice == null || basePrice < 1) {
            throw new IllegalStateException("basePrice is required and must be at least 1");
        }

        AuctionPlayerRegistration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found: " + id));

        Long tournamentId = registration.getTournament().getId();

        // Step 1: Approve if not already approved
        if (registration.getApprovalStatus() == ApprovalStatus.REJECTED) {
            throw new IllegalStateException("Rejected registrations cannot be added to pool");
        }

        if (registration.getApprovalStatus() == ApprovalStatus.PENDING) {
            Long currentUserId = getCurrentUserId();
            Player approver = playerRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

            Player existingPlayer = playerRepository.findByEmail(registration.getEmail()).orElse(null);
            if (existingPlayer == null) {
                Role outsidePlayerRole = roleRepository.findByName(PlayerRole.OUTSIDE_PLAYER.name())
                        .orElseThrow(() -> new ResourceNotFoundException("OUTSIDE_PLAYER role not found"));
                Set<Role> roles = new HashSet<>();
                roles.add(outsidePlayerRole);
                existingPlayer = Player.builder()
                        .name(registration.getName())
                        .email(registration.getEmail())
                        .employeeId(registration.getEmployeeId())
                        .skypeId(registration.getSkypeId() != null ? registration.getSkypeId() : registration.getEmployeeId())
                        .mobileNo(registration.getMobileNo())
                        .position(registration.getPlayingPosition())
                        .password(passwordEncoder.encode(defaultPassword))
                        .isActive(true)
                        .roles(roles)
                        .build();
                existingPlayer = playerRepository.save(existingPlayer);
            }
            registration.setApprovalStatus(ApprovalStatus.APPROVED);
            registration.setApprovedBy(approver);
            registration.setApprovedAt(java.time.LocalDateTime.now());
            registration.setCreatedPlayer(existingPlayer);
            registration = registrationRepository.save(registration);
        }

        // Step 2: Add to pool if not already there
        Player player = registration.getCreatedPlayer();
        if (player == null) {
            throw new IllegalStateException("No player record found for this registration");
        }

        if (!auctionPlayerRepository.existsByTournamentIdAndPlayerId(tournamentId, player.getId())) {
            Tournament tournament = registration.getTournament();
            AuctionPlayerType playerType = registration.getApprovedBy() != null ? AuctionPlayerType.OUTSIDE : AuctionPlayerType.EXISTING;
            AuctionPlayer auctionPlayer = AuctionPlayer.builder()
                    .tournament(tournament)
                    .player(player)
                    .playerType(playerType)
                    .category(category)
                    .basePrice(basePrice)
                    .status(AuctionPlayerStatus.AVAILABLE)
                    .auctionRound(1)
                    .build();
            auctionPlayerRepository.save(auctionPlayer);
        }

        return mapToResponse(registration);
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof com.bjit.royalclub.royalclubfootball.security.UserPrinciple userPrinciple) {
            return userPrinciple.getId();
        }
        throw new ResourceNotFoundException("Current user not found");
    }

    private AuctionRegistrationResponse mapToResponse(AuctionPlayerRegistration reg) {
        Long createdPlayerId = reg.getCreatedPlayer() != null ? reg.getCreatedPlayer().getId() : null;
        boolean inPool = createdPlayerId != null &&
                auctionPlayerRepository.existsByTournamentIdAndPlayerId(reg.getTournament().getId(), createdPlayerId);
        return AuctionRegistrationResponse.builder()
                .id(reg.getId())
                .tournamentId(reg.getTournament().getId())
                .tournamentName(reg.getTournament().getName())
                .name(reg.getName())
                .email(reg.getEmail())
                .employeeId(reg.getEmployeeId())
                .skypeId(reg.getSkypeId())
                .mobileNo(reg.getMobileNo())
                .playingPosition(reg.getPlayingPosition())
                .battingStyle(reg.getBattingStyle())
                .bowlingStyle(reg.getBowlingStyle())
                .previousExperience(reg.getPreviousExperience())
                .availabilityStatus(reg.getAvailabilityStatus())
                .profilePhoto(reg.getProfilePhoto())
                .emergencyContact(reg.getEmergencyContact())
                .preferredBasePrice(reg.getPreferredBasePrice())
                .approvalStatus(reg.getApprovalStatus())
                .approvedByName(reg.getApprovedBy() != null ? reg.getApprovedBy().getName() : null)
                .approvedAt(reg.getApprovedAt())
                .rejectionReason(reg.getRejectionReason())
                .createdDate(reg.getCreatedDate())
                .createdPlayerId(createdPlayerId)
                .inAuctionPool(inPool)
                .build();
    }
}
