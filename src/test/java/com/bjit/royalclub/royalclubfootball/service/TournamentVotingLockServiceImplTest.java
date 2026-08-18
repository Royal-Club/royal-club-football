package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Role;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipant;
import com.bjit.royalclub.royalclubfootball.enums.ParticipationSource;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.model.VotingLockResponse;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentParticipantRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import com.bjit.royalclub.royalclubfootball.security.UserPrinciple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.bjit.royalclub.royalclubfootball.enums.TournamentStatus.CONCLUDED;
import static com.bjit.royalclub.royalclubfootball.enums.TournamentStatus.UPCOMING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the lock as the club actually uses it: close the RSVP, stamp the silent as No, pick teams.
 *
 * <p>The cases that matter most are the ones that would quietly lose information — re-locking must
 * not restamp or steal the "locked by" name, and unlocking must only take back the rows the lock
 * itself wrote.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TournamentVotingLockServiceImplTest {

    private static final Long TOURNAMENT_ID = 3L;
    private static final Long COORDINATOR_ID = 9L;
    private static final Long MEMBER_ID = 21L;

    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private TournamentParticipantRepository participantRepository;
    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private TournamentVotingLockServiceImpl service;

    @Captor
    private ArgumentCaptor<List<TournamentParticipant>> stampedCaptor;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void lockingRecordsEverySilentPlayerAsNo() {
        signInAs("COORDINATOR");
        Tournament tournament = upcoming();
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
        when(playerRepository.findActivePlayersWithoutParticipation(TOURNAMENT_ID))
                .thenReturn(List.of(player(31L, "Silent One"), player(32L, "Silent Two")));

        VotingLockResponse response = service.lock(TOURNAMENT_ID);

        verify(participantRepository).saveAll(stampedCaptor.capture());
        assertThat(stampedCaptor.getValue())
                .hasSize(2)
                .allSatisfy(participant -> {
                    assertThat(participant.isParticipationStatus()).isFalse();
                    assertThat(participant.getParticipationSource()).isEqualTo(ParticipationSource.AUTO_LOCK);
                });
        assertThat(tournament.isVotingLocked()).isTrue();
        assertThat(tournament.getVotingLockedBy()).isEqualTo(COORDINATOR_ID);
        assertThat(response.getAutoMarkedCount()).isEqualTo(2);
    }

    @Test
    void lockingTwiceNeitherRestampsNorReassignsTheContact() {
        signInAs("ADMIN");
        Tournament tournament = upcoming();
        tournament.setVotingLocked(true);
        tournament.setVotingLockedBy(COORDINATOR_ID);
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

        VotingLockResponse response = service.lock(TOURNAMENT_ID);

        verify(participantRepository, never()).saveAll(any());
        assertThat(tournament.getVotingLockedBy()).isEqualTo(COORDINATOR_ID);
        assertThat(response.getAutoMarkedCount()).isZero();
    }

    @Test
    void aConcludedTournamentCannotBeLocked() {
        signInAs("COORDINATOR");
        Tournament tournament = upcoming();
        tournament.setTournamentStatus(CONCLUDED);
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() -> service.lock(TOURNAMENT_ID))
                .isInstanceOf(TournamentServiceException.class);
        verify(participantRepository, never()).saveAll(any());
    }

    @Test
    void unlockingTakesBackOnlyTheRowsTheLockWrote() {
        signInAs("COORDINATOR");
        Tournament tournament = upcoming();
        tournament.setVotingLocked(true);
        tournament.setVotingLockedBy(COORDINATOR_ID);
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
        when(participantRepository.deleteByTournamentIdAndSource(TOURNAMENT_ID, ParticipationSource.AUTO_LOCK))
                .thenReturn(4);

        VotingLockResponse response = service.unlock(TOURNAMENT_ID);

        verify(participantRepository).deleteByTournamentIdAndSource(TOURNAMENT_ID, ParticipationSource.AUTO_LOCK);
        assertThat(tournament.isVotingLocked()).isFalse();
        assertThat(tournament.getVotingLockedBy()).isNull();
        assertThat(response.getAutoMarkedCount()).isEqualTo(4);
    }

    @Test
    void anOrdinaryMemberIsTurnedAwayAndToldWhoLockedIt() {
        signInAs("PLAYER");
        Tournament tournament = upcoming();
        tournament.setVotingLocked(true);
        tournament.setVotingLockedBy(COORDINATOR_ID);
        when(playerRepository.findById(COORDINATOR_ID))
                .thenReturn(Optional.of(player(COORDINATOR_ID, "Rakib")));

        assertThatThrownBy(() -> service.requireVotingOpen(tournament))
                .isInstanceOf(TournamentServiceException.class)
                .hasMessageContaining("Rakib");
    }

    @Test
    void aManagerKeepsEditingAfterTheLock() {
        signInAs("COORDINATOR");
        Tournament tournament = upcoming();
        tournament.setVotingLocked(true);
        tournament.setVotingLockedBy(COORDINATOR_ID);

        service.requireVotingOpen(tournament);
    }

    @Test
    void anOpenTournamentLetsEveryoneThrough() {
        signInAs("PLAYER");

        service.requireVotingOpen(upcoming());
    }

    private Tournament upcoming() {
        return Tournament.builder()
                .id(TOURNAMENT_ID)
                .name("Friday Futsal")
                .tournamentStatus(UPCOMING)
                .isActive(true)
                .build();
    }

    private Player player(Long id, String name) {
        return Player.builder().id(id).name(name).build();
    }

    /** Signs in the member the lock acts as, carrying the role under test. */
    private void signInAs(String roleName) {
        Player player = Player.builder()
                .id("PLAYER".equals(roleName) ? MEMBER_ID : COORDINATOR_ID)
                .name("Signed In")
                .roles(Set.of(Role.builder().name(roleName).build()))
                .build();
        UserPrinciple principle = UserPrinciple.builder()
                .id(player.getId())
                .username(player.getName())
                .player(player)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + roleName)))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principle, null, principle.getAuthorities()));
    }
}
