package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.TeamFormationRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamFormationResponse;

import java.util.List;
import java.util.Optional;

/**
 * Line-ups a team captain lays out on the pitch view. A team has one default
 * formation plus, optionally, one per match.
 *
 * <p>Reads never write: asking for a formation that has not been saved yet
 * returns the preset (or the team default) the client should start from, with
 * {@code saved = false}. Nothing is persisted until someone saves.
 */
public interface TeamFormationService {

    /** The team's default line-up, or the starting preset when none is saved. */
    TeamFormationResponse getDefaultFormation(Long teamId);

    /**
     * The signed-in member's own team line-up for a tournament.
     *
     * <p>Empty when nobody is signed in or the caller was not put on a team — the
     * clients that ask this are showing a member their own side, and having no
     * side is an ordinary answer rather than an error.
     */
    Optional<TeamFormationResponse> getMyFormation(Long tournamentId);

    /**
     * The line-up for one match — falling back to the team default, then to the
     * preset, when the captain has not set the match up separately yet.
     */
    TeamFormationResponse getMatchFormation(Long teamId, Long matchId);

    /** Both sides' line-ups for a match, for the public match page. */
    List<TeamFormationResponse> getMatchFormations(Long matchId);

    TeamFormationResponse saveDefaultFormation(Long teamId, TeamFormationRequest request);

    TeamFormationResponse saveMatchFormation(Long teamId, Long matchId, TeamFormationRequest request);

    /** Drops the match-specific line-up so the team falls back to its default. */
    void resetMatchFormation(Long teamId, Long matchId);
}
