package io.softwarestrategies.tradescout.repository;

import io.softwarestrategies.tradescout.domain.PerformanceMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceMetricsRepository extends JpaRepository<PerformanceMetrics, Long> {

	List<PerformanceMetrics> findByPeriodTypeOrderByPeriodEndDesc(
			PerformanceMetrics.PeriodType periodType
	);

	Optional<PerformanceMetrics> findByPeriodTypeAndPeriodEnd(
			PerformanceMetrics.PeriodType periodType,
			LocalDate periodEnd
	);

	@Query("SELECT pm FROM PerformanceMetrics pm WHERE pm.periodType = :periodType " +
			"AND pm.periodEnd >= :fromDate ORDER BY pm.periodEnd DESC")
	List<PerformanceMetrics> findRecentMetrics(
			@Param("periodType") PerformanceMetrics.PeriodType periodType,
			@Param("fromDate") LocalDate fromDate
	);

	void deleteByPeriodEndBefore(LocalDate before);
}