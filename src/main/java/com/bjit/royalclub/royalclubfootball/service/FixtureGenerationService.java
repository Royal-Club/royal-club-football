package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Match;
import com.bjit.royalclub.royalclubfootball.model.FixtureGenerationRequest;
import com.bjit.royalclub.royalclubfootball.model.MatchFixtureUpdateRequest;
import jakarta.transaction.Transactional;

import java.util.List;

public interface FixtureGenerationService {

    /**
     * Generate simple fixtures from team IDs and match dates
     * System automatically creates team combinations as matches
     * @param tournamentId Tournament ID
     * @param fixtureRequest Request containing team IDs and match dates with time gaps
     * @return List of generated matches
     */
    @Transactional
    List<Match> generateFixtures(Long tournamentId, FixtureGenerationRequest fixtureRequest);

    /**
     * Clear all existing fixtures for a tournament
     * @param tournamentId Tournament ID
     */
    @Transactional
    void clearFixtures(Long tournamentId);

    /**
     * Update match fixture details (date and venue)
     * @param matchId Match ID
     * @param updateRequest Request containing new match date and/or venue ID
     * @return Updated match entity
     */
    @Transactional
    Match updateMatchFixture(Long matchId, MatchFixtureUpdateRequest updateRequest);

}
