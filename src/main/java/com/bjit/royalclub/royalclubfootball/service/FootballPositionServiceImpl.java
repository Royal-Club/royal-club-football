package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.enums.FootballPosition;
import com.bjit.royalclub.royalclubfootball.model.FootballPositionResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FootballPositionServiceImpl implements FootballPositionService {

    @Override
    public List<FootballPositionResponse> getAllPositions() {
        return Arrays.stream(FootballPosition.values())
                .map(position -> new FootballPositionResponse(position.name(), position.getDescription()))
                .collect(Collectors.toList());
    }
}
