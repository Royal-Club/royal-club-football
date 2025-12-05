package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.*;
import com.bjit.royalclub.royalclubfootball.service.RoundGroupService;
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
@RequestMapping("groups")
public class RoundGroupController {

    private final RoundGroupService roundGroupService;

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<Object> createGroup(@Valid @RequestBody RoundGroupRequest request) {
        RoundGroupResponse response = roundGroupService.createGroup(request);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK, response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{groupId}")
    public ResponseEntity<Object> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody RoundGroupRequest request) {
        RoundGroupResponse response = roundGroupService.updateGroup(groupId, request);
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Object> deleteGroup(@PathVariable Long groupId) {
        roundGroupService.deleteGroup(groupId);
        return buildSuccessResponse(HttpStatus.OK, "Group deleted successfully");
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<Object> getGroupById(@PathVariable Long groupId) {
        RoundGroupResponse response = roundGroupService.getGroupById(groupId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, response);
    }

    @GetMapping("/round/{roundId}")
    public ResponseEntity<Object> getGroupsByRoundId(@PathVariable Long roundId) {
        List<RoundGroupResponse> responses = roundGroupService.getGroupsByRoundId(roundId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, responses);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{groupId}/teams")
    public ResponseEntity<Object> assignTeamsToGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody TeamAssignmentRequest request) {
        roundGroupService.assignTeamsToGroup(groupId, request);
        return buildSuccessResponse(HttpStatus.OK, "Teams assigned successfully to group");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/placeholder")
    public ResponseEntity<Object> createPlaceholderTeam(@Valid @RequestBody PlaceholderTeamRequest request) {
        roundGroupService.createPlaceholderTeam(request);
        return buildSuccessResponse(HttpStatus.CREATED, "Placeholder team created successfully");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{groupId}/teams/{teamId}")
    public ResponseEntity<Object> removeTeamFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long teamId) {
        roundGroupService.removeTeamFromGroup(groupId, teamId);
        return buildSuccessResponse(HttpStatus.OK, "Team removed successfully from group");
    }

    @GetMapping("/{groupId}/standings")
    public ResponseEntity<Object> getGroupStandings(@PathVariable Long groupId) {
        List<GroupStandingResponse> responses = roundGroupService.getGroupStandings(groupId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, responses);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{groupId}/standings/recalculate")
    public ResponseEntity<Object> recalculateGroupStandings(@PathVariable Long groupId) {
        roundGroupService.recalculateGroupStandings(groupId);
        return buildSuccessResponse(HttpStatus.OK, "Group standings recalculated successfully");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{groupId}/generate-matches")
    public ResponseEntity<Object> generateGroupMatches(
            @PathVariable Long groupId,
            @Valid @RequestBody GroupMatchGenerationRequest request) {
        List<MatchResponse> matches = roundGroupService.generateGroupMatches(groupId, request);
        return buildSuccessResponse(HttpStatus.CREATED,
                "Generated " + matches.size() + " matches successfully",
                matches);
    }

    @GetMapping("/{groupId}/matches")
    public ResponseEntity<Object> getGroupMatches(@PathVariable Long groupId) {
        List<MatchResponse> matches = roundGroupService.getGroupMatches(groupId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, matches);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{groupId}/matches")
    public ResponseEntity<Object> clearGroupMatches(@PathVariable Long groupId) {
        roundGroupService.clearGroupMatches(groupId);
        return buildSuccessResponse(HttpStatus.OK, "Group matches cleared successfully");
    }
}
