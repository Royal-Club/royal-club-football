package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.*;
import com.bjit.royalclub.royalclubfootball.enums.LogicNodeType;
import com.bjit.royalclub.royalclubfootball.exception.RoundServiceException;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.model.LogicNodeRequest;
import com.bjit.royalclub.royalclubfootball.model.LogicNodeResponse;
import com.bjit.royalclub.royalclubfootball.model.AdvancedTeamsResponse;
import com.bjit.royalclub.royalclubfootball.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogicNodeServiceImpl implements LogicNodeService {

    private final LogicNodeRepository logicNodeRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentRoundRepository tournamentRoundRepository;
    private final RoundGroupRepository roundGroupRepository;
    private final LogicNodeExecutor logicNodeExecutor;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public LogicNodeResponse createLogicNode(LogicNodeRequest request) {
        log.info("Creating logic node: {} for tournament ID: {}", request.getNodeName(), request.getTournamentId());

        Tournament tournament = tournamentRepository.findById(request.getTournamentId())
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Validate source (must have either sourceRoundId or sourceGroupId, but not both)
        if (request.getSourceRoundId() == null && request.getSourceGroupId() == null) {
            throw new RoundServiceException(
                    "Either sourceRoundId or sourceGroupId must be provided",
                    HttpStatus.BAD_REQUEST);
        }
        if (request.getSourceRoundId() != null && request.getSourceGroupId() != null) {
            throw new RoundServiceException(
                    "Cannot specify both sourceRoundId and sourceGroupId",
                    HttpStatus.BAD_REQUEST);
        }

        TournamentRound sourceRound = null;
        RoundGroup sourceGroup = null;
        if (request.getSourceRoundId() != null) {
            sourceRound = tournamentRoundRepository.findById(request.getSourceRoundId())
                    .orElseThrow(() -> new RoundServiceException(ROUND_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        } else {
            sourceGroup = roundGroupRepository.findById(request.getSourceGroupId())
                    .orElseThrow(() -> new RoundServiceException(GROUP_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        }

        TournamentRound targetRound = tournamentRoundRepository.findById(request.getTargetRoundId())
                .orElseThrow(() -> new RoundServiceException(ROUND_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Validate node type
        LogicNodeType nodeType;
        try {
            nodeType = LogicNodeType.valueOf(request.getNodeType());
        } catch (IllegalArgumentException e) {
            throw new RoundServiceException(
                    "Invalid node type: " + request.getNodeType(),
                    HttpStatus.BAD_REQUEST);
        }

        LogicNode logicNode = LogicNode.builder()
                .tournament(tournament)
                .nodeName(request.getNodeName())
                .nodeType(nodeType)
                .sourceRound(sourceRound)
                .sourceGroup(sourceGroup)
                .targetRound(targetRound)
                .ruleConfig(request.getRuleConfig())
                .priorityOrder(request.getPriorityOrder())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .autoExecute(request.getAutoExecute() != null ? request.getAutoExecute() : true)
                .executionCount(0)
                .build();

        LogicNode savedNode = logicNodeRepository.save(logicNode);
        log.info("Logic node created successfully with ID: {}", savedNode.getId());

        return convertToResponse(savedNode);
    }

    @Override
    @Transactional
    public LogicNodeResponse updateLogicNode(Long nodeId, LogicNodeRequest request) {
        log.info("Updating logic node ID: {}", nodeId);

        LogicNode logicNode = logicNodeRepository.findById(nodeId)
                .orElseThrow(() -> new RoundServiceException(
                        "Logic node not found with ID: " + nodeId,
                        HttpStatus.NOT_FOUND));

        // Update fields
        if (request.getNodeName() != null) {
            logicNode.setNodeName(request.getNodeName());
        }
        if (request.getNodeType() != null) {
            try {
                logicNode.setNodeType(LogicNodeType.valueOf(request.getNodeType()));
            } catch (IllegalArgumentException e) {
                throw new RoundServiceException(
                        "Invalid node type: " + request.getNodeType(),
                        HttpStatus.BAD_REQUEST);
            }
        }
        if (request.getRuleConfig() != null) {
            logicNode.setRuleConfig(request.getRuleConfig());
        }
        if (request.getPriorityOrder() != null) {
            logicNode.setPriorityOrder(request.getPriorityOrder());
        }
        if (request.getIsActive() != null) {
            logicNode.setIsActive(request.getIsActive());
        }
        if (request.getAutoExecute() != null) {
            logicNode.setAutoExecute(request.getAutoExecute());
        }

        // Update source if provided
        if (request.getSourceRoundId() != null) {
            TournamentRound sourceRound = tournamentRoundRepository.findById(request.getSourceRoundId())
                    .orElseThrow(() -> new RoundServiceException(ROUND_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
            logicNode.setSourceRound(sourceRound);
            logicNode.setSourceGroup(null);
        } else if (request.getSourceGroupId() != null) {
            RoundGroup sourceGroup = roundGroupRepository.findById(request.getSourceGroupId())
                    .orElseThrow(() -> new RoundServiceException(GROUP_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
            logicNode.setSourceGroup(sourceGroup);
            logicNode.setSourceRound(null);
        }

        // Update target if provided
        if (request.getTargetRoundId() != null) {
            TournamentRound targetRound = tournamentRoundRepository.findById(request.getTargetRoundId())
                    .orElseThrow(() -> new RoundServiceException(ROUND_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
            logicNode.setTargetRound(targetRound);
        }

        LogicNode savedNode = logicNodeRepository.save(logicNode);
        log.info("Logic node updated successfully with ID: {}", nodeId);

        return convertToResponse(savedNode);
    }

    @Override
    @Transactional
    public void deleteLogicNode(Long nodeId) {
        log.info("Deleting logic node ID: {}", nodeId);

        LogicNode logicNode = logicNodeRepository.findById(nodeId)
                .orElseThrow(() -> new RoundServiceException(
                        "Logic node not found with ID: " + nodeId,
                        HttpStatus.NOT_FOUND));

        logicNodeRepository.delete(logicNode);
        log.info("Logic node deleted successfully with ID: {}", nodeId);
    }

    @Override
    public LogicNodeResponse getLogicNodeById(Long nodeId) {
        log.info("Fetching logic node ID: {}", nodeId);

        LogicNode logicNode = logicNodeRepository.findById(nodeId)
                .orElseThrow(() -> new RoundServiceException(
                        "Logic node not found with ID: " + nodeId,
                        HttpStatus.NOT_FOUND));

        return convertToResponse(logicNode);
    }

    @Override
    public List<LogicNodeResponse> getLogicNodesByTournament(Long tournamentId) {
        log.info("Fetching logic nodes for tournament ID: {}", tournamentId);

        List<LogicNode> logicNodes = logicNodeRepository.findByTournamentId(tournamentId);

        return logicNodes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LogicNodeResponse executeLogicNode(Long nodeId) {
        log.info("Manually executing logic node ID: {}", nodeId);

        LogicNode logicNode = logicNodeRepository.findById(nodeId)
                .orElseThrow(() -> new RoundServiceException(
                        "Logic node not found with ID: " + nodeId,
                        HttpStatus.NOT_FOUND));

        if (!logicNode.getIsActive()) {
            throw new RoundServiceException(
                    "Cannot execute inactive logic node",
                    HttpStatus.BAD_REQUEST);
        }

        // Execute the logic node using LogicNodeExecutor
        AdvancedTeamsResponse result = logicNodeExecutor.executeLogicNode(logicNode);

        // Update execution count
        logicNode.setExecutionCount(logicNode.getExecutionCount() + 1);
        logicNode.setLastExecutedAt(java.time.LocalDateTime.now());
        logicNodeRepository.save(logicNode);

        log.info("Logic node executed successfully. Execution count: {}. Teams advanced: {}", 
                logicNode.getExecutionCount(), result != null ? result.getTeamsAdvanced() : 0);

        return convertToResponse(logicNode);
    }

    @Override
    @Transactional
    public void executeLogicNodesForRound(Long roundId) {
        log.info("Executing auto-execute logic nodes for round ID: {}", roundId);

        List<LogicNode> logicNodes = logicNodeRepository.findAutoExecuteBySourceRoundId(roundId);

        for (LogicNode logicNode : logicNodes) {
            try {
                executeLogicNode(logicNode.getId());
                log.info("Auto-executed logic node ID: {}", logicNode.getId());
            } catch (Exception e) {
                log.error("Failed to execute logic node ID: {}", logicNode.getId(), e);
                // Continue with other nodes
            }
        }
    }

    @Override
    @Transactional
    public void executeLogicNodesForGroup(Long groupId) {
        log.info("Executing auto-execute logic nodes for group ID: {}", groupId);

        List<LogicNode> logicNodes = logicNodeRepository.findAutoExecuteBySourceGroupId(groupId);

        for (LogicNode logicNode : logicNodes) {
            try {
                executeLogicNode(logicNode.getId());
                log.info("Auto-executed logic node ID: {}", logicNode.getId());
            } catch (Exception e) {
                log.error("Failed to execute logic node ID: {}", logicNode.getId(), e);
                // Continue with other nodes
            }
        }
    }

    private LogicNodeResponse convertToResponse(LogicNode logicNode) {
        return LogicNodeResponse.builder()
                .id(logicNode.getId())
                .tournamentId(logicNode.getTournament().getId())
                .tournamentName(logicNode.getTournament().getName())
                .nodeName(logicNode.getNodeName())
                .nodeType(logicNode.getNodeType().toString())
                .sourceRoundId(logicNode.getSourceRound() != null ? logicNode.getSourceRound().getId() : null)
                .sourceRoundName(logicNode.getSourceRound() != null ? logicNode.getSourceRound().getRoundName() : null)
                .sourceGroupId(logicNode.getSourceGroup() != null ? logicNode.getSourceGroup().getId() : null)
                .sourceGroupName(logicNode.getSourceGroup() != null ? logicNode.getSourceGroup().getGroupName() : null)
                .targetRoundId(logicNode.getTargetRound().getId())
                .targetRoundName(logicNode.getTargetRound().getRoundName())
                .ruleConfig(logicNode.getRuleConfig())
                .priorityOrder(logicNode.getPriorityOrder())
                .isActive(logicNode.getIsActive())
                .autoExecute(logicNode.getAutoExecute())
                .executionCount(logicNode.getExecutionCount())
                .lastExecutedAt(logicNode.getLastExecutedAt())
                .createdDate(logicNode.getCreatedDate())
                .updatedDate(logicNode.getUpdatedDate())
                .build();
    }
}

