package io.softwarestrategies.tradescout.repository;

import io.softwarestrategies.tradescout.domain.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

	List<Trade> findByStatus(Trade.TradeStatus status);

	List<Trade> findBySymbolOrderByEntryDateDesc(String symbol);

	List<Trade> findByEntryDateBetween(LocalDate start, LocalDate end);

	@Query("SELECT t FROM Trade t WHERE t.status = 'CLOSED' " +
			"AND t.exitDate BETWEEN :start AND :end ORDER BY t.exitDate DESC")
	List<Trade> findClosedTradesBetween(
			@Param("start") LocalDate start,
			@Param("end") LocalDate end
	);

	@Query("SELECT COUNT(t) FROM Trade t WHERE t.status = 'OPEN'")
	long countOpenTrades();

	@Query("SELECT COUNT(t) FROM Trade t WHERE t.entryDate >= :startDate " +
			"AND t.entryDate <= :endDate")
	long countTradesInPeriod(
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate
	);

	@Query("SELECT t FROM Trade t WHERE t.status = 'CLOSED' " +
			"AND t.pnl < 0 ORDER BY t.exitDate DESC")
	List<Trade> findRecentLosers();
}