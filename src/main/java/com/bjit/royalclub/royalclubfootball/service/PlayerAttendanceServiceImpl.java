package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.model.PlayerAttendanceResponse;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamPlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentParticipantRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds the club attendance report.
 * <p>
 * The three inputs — the tournaments that were held, the RSVPs players gave,
 * and the team sheets they ended up on — are each fetched once and combined in
 * memory. Streaks need the tournaments in date order per player, which is
 * awkward to express in SQL and cheap to do here: a club season is hundreds of
 * tournaments against tens of players.
 */
@Service
@RequiredArgsConstructor
public class PlayerAttendanceServiceImpl implements PlayerAttendanceService {

    private final TournamentRepository tournamentRepository;
    private final TournamentParticipantRepository tournamentParticipantRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final PlayerRepository playerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PlayerAttendanceResponse> getAttendanceReport(Integer year, boolean activeOnly) {
        LocalDateTime now = LocalDateTime.now();
        List<Tournament> tournaments = year == null
                ? tournamentRepository.findHeldTournaments(now)
                : tournamentRepository.findHeldTournamentsByYear(now, year);

        List<Player> players = playerRepository.findAll().stream()
                .filter(player -> !activeOnly || player.isActive())
                .toList();

        if (tournaments.isEmpty() || players.isEmpty()) {
            return List.of();
        }

        List<Long> tournamentIds = tournaments.stream().map(Tournament::getId).toList();

        // playerId -> tournamentId -> answered yes?
        Map<Long, Map<Long, Boolean>> responsesByPlayer = new HashMap<>();
        tournamentParticipantRepository.findResponsesByTournamentIds(tournamentIds)
                .forEach(row -> responsesByPlayer
                        .computeIfAbsent(toLong(row[0]), key -> new HashMap<>())
                        .put(toLong(row[1]), Boolean.TRUE.equals(row[2])));

        // playerId -> tournaments they were named in a team for
        Map<Long, Set<Long>> playedByPlayer = new HashMap<>();
        teamPlayerRepository.findPlayerTournamentPairs(tournamentIds)
                .forEach(row -> playedByPlayer
                        .computeIfAbsent(toLong(row[0]), key -> new HashSet<>())
                        .add(toLong(row[1])));

        List<PlayerAttendanceResponse> report = new ArrayList<>();
        players.forEach(player -> {
            PlayerAttendanceResponse row = buildRow(
                    player,
                    tournaments,
                    responsesByPlayer.getOrDefault(player.getId(), Map.of()),
                    playedByPlayer.getOrDefault(player.getId(), Set.of()));
            if (row != null) {
                report.add(row);
            }
        });

        Comparator<PlayerAttendanceResponse> byAttendance =
                Comparator.comparingDouble(PlayerAttendanceResponse::getAttendanceRate);
        report.sort(byAttendance.reversed()
                .thenComparing(PlayerAttendanceResponse::getPlayed, Comparator.reverseOrder()));
        return report;
    }

    /**
     * Scores one player against the tournament timeline. Only tournaments held
     * after the player joined count, so someone who signed up last month isn't
     * marked absent for the club's whole history. Returns null when nothing in
     * the window applies to them.
     */
    private PlayerAttendanceResponse buildRow(Player player,
                                              List<Tournament> tournaments,
                                              Map<Long, Boolean> responses,
                                              Set<Long> played) {
        LocalDateTime joinedAt = player.getCreatedDate();

        int eligible = 0;
        int confirmed = 0;
        int declined = 0;
        int playedCount = 0;
        int currentStreak = 0;
        int longestStreak = 0;
        int runningStreak = 0;
        int currentAbsenceStreak = 0;
        boolean tailResolved = false;
        LocalDateTime lastPlayedDate = null;
        LocalDateTime firstCountedDate = null;

        // Oldest first, so the running streak reads chronologically.
        for (Tournament tournament : tournaments) {
            boolean involved = responses.containsKey(tournament.getId())
                    || played.contains(tournament.getId());

            // A tournament counts once the player has joined the club. Records
            // that predate the audit column still count if the player took part.
            boolean joinedBefore = joinedAt == null
                    || !tournament.getTournamentDate().isBefore(joinedAt);
            if (!joinedBefore && !involved) {
                continue;
            }

            eligible++;
            if (firstCountedDate == null) {
                firstCountedDate = tournament.getTournamentDate();
            }

            Boolean response = responses.get(tournament.getId());
            if (Boolean.TRUE.equals(response)) {
                confirmed++;
            } else if (Boolean.FALSE.equals(response)) {
                declined++;
            }

            if (played.contains(tournament.getId())) {
                playedCount++;
                runningStreak++;
                longestStreak = Math.max(longestStreak, runningStreak);
                lastPlayedDate = tournament.getTournamentDate();
            } else {
                runningStreak = 0;
            }
        }

        if (eligible == 0) {
            return null;
        }

        // Walk back from the most recent tournament for the live streaks.
        for (int index = tournaments.size() - 1; index >= 0 && !tailResolved; index--) {
            Tournament tournament = tournaments.get(index);
            boolean involved = responses.containsKey(tournament.getId())
                    || played.contains(tournament.getId());
            boolean joinedBefore = joinedAt == null
                    || !tournament.getTournamentDate().isBefore(joinedAt);
            if (!joinedBefore && !involved) {
                continue;
            }

            if (played.contains(tournament.getId())) {
                if (currentAbsenceStreak > 0) {
                    tailResolved = true;
                } else {
                    currentStreak++;
                }
            } else {
                if (currentStreak > 0) {
                    tailResolved = true;
                } else {
                    currentAbsenceStreak++;
                }
            }
        }

        int noResponse = Math.max(0, eligible - confirmed - declined);
        int confirmedButNotPlayed = Math.max(0, confirmed - playedCount);

        return PlayerAttendanceResponse.builder()
                .playerId(player.getId())
                .playerName(player.getName())
                .position(player.getPosition() == null ? null : player.getPosition().name())
                .active(player.isActive())
                .eligibleTournaments(eligible)
                .confirmed(confirmed)
                .declined(declined)
                .noResponse(noResponse)
                .played(playedCount)
                .confirmedButNotPlayed(confirmedButNotPlayed)
                .attendanceRate(percentage(playedCount, eligible))
                .responseRate(percentage(confirmed + declined, eligible))
                // Playing without an RSVP on file can push this past 100, which
                // reads as a data oddity rather than a score — cap it.
                .reliabilityRate(confirmed == 0 ? 0d
                        : Math.min(100d, percentage(playedCount, confirmed)))
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .currentAbsenceStreak(currentAbsenceStreak)
                .lastPlayedDate(lastPlayedDate)
                .firstCountedDate(firstCountedDate)
                .build();
    }

    private static double percentage(int part, int total) {
        return total == 0 ? 0d : Math.round((part * 10000d) / total) / 100d;
    }

    private static Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }
}
