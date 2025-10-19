package io.softwarestrategies.tradescout.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity storing calculated volatility metrics for a stock.
 * Used for anomaly detection and risk assessment.
 */
@Entity
@Table(name = "volatility_metrics",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_volatility_metrics",
						columnNames = {"symbol", "calculation_date", "lookback_days"})
		},
		indexes = {
				@Index(name = "idx_volatility_metrics_symbol", columnList = "symbol,calculation_date")
		})
public class VolatilityMetrics {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 10)
	private String symbol;

	@Column(name = "calculation_date", nullable = false)
	private LocalDate calculationDate;

	@Column(name = "lookback_days", nullable = false)
	private Integer lookbackDays;

	// Daily range metrics
	@Column(name = "avg_daily_range_pct")
	private Double avgDailyRangePct;

	@Column(name = "stddev_daily_range_pct")
	private Double stddevDailyRangePct;

	// Intraday drop metrics (most important for our strategy)
	@Column(name = "avg_max_drop_pct")
	private Double avgMaxDropPct;

	@Column(name = "stddev_max_drop_pct")
	private Double stddevMaxDropPct;

	// Daily change metrics
	@Column(name = "avg_daily_change_pct")
	private Double avgDailyChangePct;

	@Column(name = "stddev_daily_change_pct")
	private Double stddevDailyChangePct;

	// Volume metrics
	@Column(name = "avg_volume")
	private Long avgVolume;

	@Column(name = "stddev_volume")
	private Long stddevVolume;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = Instant.now();
	}

	// Constructors
	public VolatilityMetrics() {}

	public VolatilityMetrics(String symbol, LocalDate calculationDate, Integer lookbackDays) {
		this.symbol = symbol;
		this.calculationDate = calculationDate;
		this.lookbackDays = lookbackDays;
	}

	// Getters and setters
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getSymbol() { return symbol; }
	public void setSymbol(String symbol) { this.symbol = symbol; }

	public LocalDate getCalculationDate() { return calculationDate; }
	public void setCalculationDate(LocalDate calculationDate) {
		this.calculationDate = calculationDate;
	}

	public Integer getLookbackDays() { return lookbackDays; }
	public void setLookbackDays(Integer lookbackDays) {
		this.lookbackDays = lookbackDays;
	}

	public Double getAvgDailyRangePct() { return avgDailyRangePct; }
	public void setAvgDailyRangePct(Double avgDailyRangePct) {
		this.avgDailyRangePct = avgDailyRangePct;
	}

	public Double getStddevDailyRangePct() { return stddevDailyRangePct; }
	public void setStddevDailyRangePct(Double stddevDailyRangePct) {
		this.stddevDailyRangePct = stddevDailyRangePct;
	}

	public Double getAvgMaxDropPct() { return avgMaxDropPct; }
	public void setAvgMaxDropPct(Double avgMaxDropPct) {
		this.avgMaxDropPct = avgMaxDropPct;
	}

	public Double getStddevMaxDropPct() { return stddevMaxDropPct; }
	public void setStddevMaxDropPct(Double stddevMaxDropPct) {
		this.stddevMaxDropPct = stddevMaxDropPct;
	}

	public Double getAvgDailyChangePct() { return avgDailyChangePct; }
	public void setAvgDailyChangePct(Double avgDailyChangePct) {
		this.avgDailyChangePct = avgDailyChangePct;
	}

	public Double getStddevDailyChangePct() { return stddevDailyChangePct; }
	public void setStddevDailyChangePct(Double stddevDailyChangePct) {
		this.stddevDailyChangePct = stddevDailyChangePct;
	}

	public Long getAvgVolume() { return avgVolume; }
	public void setAvgVolume(Long avgVolume) { this.avgVolume = avgVolume; }

	public Long getStddevVolume() { return stddevVolume; }
	public void setStddevVolume(Long stddevVolume) {
		this.stddevVolume = stddevVolume;
	}

	public Instant getCreatedAt() { return createdAt; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VolatilityMetrics that = (VolatilityMetrics) o;
		return Objects.equals(symbol, that.symbol) &&
				Objects.equals(calculationDate, that.calculationDate) &&
				Objects.equals(lookbackDays, that.lookbackDays);
	}

	@Override
	public int hashCode() {
		return Objects.hash(symbol, calculationDate, lookbackDays);
	}
}