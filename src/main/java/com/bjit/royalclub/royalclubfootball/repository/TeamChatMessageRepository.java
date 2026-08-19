package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.TeamChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamChatMessageRepository extends JpaRepository<TeamChatMessage, Long> {

    /**
     * A page of one room's messages, newest first, optionally older than a message the caller
     * already holds.
     *
     * <p>Paged by id rather than by offset because the room is live: with an offset, a message
     * arriving between two scroll-ups shifts every older row down and the reader sees one of them
     * twice.
     *
     * <p>Only the sender is joined here. Fetching the attachment collection in the same query would
     * make the row count differ from the message count, and Hibernate would answer by loading the
     * entire history and paging it in memory - fine for a first fixture, quietly ruinous for a room
     * with a season behind it. Attachments come from {@link #fetchAttachments} instead.
     */
    @Query("""
            SELECT m FROM TeamChatMessage m
            JOIN FETCH m.sender
            WHERE m.team.id = :teamId
            ORDER BY m.id DESC
            """)
    List<TeamChatMessage> findLatestPage(@Param("teamId") Long teamId, Pageable pageable);

    /**
     * Split from {@link #findLatestPage} rather than folded into it behind a nullable parameter.
     * A single query guarded by {@code (:beforeId IS NULL OR m.id < :beforeId)} reads more neatly but
     * makes the database compare an untyped null on every page load, which different drivers handle
     * differently. Two queries have one obvious meaning each.
     */
    @Query("""
            SELECT m FROM TeamChatMessage m
            JOIN FETCH m.sender
            WHERE m.team.id = :teamId
              AND m.id < :beforeId
            ORDER BY m.id DESC
            """)
    List<TeamChatMessage> findPageBefore(@Param("teamId") Long teamId,
                                         @Param("beforeId") Long beforeId,
                                         Pageable pageable);

    /**
     * Loads the attachments of an already-fetched page in one round trip.
     *
     * <p>The return value is deliberately ignored by callers: running this inside the same
     * transaction populates the collections on the messages the persistence context is already
     * holding, which is what turns one query per message into one query per page.
     */
    @Query("""
            SELECT DISTINCT m FROM TeamChatMessage m
            LEFT JOIN FETCH m.attachments
            WHERE m.id IN :ids
            """)
    List<TeamChatMessage> fetchAttachments(@Param("ids") List<Long> ids);

    /**
     * Bytes already shared into one room, across every message.
     *
     * <p>Summed on demand rather than kept as a running total on the team. A counter would have to be
     * corrected on every delete and every failed post, and a counter that drifts is worse than no
     * counter at all - it would refuse uploads into a room that is actually empty. A room holds a
     * few dozen rows, so the sum is cheap.
     *
     * @return zero for a room with no attachments, never null
     */
    @Query("""
            SELECT COALESCE(SUM(a.sizeBytes), 0) FROM TeamChatAttachment a
            WHERE a.message.team.id = :teamId
            """)
    long sumAttachmentBytesByTeamId(@Param("teamId") Long teamId);

    /**
     * Hard delete. Bulk rather than {@code deleteAll} because a purge has no interest in loading a
     * season of chat into memory to throw it away; the attachment rows follow through the foreign
     * key's ON DELETE CASCADE.
     */
    @Modifying
    @Query("DELETE FROM TeamChatMessage m WHERE m.team.id = :teamId")
    int deleteByTeamId(@Param("teamId") Long teamId);

    long countByTeamId(Long teamId);
}
