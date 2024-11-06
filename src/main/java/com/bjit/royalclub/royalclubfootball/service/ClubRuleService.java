package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.ClubRuleRequest;
import com.bjit.royalclub.royalclubfootball.model.ClubRuleResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface ClubRuleService {

    List<ClubRuleResponse> rules();

    @Transactional
    void save(ClubRuleRequest clubRuleRequest);

    @Transactional
    ClubRuleResponse updateClubRule(Long clubRuleId, ClubRuleRequest clubRuleRequest);

    ClubRuleResponse getById(Long clubRuleId);
}
