package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.ClubRuleRequest;
import com.bjit.royalclub.royalclubfootball.model.ClubRuleResponse;
import com.bjit.royalclub.royalclubfootball.service.ClubRuleService;
import com.bjit.royalclub.royalclubfootball.util.ResponseBuilder;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.UPDATE_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequestMapping("club-rules")
@RequiredArgsConstructor
public class ClubRuleController {

    private final ClubRuleService clubRuleService;

    @GetMapping
    public ResponseEntity<Object> clubRules() {
        List<ClubRuleResponse> clubRuleResponses = clubRuleService.rules();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, clubRuleResponses);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Object> saveClubRule(@Valid @RequestBody ClubRuleRequest clubRuleRequest) {
        clubRuleService.save(clubRuleRequest);
        return ResponseBuilder.buildSuccessResponse(HttpStatus.CREATED, CREATE_OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateClubRule(@PathVariable Long id,
                                                 @Valid @RequestBody ClubRuleRequest clubRuleRequest) {
        ClubRuleResponse updateClubRule = clubRuleService.updateClubRule(id, clubRuleRequest);
        return ResponseBuilder.buildSuccessResponse(HttpStatus.OK, UPDATE_OK, updateClubRule);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Object> getClubRule(@PathVariable Long id) {
        ClubRuleResponse clubRuleResponse = clubRuleService.getById(id);
        return ResponseBuilder.buildSuccessResponse(HttpStatus.OK, UPDATE_OK, clubRuleResponse);
    }
}
