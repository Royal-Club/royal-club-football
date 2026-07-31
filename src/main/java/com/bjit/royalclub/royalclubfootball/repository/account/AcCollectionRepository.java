package com.bjit.royalclub.royalclubfootball.repository.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AcCollectionRepository extends JpaRepository<AcCollection, Long> {
    List<AcCollection> findByMonthOfPaymentBetween
            (LocalDate startDate, LocalDate endDate);

    AcCollection findByTransactionId(String transactionId);

    @Query("SELECT c FROM AcCollection c WHERE FUNCTION('YEAR', c.date) = :year")
    List<AcCollection> findCollectionsByYear(@Param("year") Integer year);

    @Query("SELECT DISTINCT c FROM AcCollection c LEFT JOIN FETCH c.players")
    List<AcCollection> findAllWithPlayers();

    @Query("SELECT DISTINCT c FROM AcCollection c LEFT JOIN FETCH c.players WHERE FUNCTION('YEAR', c.date) = :year")
    List<AcCollection> findCollectionsByYearWithPlayers(@Param("year") Integer year);

    @Query("SELECT DISTINCT YEAR(c.date) FROM AcCollection c ORDER BY YEAR(c.date) DESC")
    List<Integer> findAllCollectionYears();

    /** Every collection this player was included in, newest month first — their payment history. */
    @Query("SELECT c FROM AcCollection c JOIN c.players p WHERE p.id = :playerId "
            + "ORDER BY c.monthOfPayment DESC, c.date DESC")
    List<AcCollection> findByPlayerId(@Param("playerId") Long playerId);
}
