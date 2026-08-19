package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.TeamChatAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamChatAttachmentRepository extends JpaRepository<TeamChatAttachment, Long> {

    /**
     * An attachment together with the team whose room it belongs to.
     *
     * <p>The download endpoint needs the team to decide whether the caller may have the file at all,
     * so it is fetched in the same query - looking the file up first and authorising afterwards is
     * the shape that leaks one room's documents into another.
     */
    @Query("""
            SELECT a FROM TeamChatAttachment a
            JOIN FETCH a.message m
            JOIN FETCH m.team
            WHERE a.id = :id
            """)
    Optional<TeamChatAttachment> findByIdWithTeam(@Param("id") Long id);
}
