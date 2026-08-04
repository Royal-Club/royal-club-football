package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.ResourceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ResourceCategoryRepository extends JpaRepository<ResourceCategory, Long> {

    Optional<ResourceCategory> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @Query("SELECT c FROM ResourceCategory c ORDER BY c.sortOrder ASC, c.name ASC")
    List<ResourceCategory> findAllOrdered();
}
