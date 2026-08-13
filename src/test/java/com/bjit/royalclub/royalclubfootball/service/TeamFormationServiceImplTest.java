package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.model.TeamFormationResponse;
import com.bjit.royalclub.royalclubfootball.repository.MatchRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamFormationRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamPlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import com.bjit.royalclub.royalclubfootball.security.UserPrinciple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Covers "which team am I on", the lookup behind the members' apps.
 *
 * <p>The two empty cases matter as much as the happy one: not being on a team sheet is
 * an ordinary answer, and if it ever started throwing, every member without a team
 * would see a broken screen instead of no screen.
 */
@ExtendWith(MockitoExtension.class)
class TeamFormationServiceImplTest {

    private static final Long TOURNAMENT_ID = 3L;
    private static final Long PLAYER_ID = 7L;
    private static final Long TEAM_ID = 11L;

    @Mock
    private TeamFormationRepository teamFormationRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamPlayerRepository teamPlayerRepository;
    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private TeamFormationServiceImpl service;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void anonymousCallerIsOnNoTeam() {
        assertThat(service.getMyFormation(TOURNAMENT_ID)).isEmpty();
        verifyNoInteractions(teamPlayerRepository);
    }

    @Test
    void memberLeftOffEveryTeamSheetIsOnNoTeam() {
        signIn();
        when(teamPlayerRepository.findTeamIdsByTournamentIdAndPlayerId(TOURNAMENT_ID, PLAYER_ID))
                .thenReturn(List.of());

        assertThat(service.getMyFormation(TOURNAMENT_ID)).isEmpty();
        verifyNoInteractions(teamRepository);
    }

    @Test
    void returnsThePresetShapeWhenTheCaptainHasSavedNothing() {
        signIn();
        when(teamPlayerRepository.findTeamIdsByTournamentIdAndPlayerId(TOURNAMENT_ID, PLAYER_ID))
                .thenReturn(List.of(TEAM_ID));
        when(teamRepository.findById(TEAM_ID))
                .thenReturn(Optional.of(Team.builder().id(TEAM_ID).teamName("Falcons").build()));
        when(teamFormationRepository.findDefaultByTeamId(TEAM_ID)).thenReturn(Optional.empty());
        when(teamPlayerRepository.findAllByTeamId(TEAM_ID)).thenReturn(List.of());
        when(teamPlayerRepository.findByTeamIdAndPlayerId(TEAM_ID, PLAYER_ID)).thenReturn(Optional.empty());

        Optional<TeamFormationResponse> result = service.getMyFormation(TOURNAMENT_ID);

        assertThat(result).isPresent();
        TeamFormationResponse formation = result.orElseThrow();
        assertThat(formation.getTeamId()).isEqualTo(TEAM_ID);
        assertThat(formation.getTeamName()).isEqualTo("Falcons");
        // The client shows a squad list rather than an empty pitch when this is false.
        assertThat(formation.getSaved()).isFalse();
        assertThat(formation.getSlots()).isNotEmpty();
        // A plain member cannot edit, so the app has no reason to offer it.
        assertThat(formation.getEditable()).isFalse();
    }

    /** Signs in an ordinary member — no manager role, so the captain check decides editability. */
    private void signIn() {
        UserPrinciple principle = UserPrinciple.builder()
                .id(PLAYER_ID)
                .username("member")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_PLAYER")))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principle, null, principle.getAuthorities()));
    }
}
