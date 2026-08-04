package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.ResourceView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResourceViewRepository extends JpaRepository<ResourceView, Long> {

    Optional<ResourceView> findByResourceIdAndPlayerId(Long resourceId, Long playerId);

    long countByResourceId(Long resourceId);

    List<ResourceView> findByResourceIdOrderByLastViewedAtDesc(Long resourceId);
}
