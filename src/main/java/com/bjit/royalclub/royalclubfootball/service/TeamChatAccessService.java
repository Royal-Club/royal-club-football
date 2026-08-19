package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.TeamPlayer;
import com.bjit.royalclub.royalclubfootball.enums.TournamentStatus;
import com.bjit.royalclub.royalclubfootball.exception.TeamServiceException;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import com.bjit.royalclub.royalclubfootball.util.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TEAM_CHAT_CONCLUDED;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TEAM_CHAT_NOT_A_MEMBER;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TEAM_CHAT_NOT_OPEN;

/**
 * The one place that decides who may enter a team room, and whether the room is there to enter.
 *
 * <p>Every chat entry point - reading history, posting, uploading, downloading a file, and the
 * WebSocket subscription - goes through here rather than repeating the rule. A private room is only
 * as private as its least careful caller, and the download route in particular is the one people
 * forget: the file is the message.
 *
 * <p>Access has exactly two parts, checked in this order:
 * <ol>
 *   <li><b>Membership.</b> The caller holds a {@code TeamPlayer} row for this team. Nothing else
 *       qualifies - not being a captain of the other side, not being an admin. Admins were
 *       deliberately left out: a players' room that management can read is not the thing that was
 *       asked for.</li>
 *   <li><b>The room exists.</b> Its line-up has been published, and its tournament has not
 *       concluded.</li>
 * </ol>
 *
 * <p>Membership is checked first and reported with the same message whether or not the room exists,
 * so an outsider probing team ids learns nothing about which teams are talking.
 */
@Service
@RequiredArgsConstructor
public class TeamChatAccessService {

    private final TeamRepository teamRepository;

    /**
     * The team behind an open room the caller belongs to.
     *
     * @throws TeamServiceException 403 if the caller is not in the squad, 409 if the room is not
     *                              open (never published, or purged when the tournament finished)
     */
    @Transactional(readOnly = true)
    public Team requireOpenRoom(Long teamId) {
        Team team = requireMembership(teamId);
        requireOpen(team);
        return team;
    }

    /**
     * The team, provided the caller is in its squad - whether or not the room is currently open.
     *
     * <p>Used by the endpoint that describes a room, which has to be able to answer "not published
     * yet" and "deleted when the tournament ended" rather than simply refusing.
     */
    @Transactional(readOnly = true)
    public Team requireMembership(Long teamId) {
        Team team = teamRepository.findByIdWithPlayersAndTournament(teamId)
                // Deliberately the membership message and not "team not found": a missing team and a
                // team you are not in must be indistinguishable from outside.
                .orElseThrow(() -> new TeamServiceException(TEAM_CHAT_NOT_A_MEMBER, HttpStatus.FORBIDDEN));

        Long playerId = CurrentUserUtil.currentPlayerId()
                .orElseThrow(() -> new TeamServiceException(TEAM_CHAT_NOT_A_MEMBER, HttpStatus.FORBIDDEN));

        if (!isMember(team, playerId)) {
            throw new TeamServiceException(TEAM_CHAT_NOT_A_MEMBER, HttpStatus.FORBIDDEN);
        }
        return team;
    }

    /** Throws unless the room is open right now. */
    public void requireOpen(Team team) {
        closedReason(team).ifPresent(reason -> {
            throw new TeamServiceException(reason, HttpStatus.CONFLICT);
        });
    }

    /**
     * Why the room cannot be used, or empty when it can.
     *
     * <p>Returned rather than thrown so the room endpoint can render the reason instead of failing:
     * "the line-up is not out yet" is normal for most of the week, not an error.
     */
    public Optional<String> closedReason(Team team) {
        if (team.getTournament() != null
                && team.getTournament().getTournamentStatus() == TournamentStatus.CONCLUDED) {
            // Checked ahead of chatOpenedAt so a concluded tournament always reads as concluded,
            // whether or not the purge sweep has run yet.
            return Optional.of(TEAM_CHAT_CONCLUDED);
        }
        if (team.getChatOpenedAt() == null) {
            return Optional.of(TEAM_CHAT_NOT_OPEN);
        }
        return Optional.empty();
    }

    /** True when this player holds a squad place in the team. */
    public boolean isMember(Team team, Long playerId) {
        if (team.getTeamPlayers() == null || playerId == null) {
            return false;
        }
        return team.getTeamPlayers().stream()
                .map(TeamPlayer::getPlayer)
                .filter(Objects::nonNull)
                .anyMatch(player -> Objects.equals(player.getId(), playerId));
    }

    /**
     * Membership test for callers that are not on a request thread - the WebSocket subscription
     * check, which has a player id from the session rather than from the security context.
     */
    @Transactional(readOnly = true)
    public boolean canSubscribe(Long teamId, Long playerId) {
        return teamRepository.findByIdWithPlayersAndTournament(teamId)
                .filter(team -> isMember(team, playerId))
                .filter(team -> closedReason(team).isEmpty())
                .isPresent();
    }
}
