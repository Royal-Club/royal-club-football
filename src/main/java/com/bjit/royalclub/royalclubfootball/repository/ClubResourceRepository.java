package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.ClubResource;
import com.bjit.royalclub.royalclubfootball.enums.ResourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubResourceRepository extends JpaRepository<ClubResource, Long> {

    /**
     * Pinned items first, then the curated sort order. The category is fetched
     * eagerly because every list row renders its category name.
     */
    @Query("""
            SELECT r FROM ClubResource r
            LEFT JOIN FETCH r.category
            ORDER BY r.isPinned DESC, r.sortOrder ASC, r.id DESC
            """)
    List<ClubResource> findAllOrdered();

    @Query("""
            SELECT r FROM ClubResource r
            LEFT JOIN FETCH r.category
            WHERE r.status = :status
            ORDER BY r.isPinned DESC, r.sortOrder ASC, r.id DESC
            """)
    List<ClubResource> findAllByStatusOrdered(@Param("status") ResourceStatus status);

    @Query("SELECT r FROM ClubResource r LEFT JOIN FETCH r.category WHERE r.slug = :slug")
    Optional<ClubResource> findBySlugWithCategory(@Param("slug") String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    long countByCategoryId(Long categoryId);

    long countByCategoryIdAndStatus(Long categoryId, ResourceStatus status);

    @Modifying
    @Query("UPDATE ClubResource r SET r.viewCount = r.viewCount + 1 WHERE r.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
