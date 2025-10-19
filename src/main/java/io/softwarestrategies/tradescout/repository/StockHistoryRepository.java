package io.softwarestrategies.tradescout.repository;

import io.softwarestrategies.tradescout.domain.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {

	Optional<StockHistory> findBySymbolAndTradeDate(String symbol, LocalDate tradeDate);

	List<StockHistory> findBySymbolAndTradeDateBetweenOrderByTradeDateDesc(
			String symbol, LocalDate start, LocalDate end
	);

	List<StockHistory> findBySymbolOrderByTradeDateDesc(String symbol);

	@Query("SELECT sh FROM StockHistory sh WHERE sh.symbol = :symbol " +
			"AND sh.tradeDate >= :startDate ORDER BY sh.tradeDate DESC")
	List<StockHistory> findRecentHistory(
			@Param("symbol") String symbol,
			@Param("startDate") LocalDate startDate
	);

	@Query("SELECT DISTINCT sh.symbol FROM StockHistory sh")
	List<String> findAllDistinctSymbols();

	void deleteBySymbolAndTradeDateBefore(String symbol, LocalDate before);
}