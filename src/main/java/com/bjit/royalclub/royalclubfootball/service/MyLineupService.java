package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.MyLineupResponse;

import java.util.Optional;

public interface MyLineupService {

    /**
     * The signed-in player's own line-up for a tournament.
     *
     * @return empty whenever there is nothing worth showing - the caller is on no team in this
     * tournament, the captain has not saved a formation, or the saved formation has nobody placed
     * in it. Callers answer that with 204 rather than an error: none of these are failures, they
     * are simply "not announced yet".
     */
    Optional<MyLineupResponse> getMyLineup(Long tournamentId);
}
