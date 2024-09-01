package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.PlayerRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerResponse;
import com.bjit.royalclub.royalclubfootball.model.PlayerUpdateRequest;
import com.bjit.royalclub.royalclubfootball.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ResponseEntity<Object> getPlayers() {
        List<PlayerResponse> players = playerService.getAllPlayers();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, players);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getPlayerById(@PathVariable Long id) {
        PlayerResponse player = playerService.getPlayerById(id);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, player);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Object> getPlayerById(@PathVariable Long id, @RequestParam boolean status) {
        playerService.updatePlayerStatus(id, status);
        return buildSuccessResponse(HttpStatus.OK, STATUS_UPDATE_OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updatePlayer(@PathVariable Long id, @Valid @RequestBody PlayerUpdateRequest updateRequest) {
        PlayerResponse playerResponse = playerService.updatePlayer(id, updateRequest);
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, playerResponse);
    }
}
