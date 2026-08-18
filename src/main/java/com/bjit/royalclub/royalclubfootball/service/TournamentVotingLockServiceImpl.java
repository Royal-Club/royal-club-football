package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipant;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.model.VotingLockResponse;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentParticipantRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.VOTING_LOCK_NEEDS_UPCOMING;
import static com.bjit.royalclub.royalclubfootball.enums.ParticipationSource.AUTO_LOCK;
import static com.bjit.royalclub.royalclubfootball.enums.TournamentStatus.UPCOMING;
import static com.bjit.royalclub.royalclubfootball.security.util.SecurityUtil.getLoggedInUserId;
import static com.bjit.royalclub.royalclubfootball.security.util.SecurityUtil.isTournamentManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentVotingLockServiceImpl implements TournamentVotingLockService {

    private final TournamentRepository tournamentRepository;
    private final TournamentParticipantRepository participantRepository;
    private final PlayerRepository playerRepository;

    @Override
    @Transactional
    public VotingLockResponse lock(Long tournamentId) {
        Tournament tournament = load(tournamentId);

        // Idempotent: a second press must not re-stamp anyone or move the "locked by" name onto
        // whoever happened to click last.
        if (tournament.isVotingLocked()) {
            return describe(tournament, 0);
        }
        if (!tournament.isActive() || tournament.getTournamentStatus() != UPCOMING) {
            throw new TournamentServiceException(VOTING_LOCK_NEEDS_UPCOMING, HttpStatus.CONFLICT);
        }

        int autoMarked = stampSilentPlayersAsNo(tournament);

        tournament.setVotingLocked(true);
        tournament.setVotingLockedBy(getLoggedInUserId());
        // Stored timestamps are UTC; say so rather than trusting the host's default zone.
        tournament.setVotingLockedAt(LocalDateTime.now(ZoneOffset.UTC));
        tournamentRepository.save(tournament);

        log.info("Voting locked on tournament {} by player {}; {} silent player(s) recorded as No.",
                tournamentId, tournament.getVotingLockedBy(), autoMarked);
        return describe(tournament, autoMarked);
    }

    @Override
    @Transactional
    public VotingLockResponse unlock(Long tournamentId) {
        Tournament tournament = load(tournamentId);
        if (!tournament.isVotingLocked()) {
            return describe(tournament, 0);
        }

        // Clear the flag first: the bulk delete below clears the persistence context, which would
        // otherwise detach the tournament and turn this save into a merge.
        tournament.setVotingLocked(false);
        tournament.setVotingLockedBy(null);
        tournament.setVotingLockedAt(null);
        tournamentRepository.save(tournament);

        // Undo the stamping, not the answers: only rows this lock created are removed, so anyone
        // who really did decline - or whose answer a manager edited since - keeps it.
        int reverted = participantRepository.deleteByTournamentIdAndSource(tournamentId, AUTO_LOCK);

        log.info("Voting reopened on tournament {}; {} auto-recorded No(s) reverted to pending.",
                tournamentId, reverted);
        return describe(tournament, reverted);
    }

    @Override
    @Transactional(readOnly = true)
    public VotingLockResponse status(Long tournamentId) {
        return describe(load(tournamentId), 0);
    }

    @Override
    public void requireVotingOpen(Tournament tournament) {
        if (!tournament.isVotingLocked() || isTournamentManager()) {
            return;
        }
        throw new TournamentServiceException(votingClosedMessage(tournament), HttpStatus.CONFLICT);
    }

    @Override
    public String lockedByName(Tournament tournament) {
        if (tournament.getVotingLockedBy() == null) {
            return null;
        }
        return playerRepository.findById(tournament.getVotingLockedBy())
                .map(Player::getName)
                .orElse(null);
    }

    /**
     * Writes a No for every active player with no answer on record, tagged {@code AUTO_LOCK}.
     * <p>
     * These are inserts, not updates: silence is the absence of a row everywhere else in the app,
     * which is exactly why the tag matters - it is the only thing that keeps "declined" and "never
     * replied" apart once the rows exist.
     */
    private int stampSilentPlayersAsNo(Tournament tournament) {
        List<Player> silent = playerRepository.findActivePlayersWithoutParticipation(tournament.getId());
        if (silent.isEmpty()) {
            return 0;
        }
        // Explicit witness: SuperBuilder's build() returns a captured wildcard, which infers to
        // List<capture-of-?> without it.
        List<TournamentParticipant> stamped = silent.stream()
                .<TournamentParticipant>map(player -> TournamentParticipant.builder()
                        .tournament(tournament)
                        .player(player)
                        .participationStatus(false)
                        .participationSource(AUTO_LOCK)
                        .build())
                .toList();
        participantRepository.saveAll(stamped);
        return stamped.size();
    }

    private String votingClosedMessage(Tournament tournament) {
        String contact = lockedByName(tournament);
        return contact == null
                ? "Voting is closed for this tournament. Contact a coordinator to change your answer."
                : "Voting is closed for this tournament. Contact " + contact + " to change your answer.";
    }

    private VotingLockResponse describe(Tournament tournament, int autoMarkedCount) {
        Long id = tournament.getId();
        return VotingLockResponse.builder()
                .tournamentId(id)
                .votingLocked(tournament.isVotingLocked())
                .lockedById(tournament.getVotingLockedBy())
                .lockedByName(lockedByName(tournament))
                .lockedAt(tournament.getVotingLockedAt())
                .confirmedCount(participantRepository.countByTournamentIdAndParticipationStatusTrue(id))
                .declinedCount(participantRepository.countByTournamentIdAndParticipationStatusFalse(id))
                .pendingCount(playerRepository.findActivePlayersWithoutParticipation(id).size())
                .autoMarkedCount(autoMarkedCount)
                .build();
    }

    private Tournament load(Long tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
    }
}
