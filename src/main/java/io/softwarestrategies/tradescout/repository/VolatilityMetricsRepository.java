package io.softwarestrategies.tradescout.repository;

import io.softwarestrategies.tradescout.domain.VolatilityMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VolatilityMetricsRepository extends JpaRepository<VolatilityMetrics, Long> {

	Optional<VolatilityMetrics> findBySymbolAndCalculationDateAndLookbackDays(
			String symbol, LocalDate calculationDate, Integer lookbackDays
	);

	@Query("SELECT vm FROM VolatilityMetrics vm WHERE vm.symbol = :symbol " +
			"ORDER BY vm.calculationDate DESC LIMIT 1")
	Optional<VolatilityMetrics> findLatestBySymbol(@Param("symbol") String symbol);

	List<VolatilityMetrics> findBySymbolOrderByCalculationDateDesc(String symbol);

	void deleteByCalculationDateBefore(LocalDate before);
}
