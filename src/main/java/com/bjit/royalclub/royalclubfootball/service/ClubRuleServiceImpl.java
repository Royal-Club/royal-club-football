package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.ClubRule;
import com.bjit.royalclub.royalclubfootball.exception.BadRequestException;
import com.bjit.royalclub.royalclubfootball.model.ClubRuleRequest;
import com.bjit.royalclub.royalclubfootball.model.ClubRuleResponse;
import com.bjit.royalclub.royalclubfootball.repository.ClubRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.CLUB_RULE_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ClubRuleServiceImpl implements ClubRuleService {

    private final ClubRuleRepository clubRuleRepository;

    @Override
    public List<ClubRuleResponse> rules() {
        return clubRuleRepository.findAll()
                .stream().map(this::convertToClubRuleDto)
                .toList();
    }

    @Override
    public void save(ClubRuleRequest clubRuleRequest) {
        ClubRule clubRule = ClubRule.builder()
                .description(clubRuleRequest.getDescription())
                .build();
        clubRuleRepository.save(clubRule);
    }

    @Override
    public ClubRuleResponse updateClubRule(Long clubRuleId, ClubRuleRequest clubRuleRequest) {
        ClubRule clubRule = getClubRuleById(clubRuleId);
        clubRule.setDescription(clubRuleRequest.getDescription());
        ClubRule updatedClubRule = clubRuleRepository.save(clubRule);
        return convertToClubRuleDto(updatedClubRule);
    }

    @Override
    public ClubRuleResponse getById(Long clubRuleId) {
        ClubRule clubRule = getClubRuleById(clubRuleId);
        return convertToClubRuleDto(clubRule);

    }

    private ClubRule getClubRuleById(Long clubRuleId) {
        return clubRuleRepository.findById(clubRuleId)
                .orElseThrow(() -> new BadRequestException(CLUB_RULE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private ClubRuleResponse convertToClubRuleDto(ClubRule clubRule) {
        return ClubRuleResponse.builder()
                .id(clubRule.getId())
                .description(clubRule.getDescription())
                .build();
    }
}
