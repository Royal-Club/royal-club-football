package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.GoalKeeperHistoryDto;
import com.bjit.royalclub.royalclubfootball.model.GoalKeeperQueueResponseDto;
import com.bjit.royalclub.royalclubfootball.model.PlayerPhotoUpdateRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerResponse;
import com.bjit.royalclub.royalclubfootball.model.PlayerUpdateRequest;
import com.bjit.royalclub.royalclubfootball.model.TournamentPrizeResponse;
import com.bjit.royalclub.royalclubfootball.service.TournamentPrizeService;
import com.bjit.royalclub.royalclubfootball.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.STATUS_UPDATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.UPDATE_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;
    private final TournamentPrizeService tournamentPrizeService;

    @PostMapping
    public ResponseEntity<Object> registerPlayer(@Valid @RequestBody PlayerRegistrationRequest registrationRequest) {
        playerService.registerPlayer(registrationRequest);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK);
    }

    @GetMapping
    public ResponseEntity<Object> getAllPlayers() {
        List<PlayerResponse> players = playerService.getAllPlayers();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, players);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getPlayerById(@PathVariable Long id) {
        PlayerResponse player = playerService.getPlayerById(id);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, player);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<Object> getPlayerById(@PathVariable Long id, @RequestParam boolean active) {
        playerService.updatePlayerStatus(id, active);
        return buildSuccessResponse(HttpStatus.OK, STATUS_UPDATE_OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updatePlayer(@PathVariable Long id,
                                               @Valid @RequestBody PlayerUpdateRequest updateRequest) {
        PlayerResponse playerResponse = playerService.updatePlayer(id, updateRequest);
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, playerResponse);
    }

    /**
     * Attaches an already-uploaded photo to a player. Separate from the full update so a client with
     * only a camera - the mobile app - does not have to resend a whole profile it never collected.
     * The upload itself is what the rate limit guards, at presign time.
     */
    @PatchMapping("/{id}/photo")
    public ResponseEntity<Object> updatePlayerPhoto(@PathVariable Long id,
                                                    @Valid @RequestBody PlayerPhotoUpdateRequest request) {
        PlayerResponse playerResponse = playerService.updatePhoto(id, request.getPhotoKey());
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, playerResponse);
    }

    /**
     * Everything this player has ever won, newest first — their honours board.
     * <p>
     * Lives on the player rather than under a tournament because it spans all of them. The prize
     * endpoints under {@code /tournaments/{id}/prizes} answer "who won this competition"; building a
     * career record from those would take one call per tournament the club has ever run.
     */
    @GetMapping("/{id}/prizes")
    public ResponseEntity<Object> playerPrizes(@PathVariable Long id) {
        List<TournamentPrizeResponse> prizes = tournamentPrizeService.getCareerPrizesByPlayer(id);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, prizes, prizes.size());
    }

    @GetMapping("/goal-keeper-history")
    public ResponseEntity<Object> goalKeepingHistory() {
        Map<Integer, List<GoalKeeperHistoryDto>> goalKeepingHistory = playerService.goalKeepingHistory();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, goalKeepingHistory);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/goalkeeping-history")
    public ResponseEntity<Object> getGoalKeeperHistoryForLoggedInUser() {
        List<GoalKeeperHistoryDto> goalkeepingHistory = playerService.getGoalKeeperHistoryByLoggedInUser();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, goalkeepingHistory);
    }

    @GetMapping("/goalkeeper-queue")
    public ResponseEntity<Object> getGoalKeeperPriorityQueue(@RequestParam Long tournamentId) {
        GoalKeeperQueueResponseDto response = playerService.getGoalKeeperPriorityQueue(tournamentId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, response);
    }

}
