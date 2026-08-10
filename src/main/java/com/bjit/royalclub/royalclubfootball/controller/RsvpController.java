package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.exception.RsvpTokenException;
import com.bjit.royalclub.royalclubfootball.model.RsvpVoteResponse;
import com.bjit.royalclub.royalclubfootball.service.RsvpVoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Supplier;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

/**
 * Public endpoints behind the Yes/No links in RSVP emails. Unauthenticated by design - the signed
 * token is the credential.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("rsvp")
public class RsvpController {

    private final RsvpVoteService rsvpVoteService;

    /**
     * Read-only: describes what the link would do so the page can ask for confirmation.
     * Deliberately has no side effect, because mail scanners follow links in transit.
     */
    @GetMapping("/preview")
    public ResponseEntity<Object> preview(@RequestParam String token) {
        return respond(() -> rsvpVoteService.preview(token));
    }

    /**
     * Records the vote. POST, so it can only be reached by a real click on the confirm button.
     */
    @PostMapping("/vote")
    public ResponseEntity<Object> vote(@RequestParam String token) {
        return respond(() -> rsvpVoteService.vote(token));
    }

    /**
     * A bad or expired link is a normal outcome of this flow, not a server error: it is answered with
     * a status the page can render rather than an error the browser has to interpret.
     */
    private ResponseEntity<Object> respond(Supplier<RsvpVoteResponse> action) {
        try {
            return buildSuccessResponse(HttpStatus.OK, FETCH_OK, action.get());
        } catch (RsvpTokenException e) {
            return buildSuccessResponse(HttpStatus.OK, FETCH_OK, RsvpVoteResponse.builder()
                    .status(e.getStatus())
                    .message(e.getMessage())
                    .build());
        }
    }
}
