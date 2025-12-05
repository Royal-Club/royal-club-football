package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.LogicNodeRequest;
import com.bjit.royalclub.royalclubfootball.model.LogicNodeResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface LogicNodeService {

    @Transactional
    LogicNodeResponse createLogicNode(LogicNodeRequest request);

    @Transactional
    LogicNodeResponse updateLogicNode(Long nodeId, LogicNodeRequest request);

    @Transactional
    void deleteLogicNode(Long nodeId);

    LogicNodeResponse getLogicNodeById(Long nodeId);

    List<LogicNodeResponse> getLogicNodesByTournament(Long tournamentId);

    @Transactional
    LogicNodeResponse executeLogicNode(Long nodeId);

    @Transactional
    void executeLogicNodesForRound(Long roundId);

    @Transactional
    void executeLogicNodesForGroup(Long groupId);
}

