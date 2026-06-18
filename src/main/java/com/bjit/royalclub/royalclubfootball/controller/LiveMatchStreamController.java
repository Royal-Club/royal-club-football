package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.service.LiveMatchUpdatePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/matches/live")
@RequiredArgsConstructor
public class LiveMatchStreamController {

    private final LiveMatchUpdatePublisher liveMatchUpdatePublisher;

    @GetMapping(value = "/tournaments/{tournamentId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTournamentMatchUpdates(@PathVariable Long tournamentId) {
        return liveMatchUpdatePublisher.subscribe(tournamentId);
    }
}
