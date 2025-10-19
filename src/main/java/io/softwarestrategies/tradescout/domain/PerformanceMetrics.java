package io.softwarestrategies.tradescout.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity storing performance metrics for a given time period.
 * Used for tracking progress toward annual targets.
 */
@Entity
@Table(name = "performance_metrics",
		indexes = {
				@Index(name = "idx_performance_period", columnList = "period_type,period_end")
		})
public class PerformanceMetrics {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "period_type", nullable = false, length = 20)
	private PeriodType periodType;

	@Column(name = "period_start", nullable = false)
	private LocalDate periodStart;

	@Column(name = "period_end", nullable = false)
	private LocalDate periodEnd;

	@Column(name = "starting_capital", precision = 12, scale = 2)
	private BigDecimal startingCapital;

	@Column(name = "ending_capital", precision = 12, scale = 2)
	private BigDecimal endingCapital;

	@Column(name = "total_trades")
	private Integer totalTrades;

	@Column(name = "winning_trades")
	private Integer winningTrades;

	@Column(name = "losing_trades")
	private Integer losingTrades;

	@Column(name = "win_rate")
	private Double winRate;

	@Column(name = "total_pnl", precision = 12, scale = 2)
	private BigDecimal totalPnl;

	@Column(name = "return_percent")
	private Double returnPercent;

	@Column(name = "avg_win", precision = 10, scale = 2)
	private BigDecimal avgWin;

	@Column(name = "avg_loss", precision = 10, scale = 2)
	private BigDecimal avgLoss;

	@Column(name = "largest_win", precision = 10, scale = 2)
	private BigDecimal largestWin;

	@Column(name = "largest_loss", precision = 10, scale = 2)
	private BigDecimal largestLoss;

	@Column(name = "profit_factor")
	private Double profitFactor;

	@Column(name = "max_drawdown", precision = 10, scale = 2)
	private BigDecimal maxDrawdown;

	@Column(name = "max_drawdown_percent")
	private Double maxDrawdownPercent;

	@Column(name = "sharpe_ratio")
	private Double sharpeRatio;

	@Column(name = "on_pace_for_annual_target")
	private Boolean onPaceForAnnualTarget;

	@Column(name = "projected_annual_return")
	private Double projectedAnnualReturn;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = Instant.now();
	}

	public enum PeriodType {
		WEEKLY, MONTHLY, QUARTERLY, ANNUAL
	}

	// Constructors
	public PerformanceMetrics() {}

	public PerformanceMetrics(PeriodType periodType, LocalDate periodStart, LocalDate periodEnd) {
		this.periodType = periodType;
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
	}

	// Getters and setters
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public PeriodType getPeriodType() { return periodType; }
	public void setPeriodType(PeriodType periodType) { this.periodType = periodType; }

	public LocalDate getPeriodStart() { return periodStart; }
	public void setPeriodStart(LocalDate periodStart) {
		this.periodStart = periodStart;
	}

	public LocalDate getPeriodEnd() { return periodEnd; }
	public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

	public BigDecimal getStartingCapital() { return startingCapital; }
	public void setStartingCapital(BigDecimal startingCapital) {
		this.startingCapital = startingCapital;
	}

	public BigDecimal getEndingCapital() { return endingCapital; }
	public void setEndingCapital(BigDecimal endingCapital) {
		this.endingCapital = endingCapital;
	}

	public Integer getTotalTrades() { return totalTrades; }
	public void setTotalTrades(Integer totalTrades) {
		this.totalTrades = totalTrades;
	}

	public Integer getWinningTrades() { return winningTrades; }
	public void setWinningTrades(Integer winningTrades) {
		this.winningTrades = winningTrades;
	}

	public Integer getLosingTrades() { return losingTrades; }
	public void setLosingTrades(Integer losingTrades) {
		this.losingTrades = losingTrades;
	}

	public Double getWinRate() { return winRate; }
	public void setWinRate(Double winRate) { this.winRate = winRate; }

	public BigDecimal getTotalPnl() { return totalPnl; }
	public void setTotalPnl(BigDecimal totalPnl) { this.totalPnl = totalPnl; }

	public Double getReturnPercent() { return returnPercent; }
	public void setReturnPercent(Double returnPercent) {
		this.returnPercent = returnPercent;
	}

	public BigDecimal getAvgWin() { return avgWin; }
	public void setAvgWin(BigDecimal avgWin) { this.avgWin = avgWin; }

	public BigDecimal getAvgLoss() { return avgLoss; }
	public void setAvgLoss(BigDecimal avgLoss) { this.avgLoss = avgLoss; }

	public BigDecimal getLargestWin() { return largestWin; }
	public void setLargestWin(BigDecimal largestWin) {
		this.largestWin = largestWin;
	}

	public BigDecimal getLargestLoss() { return largestLoss; }
	public void setLargestLoss(BigDecimal largestLoss) {
		this.largestLoss = largestLoss;
	}

	public Double getProfitFactor() { return profitFactor; }
	public void setProfitFactor(Double profitFactor) {
		this.profitFactor = profitFactor;
	}

	public BigDecimal getMaxDrawdown() { return maxDrawdown; }
	public void setMaxDrawdown(BigDecimal maxDrawdown) {
		this.maxDrawdown = maxDrawdown;
	}

	public Double getMaxDrawdownPercent() { return maxDrawdownPercent; }
	public void setMaxDrawdownPercent(Double maxDrawdownPercent) {
		this.maxDrawdownPercent = maxDrawdownPercent;
	}

	public Double getSharpeRatio() { return sharpeRatio; }
	public void setSharpeRatio(Double sharpeRatio) { this.sharpeRatio = sharpeRatio; }

	public Boolean getOnPaceForAnnualTarget() { return onPaceForAnnualTarget; }
	public void setOnPaceForAnnualTarget(Boolean onPaceForAnnualTarget) {
		this.onPaceForAnnualTarget = onPaceForAnnualTarget;
	}

	public Double getProjectedAnnualReturn() { return projectedAnnualReturn; }
	public void setProjectedAnnualReturn(Double projectedAnnualReturn) {
		this.projectedAnnualReturn = projectedAnnualReturn;
	}

	public Instant getCreatedAt() { return createdAt; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PerformanceMetrics that = (PerformanceMetrics) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}