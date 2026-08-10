package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipant;
import com.bjit.royalclub.royalclubfootball.enums.RsvpVoteStatus;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.model.RsvpTokenPayload;
import com.bjit.royalclub.royalclubfootball.model.RsvpVoteResponse;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentParticipantRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import com.bjit.royalclub.royalclubfootball.util.RsvpTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.enums.TournamentStatus.UPCOMING;

/**
 * Handles the one-click Yes/No links from RSVP emails.
 * <p>
 * Runs unauthenticated: the signed token is the credential, and it names both the player and the
 * answer, so there is nothing for a caller to spoof by editing the URL.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RsvpVoteServiceImpl implements RsvpVoteService {

    private final RsvpTokenUtil rsvpTokenUtil;
    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final TournamentParticipantRepository participantRepository;

    @Override
    @Transactional(readOnly = true)
    public RsvpVoteResponse preview(String token) {
        RsvpTokenPayload payload = rsvpTokenUtil.parse(token);
        Tournament tournament = loadTournament(payload.tournamentId());
        Player player = loadPlayer(payload.playerId());

        RsvpVoteStatus blocked = checkTournamentOpen(tournament);
        if (blocked != null) {
            return describe(blocked, player, tournament, payload.attending(), messageFor(blocked, payload.attending()));
        }

        return describe(RsvpVoteStatus.RECORDED, player, tournament, payload.attending(),
                "Confirm your answer below.");
    }

    @Override
    @Transactional
    public RsvpVoteResponse vote(String token) {
        RsvpTokenPayload payload = rsvpTokenUtil.parse(token);
        Tournament tournament = loadTournament(payload.tournamentId());
        Player player = loadPlayer(payload.playerId());

        RsvpVoteStatus blocked = checkTournamentOpen(tournament);
        if (blocked != null) {
            return describe(blocked, player, tournament, payload.attending(), messageFor(blocked, payload.attending()));
        }

        Optional<TournamentParticipant> existing =
                participantRepository.findByTournamentIdAndPlayerId(tournament.getId(), player.getId());

        TournamentParticipant participant = existing.orElseGet(() -> TournamentParticipant.builder()
                .tournament(tournament)
                .player(player)
                .build());
        participant.setParticipationStatus(payload.attending());
        participantRepository.save(participant);

        // An existing row means they had already answered; the link stays live until kickoff precisely
        // so a member can change their mind, so this overwrites rather than rejecting.
        RsvpVoteStatus status = existing.isPresent() ? RsvpVoteStatus.UPDATED : RsvpVoteStatus.RECORDED;
        log.info("RSVP {} via email link: player {} is {} for tournament {}.",
                status, player.getId(), payload.attending() ? "IN" : "OUT", tournament.getId());

        return describe(status, player, tournament, payload.attending(),
                messageFor(status, payload.attending()));
    }

    /**
     * @return the blocking status, or null when the tournament can still accept answers.
     */
    private RsvpVoteStatus checkTournamentOpen(Tournament tournament) {
        if (!tournament.isActive() || tournament.getTournamentStatus() != UPCOMING) {
            return RsvpVoteStatus.TOURNAMENT_CANCELLED;
        }
        // Stored dates are UTC (the JVM default zone is pinned to UTC and clients post UTC).
        if (!tournament.getTournamentDate().isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
            return RsvpVoteStatus.TOURNAMENT_STARTED;
        }
        return null;
    }

    private Tournament loadTournament(Long tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private Player loadPlayer(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private RsvpVoteResponse describe(RsvpVoteStatus status, Player player, Tournament tournament,
                                      boolean attending, String message) {
        return RsvpVoteResponse.builder()
                .status(status)
                .playerName(player.getName())
                .tournamentName(tournament.getName())
                .tournamentDate(tournament.getTournamentDate())
                .venueName(tournament.getVenue() != null ? tournament.getVenue().getName() : null)
                .attending(attending)
                .message(message)
                .build();
    }

    private String messageFor(RsvpVoteStatus status, boolean attending) {
        return switch (status) {
            case RECORDED -> attending
                    ? "You are in. See you on the pitch!"
                    : "Thanks for letting us know you cannot make it.";
            case UPDATED -> attending
                    ? "Your answer has been changed to Yes."
                    : "Your answer has been changed to No.";
            case TOURNAMENT_CANCELLED -> "This tournament has been cancelled.";
            case TOURNAMENT_STARTED -> "This tournament has already started.";
            case EXPIRED -> "This link has expired.";
            case INVALID -> "This link is not valid.";
        };
    }
}
