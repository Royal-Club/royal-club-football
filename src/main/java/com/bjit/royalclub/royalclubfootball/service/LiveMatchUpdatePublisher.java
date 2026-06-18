package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.LiveMatchUpdateEvent;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class LiveMatchUpdatePublisher {

    private static final long SSE_TIMEOUT_MS = 0L;
    private static final String EVENT_NAME = "match-update";

    private final Map<Long, List<SseEmitter>> emittersByTournament = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long tournamentId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        List<SseEmitter> emitters = emittersByTournament.computeIfAbsent(
                tournamentId,
                key -> new CopyOnWriteArrayList<>()
        );
        emitters.add(emitter);

        emitter.onCompletion(() -> removeEmitter(tournamentId, emitter));
        emitter.onTimeout(() -> removeEmitter(tournamentId, emitter));
        emitter.onError(error -> removeEmitter(tournamentId, emitter));

        try {
            LiveMatchUpdateEvent initialEvent = LiveMatchUpdateEvent.builder()
                    .tournamentId(tournamentId)
                    .eventType("CONNECTED")
                    .timestamp(System.currentTimeMillis())
                    .build();
            emitter.send(SseEmitter.event()
                    .name(EVENT_NAME)
                    .data(initialEvent, MediaType.APPLICATION_JSON));
        } catch (IOException ioException) {
            removeEmitter(tournamentId, emitter);
        }

        return emitter;
    }

    public void publishMatchUpdate(Long tournamentId, Long matchId, String eventType) {
        if (tournamentId == null) {
            return;
        }

        List<SseEmitter> emitters = emittersByTournament.get(tournamentId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        LiveMatchUpdateEvent payload = LiveMatchUpdateEvent.builder()
                .tournamentId(tournamentId)
                .matchId(matchId)
                .eventType(eventType)
                .timestamp(System.currentTimeMillis())
                .build();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(EVENT_NAME)
                        .id(String.valueOf(payload.getTimestamp()))
                        .data(payload, MediaType.APPLICATION_JSON));
            } catch (IOException ioException) {
                removeEmitter(tournamentId, emitter);
            }
        }
    }

    private void removeEmitter(Long tournamentId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByTournament.get(tournamentId);
        if (emitters == null) {
            return;
        }

        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByTournament.remove(tournamentId);
        }
    }
}
