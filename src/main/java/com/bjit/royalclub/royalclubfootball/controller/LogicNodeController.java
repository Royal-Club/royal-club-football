package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.LogicNodeRequest;
import com.bjit.royalclub.royalclubfootball.model.LogicNodeResponse;
import com.bjit.royalclub.royalclubfootball.service.LogicNodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.*;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("logic-nodes")
public class LogicNodeController {

    private final LogicNodeService logicNodeService;

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<Object> createLogicNode(@Valid @RequestBody LogicNodeRequest request) {
        LogicNodeResponse response = logicNodeService.createLogicNode(request);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK, response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{nodeId}")
    public ResponseEntity<Object> updateLogicNode(
            @PathVariable Long nodeId,
            @Valid @RequestBody LogicNodeRequest request) {
        LogicNodeResponse response = logicNodeService.updateLogicNode(nodeId, request);
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{nodeId}")
    public ResponseEntity<Object> deleteLogicNode(@PathVariable Long nodeId) {
        logicNodeService.deleteLogicNode(nodeId);
        return buildSuccessResponse(HttpStatus.OK, "Logic node deleted successfully");
    }

    @GetMapping("/{nodeId}")
    public ResponseEntity<Object> getLogicNodeById(@PathVariable Long nodeId) {
        LogicNodeResponse response = logicNodeService.getLogicNodeById(nodeId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, response);
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<Object> getLogicNodesByTournament(@PathVariable Long tournamentId) {
        List<LogicNodeResponse> responses = logicNodeService.getLogicNodesByTournament(tournamentId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, responses);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{nodeId}/execute")
    public ResponseEntity<Object> executeLogicNode(@PathVariable Long nodeId) {
        LogicNodeResponse response = logicNodeService.executeLogicNode(nodeId);
        return buildSuccessResponse(HttpStatus.OK, "Logic node executed successfully", response);
    }
}

