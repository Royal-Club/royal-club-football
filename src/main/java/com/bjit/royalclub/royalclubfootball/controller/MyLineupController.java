package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.MyLineupResponse;
import com.bjit.royalclub.royalclubfootball.service.MyLineupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequestMapping("/tournaments/{tournamentId}")
@RequiredArgsConstructor
public class MyLineupController {

    private final MyLineupService myLineupService;

    /**
     * The signed-in player's own team and line-up for this tournament.
     * <p>
     * Requires authentication by definition - the answer depends on who is asking, and the caller is
     * resolved from the token rather than from a client-supplied id. That also keeps this off the
     * public {@code /tournaments/details} route, which hands out every team and every player name to
     * anyone at all.
     *
     * @return 204 when there is nothing to show: no team, not on one, or no line-up published yet.
     * Not an error - "not announced yet" is the normal state for most of the week.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my-lineup")
    public ResponseEntity<Object> myLineup(@PathVariable Long tournamentId) {
        Optional<MyLineupResponse> lineup = myLineupService.getMyLineup(tournamentId);
        return lineup
                .<ResponseEntity<Object>>map(response -> buildSuccessResponse(HttpStatus.OK, FETCH_OK, response))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
