package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.LineupNotificationLog;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.TeamFormation;
import com.bjit.royalclub.royalclubfootball.entity.TeamFormationSlot;
import com.bjit.royalclub.royalclubfootball.entity.TeamPlayer;
import com.bjit.royalclub.royalclubfootball.enums.PositionGroup;
import com.bjit.royalclub.royalclubfootball.model.LineupPublishResponse;
import com.bjit.royalclub.royalclubfootball.repository.LineupNotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the rules that decide who hears about a line-up, and how often.
 * <p>
 * The failure these guard against is not a crash: it is a captain arranging a team and buzzing
 * thirty phones each time they save, or a replacement player never being told they are playing.
 * Both look fine in code review and are only visible in someone's notification tray.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LineupPublishedNotifierTest {

    private static final long FORMATION_ID = 100L;

    @Mock
    private NotificationService notificationService;
    @Mock
    private LineupNotificationLogRepository notificationLogRepository;

    @InjectMocks
    private LineupPublishedNotifier notifier;

    private Team team;
    /** Stands in for the table's unique key, so a second publish sees what the first wrote. */
    private Set<Long> notifiedIds;

    @BeforeEach
    void setUp() {
        team = new Team();
        team.setId(7L);
        team.setTeamName("Royal Reds");

        notifiedIds = new HashSet<>();
        when(notificationLogRepository.findNotifiedPlayerIds(FORMATION_ID))
                .thenAnswer(invocation -> Set.copyOf(notifiedIds));
        when(notificationLogRepository.save(any(LineupNotificationLog.class)))
                .thenAnswer(invocation -> {
                    LineupNotificationLog saved = invocation.getArgument(0);
                    notifiedIds.add(saved.getPlayer().getId());
                    return saved;
                });
        // Every send reaches its player unless a test says otherwise.
        when(notificationService.sendToPlayers(any(), anyString(), anyString(), anyMap()))
                .thenAnswer(invocation -> {
                    List<Player> players = invocation.getArgument(0);
                    return players.stream().map(Player::getId).collect(java.util.stream.Collectors.toSet());
                });
    }

    @Test
    void savingDoesNotNotifyAnyone() {
        // Saving never calls the notifier at all - the only entry points are publish() and status().
        // status() is what a board polls after a save, and it must stay silent.
        TeamFormation formation = formationWith(player(1L, "Rakib"), player(2L, "Sumon"));

        LineupPublishResponse status = notifier.status(formation);

        verify(notificationService, never()).sendToPlayers(any(), anyString(), anyString(), anyMap());
        assertThat(status.isPublished()).isFalse();
        assertThat(status.getPendingPlayers()).isEqualTo(2);
    }

    @Test
    void firstPublishNotifiesEveryPlacedPlayer() {
        TeamFormation formation = formationWith(player(1L, "Rakib"), player(2L, "Sumon"));

        LineupPublishResponse result = notifier.publish(formation, team);

        verify(notificationService, times(2)).sendToPlayers(any(), anyString(), anyString(), anyMap());
        assertThat(result.getNotifiedNow()).isEqualTo(2);
        assertThat(result.getPendingPlayers()).isZero();
        assertThat(result.isPublished()).isTrue();
    }

    @Test
    void republishingNotifiesNobodyAgain() {
        TeamFormation formation = formationWith(player(1L, "Rakib"), player(2L, "Sumon"));
        notifier.publish(formation, team);

        LineupPublishResponse second = notifier.publish(formation, team);

        // Two sends in total, from the first publish only - the button is safe to press twice.
        verify(notificationService, times(2)).sendToPlayers(any(), anyString(), anyString(), anyMap());
        assertThat(second.getNotifiedNow()).isZero();
        assertThat(second.getNotifiedPlayers()).isEqualTo(2);
    }

    @Test
    void onlyTheReplacementIsNotifiedAfterASwap() {
        Player kept = player(1L, "Rakib");
        TeamFormation formation = formationWith(kept, player(2L, "Sumon"));
        notifier.publish(formation, team);

        // Sumon drops out, Tahid comes in.
        Player replacement = player(3L, "Tahid");
        formation.setSlots(slotsFor(formation, kept, replacement));

        LineupPublishResponse afterSwap = notifier.publish(formation, team);

        assertThat(afterSwap.getNotifiedNow()).isEqualTo(1);
        assertThat(lastNotifiedPlayer().getId()).isEqualTo(3L);
    }

    @Test
    void aRemovedPlayerIsNotNotifiedAgainIfTheyReturn() {
        Player kept = player(1L, "Rakib");
        Player dropped = player(2L, "Sumon");
        TeamFormation formation = formationWith(kept, dropped);
        notifier.publish(formation, team);

        // Dropped from the line-up, then put back before the next publish.
        formation.setSlots(slotsFor(formation, kept));
        notifier.publish(formation, team);
        formation.setSlots(slotsFor(formation, kept, dropped));

        LineupPublishResponse afterReturn = notifier.publish(formation, team);

        // Their row survives their removal, so returning does not earn a second buzz.
        assertThat(afterReturn.getNotifiedNow()).isZero();
        verify(notificationService, times(2)).sendToPlayers(any(), anyString(), anyString(), anyMap());
    }

    @Test
    void anEmptyLineupPublishesNothing() {
        TeamFormation formation = formationWith();

        LineupPublishResponse result = notifier.publish(formation, team);

        verify(notificationService, never()).sendToPlayers(any(), anyString(), anyString(), anyMap());
        assertThat(result.getNotifiedNow()).isZero();
    }

    @Test
    void theMessageNamesThePlayersOwnPosition() {
        TeamFormation formation = formationWith(player(1L, "Rakib"));
        formation.getSlots().get(0).setPositionGroup(PositionGroup.MID);

        notifier.publish(formation, team);

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(notificationService).sendToPlayers(any(), anyString(), body.capture(), anyMap());
        assertThat(body.getValue()).isEqualTo("You're starting at Midfield for Royal Reds. Tap to see it.");
    }

    @Test
    void anUnreachableDeviceStillCountsAsAnnounced() {
        // FCM accepted nothing - the player has no registered device. Re-stubbed with doReturn
        // because when() would re-invoke the existing answer with null arguments while setting up.
        org.mockito.Mockito.doReturn(Set.of())
                .when(notificationService).sendToPlayers(any(), anyString(), anyString(), anyMap());
        TeamFormation formation = formationWith(player(1L, "Rakib"));

        notifier.publish(formation, team);
        LineupPublishResponse second = notifier.publish(formation, team);

        // The ledger records that they were announced to, not that a handset lit up. Otherwise a
        // player without the app would be re-notified on every publish for the rest of the season.
        assertThat(second.getNotifiedNow()).isZero();
    }

    @Test
    void thePayloadCarriesWhatTheAppNeedsToDeepLink() {
        TeamFormation formation = formationWith(player(1L, "Rakib"));

        notifier.publish(formation, team);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> data = ArgumentCaptor.forClass(Map.class);
        verify(notificationService).sendToPlayers(any(), anyString(), anyString(), data.capture());
        assertThat(data.getValue()).containsEntry("type", "LINEUP_PUBLISHED");
        assertThat(data.getValue()).containsEntry("teamId", "7");
    }

    private Player lastNotifiedPlayer() {
        ArgumentCaptor<List<Player>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationService, org.mockito.Mockito.atLeastOnce())
                .sendToPlayers(captor.capture(), anyString(), anyString(), anyMap());
        List<List<Player>> all = captor.getAllValues();
        return all.get(all.size() - 1).get(0);
    }

    private Player player(Long id, String name) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        return player;
    }

    private TeamFormation formationWith(Player... players) {
        TeamFormation formation = new TeamFormation();
        formation.setId(FORMATION_ID);
        formation.setTeam(team);
        formation.setSlots(slotsFor(formation, players));
        return formation;
    }

    private List<TeamFormationSlot> slotsFor(TeamFormation formation, Player... players) {
        List<TeamFormationSlot> slots = new ArrayList<>();
        int index = 0;
        for (Player player : players) {
            TeamPlayer teamPlayer = new TeamPlayer();
            teamPlayer.setId(player.getId());
            teamPlayer.setPlayer(player);
            teamPlayer.setTeam(team);

            TeamFormationSlot slot = new TeamFormationSlot();
            slot.setFormation(formation);
            slot.setTeamPlayer(teamPlayer);
            slot.setSlotIndex(index++);
            slot.setPositionGroup(PositionGroup.MID);
            slot.setSlotLabel("MID");
            slots.add(slot);
        }
        return slots;
    }
}
