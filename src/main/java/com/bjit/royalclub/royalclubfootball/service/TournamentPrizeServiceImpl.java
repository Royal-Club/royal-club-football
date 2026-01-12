package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.TournamentPrize;
import com.bjit.royalclub.royalclubfootball.enums.PrizeCategory;
import com.bjit.royalclub.royalclubfootball.enums.PrizeType;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.model.TournamentPrizeRequest;
import com.bjit.royalclub.royalclubfootball.model.TournamentPrizeResponse;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentPrizeRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentPrizeServiceImpl implements TournamentPrizeService {

    private final TournamentPrizeRepository prizeRepository;
    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public TournamentPrizeResponse createPrize(TournamentPrizeRequest request) {
        // Validate tournament exists
        Tournament tournament = tournamentRepository.findById(request.getTournamentId())
                .orElseThrow(() -> new PlayerServiceException(
                        "Tournament not found with ID: " + request.getTournamentId(),
                        HttpStatus.NOT_FOUND
                ));

        // Validate prize type and recipient
        validatePrizeRequest(request);

        // Check for duplicate prize category
        checkDuplicatePrize(request);

        // Build the prize entity
        TournamentPrize.TournamentPrizeBuilder prizeBuilder = TournamentPrize.builder()
                .tournament(tournament)
                .prizeType(request.getPrizeType())
                .positionRank(request.getPositionRank())
                .prizeAmount(request.getPrizeAmount())
                .prizeCategory(request.getPrizeCategory())
                .description(request.getDescription())
                .imageLinks(convertListToJson(request.getImageLinks()));

        // Set team or player based on prize type
        if (request.getPrizeType() == PrizeType.TEAM) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new PlayerServiceException(
                            "Team not found with ID: " + request.getTeamId(),
                            HttpStatus.NOT_FOUND
                    ));
            prizeBuilder.team(team);
        } else {
            Player player = playerRepository.findById(request.getPlayerId())
                    .orElseThrow(() -> new PlayerServiceException(
                            "Player not found with ID: " + request.getPlayerId(),
                            HttpStatus.NOT_FOUND
                    ));
            prizeBuilder.player(player);
        }

        TournamentPrize savedPrize = prizeRepository.save(prizeBuilder.build());
        return mapToResponse(savedPrize);
    }

    @Override
    @Transactional
    public TournamentPrizeResponse updatePrize(Long prizeId, TournamentPrizeRequest request) {
        TournamentPrize existingPrize = prizeRepository.findById(prizeId)
                .orElseThrow(() -> new PlayerServiceException(
                        "Prize not found with ID: " + prizeId,
                        HttpStatus.NOT_FOUND
                ));

        // Validate tournament exists
        Tournament tournament = tournamentRepository.findById(request.getTournamentId())
                .orElseThrow(() -> new PlayerServiceException(
                        "Tournament not found with ID: " + request.getTournamentId(),
                        HttpStatus.NOT_FOUND
                ));

        // Validate prize type and recipient
        validatePrizeRequest(request);

        // Update fields
        existingPrize.setTournament(tournament);
        existingPrize.setPrizeType(request.getPrizeType());
        existingPrize.setPositionRank(request.getPositionRank());
        existingPrize.setPrizeAmount(request.getPrizeAmount());
        existingPrize.setPrizeCategory(request.getPrizeCategory());
        existingPrize.setDescription(request.getDescription());
        existingPrize.setImageLinks(convertListToJson(request.getImageLinks()));

        // Update team or player based on prize type
        if (request.getPrizeType() == PrizeType.TEAM) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new PlayerServiceException(
                            "Team not found with ID: " + request.getTeamId(),
                            HttpStatus.NOT_FOUND
                    ));
            existingPrize.setTeam(team);
            existingPrize.setPlayer(null);
        } else {
            Player player = playerRepository.findById(request.getPlayerId())
                    .orElseThrow(() -> new PlayerServiceException(
                            "Player not found with ID: " + request.getPlayerId(),
                            HttpStatus.NOT_FOUND
                    ));
            existingPrize.setPlayer(player);
            existingPrize.setTeam(null);
        }

        TournamentPrize updatedPrize = prizeRepository.save(existingPrize);
        return mapToResponse(updatedPrize);
    }

    @Override
    @Transactional
    public void deletePrize(Long prizeId) {
        if (!prizeRepository.existsById(prizeId)) {
            throw new PlayerServiceException(
                    "Prize not found with ID: " + prizeId,
                    HttpStatus.NOT_FOUND
            );
        }
        prizeRepository.deleteById(prizeId);
    }

    @Override
    public TournamentPrizeResponse getPrizeById(Long prizeId) {
        TournamentPrize prize = prizeRepository.findById(prizeId)
                .orElseThrow(() -> new PlayerServiceException(
                        "Prize not found with ID: " + prizeId,
                        HttpStatus.NOT_FOUND
                ));
        return mapToResponse(prize);
    }

    @Override
    public List<TournamentPrizeResponse> getAllPrizesByTournament(Long tournamentId) {
        List<TournamentPrize> prizes = prizeRepository.findByTournamentIdOrderByPositionRankAsc(tournamentId);
        return prizes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TournamentPrizeResponse> getPrizesByTournamentAndType(Long tournamentId, PrizeType prizeType) {
        List<TournamentPrize> prizes = prizeRepository.findByTournamentIdAndPrizeTypeOrderByPositionRankAsc(
                tournamentId,
                prizeType
        );
        return prizes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TournamentPrizeResponse> getPrizesByTeam(Long tournamentId, Long teamId) {
        List<TournamentPrize> prizes = prizeRepository.findByTournamentIdAndTeamId(tournamentId, teamId);
        return prizes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TournamentPrizeResponse> getPrizesByPlayer(Long tournamentId, Long playerId) {
        List<TournamentPrize> prizes = prizeRepository.findByTournamentIdAndPlayerId(tournamentId, playerId);
        return prizes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Helper methods

    private void validatePrizeRequest(TournamentPrizeRequest request) {
        if (request.getPrizeType() == PrizeType.TEAM) {
            if (request.getTeamId() == null) {
                throw new PlayerServiceException(
                        "Team ID is required for TEAM prize type",
                        HttpStatus.BAD_REQUEST
                );
            }
            if (request.getPlayerId() != null) {
                throw new PlayerServiceException(
                        "Player ID must be null for TEAM prize type",
                        HttpStatus.BAD_REQUEST
                );
            }
        } else if (request.getPrizeType() == PrizeType.PLAYER) {
            if (request.getPlayerId() == null) {
                throw new PlayerServiceException(
                        "Player ID is required for PLAYER prize type",
                        HttpStatus.BAD_REQUEST
                );
            }
            if (request.getTeamId() != null) {
                throw new PlayerServiceException(
                        "Team ID must be null for PLAYER prize type",
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        if (request.getPositionRank() == null || request.getPositionRank() < 1) {
            throw new PlayerServiceException(
                    "Position rank must be at least 1",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (request.getPrizeAmount() != null && request.getPrizeAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new PlayerServiceException(
                    "Prize amount cannot be negative",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void checkDuplicatePrize(TournamentPrizeRequest request) {
        if (request.getPrizeType() == PrizeType.TEAM && request.getTeamId() != null) {
            Optional<TournamentPrize> existing = prizeRepository.findByTournamentIdAndTeamIdAndPrizeCategory(
                    request.getTournamentId(),
                    request.getTeamId(),
                    request.getPrizeCategory()
            );
            if (existing.isPresent()) {
                throw new PlayerServiceException(
                        "Prize already exists for this team and category",
                        HttpStatus.CONFLICT
                );
            }
        } else if (request.getPrizeType() == PrizeType.PLAYER && request.getPlayerId() != null) {
            Optional<TournamentPrize> existing = prizeRepository.findByTournamentIdAndPlayerIdAndPrizeCategory(
                    request.getTournamentId(),
                    request.getPlayerId(),
                    request.getPrizeCategory()
            );
            if (existing.isPresent()) {
                throw new PlayerServiceException(
                        "Prize already exists for this player and category",
                        HttpStatus.CONFLICT
                );
            }
        }
    }

    private String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("Error converting list to JSON", e);
            return null;
        }
    }

    private List<String> convertJsonToList(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to list", e);
            return List.of();
        }
    }

    private TournamentPrizeResponse mapToResponse(TournamentPrize prize) {
        TournamentPrizeResponse.TournamentPrizeResponseBuilder responseBuilder = TournamentPrizeResponse.builder()
                .id(prize.getId())
                .tournamentId(prize.getTournament().getId())
                .tournamentName(prize.getTournament().getName())
                .prizeType(prize.getPrizeType())
                .positionRank(prize.getPositionRank())
                .prizeAmount(prize.getPrizeAmount())
                .prizeCategory(prize.getPrizeCategory())
                .description(prize.getDescription())
                .imageLinks(convertJsonToList(prize.getImageLinks()))
                .createdDate(prize.getCreatedDate())
                .updatedDate(prize.getUpdatedDate());

        if (prize.getTeam() != null) {
            responseBuilder
                    .teamId(prize.getTeam().getId())
                    .teamName(prize.getTeam().getTeamName());
        }

        if (prize.getPlayer() != null) {
            responseBuilder
                    .playerId(prize.getPlayer().getId())
                    .playerName(prize.getPlayer().getName())
                    .playerEmployeeId(prize.getPlayer().getEmployeeId());
        }

        return responseBuilder.build();
    }
}
