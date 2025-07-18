package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.GoalKeeperHistoryDto;
import com.bjit.royalclub.royalclubfootball.model.PlayerRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerResponse;
import com.bjit.royalclub.royalclubfootball.model.PlayerUpdateRequest;
import com.bjit.royalclub.royalclubfootball.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PreAuthorize("hasAnyRole('ADMIN')")
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

}
