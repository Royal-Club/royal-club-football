package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.LiveMatchUpdateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LiveMatchUpdatePublisher {

    // Finite timeout so abandoned connections are released by the container
    // instead of being held forever (previously 0L = never times out).
    private static final long SSE_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(30);
    private static final String EVENT_NAME = "match-update";
    private static final String HEARTBEAT_EVENT_NAME = "heartbeat";

    private final Map<Long, List<SseEmitter>> emittersByTournament = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long tournamentId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        List<SseEmitter> emitters = emittersByTournament.computeIfAbsent(
                tournamentId,
                key -> new CopyOnWriteArrayList<>()
        );
        emitters.add(emitter);

        emitter.onCompletion(() -> removeEmitter(tournamentId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            removeEmitter(tournamentId, emitter);
        });
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
            } catch (IOException | IllegalStateException ex) {
                removeEmitter(tournamentId, emitter);
            }
        }
    }

    /**
     * Periodically pings every open connection. This detects clients that have
     * disconnected without a clean close (mobile drops, refreshes, proxies) so
     * their emitters can be pruned instead of accumulating in memory and tying
     * up async request threads. Without active match traffic there is otherwise
     * no signal that would trigger cleanup.
     */
    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void sendHeartbeat() {
        if (emittersByTournament.isEmpty()) {
            return;
        }

        emittersByTournament.forEach((tournamentId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(HEARTBEAT_EVENT_NAME)
                            .comment("ping"));
                } catch (IOException | IllegalStateException ex) {
                    removeEmitter(tournamentId, emitter);
                }
            }
        });
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
