package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.CostType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CostTypeRepository extends JpaRepository<CostType, Long> {
    Optional<CostType> findByName(String costType);
}
