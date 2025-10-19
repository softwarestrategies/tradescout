package io.softwarestrategies.tradescout.service;

import io.softwarestrategies.tradescout.config.TradeScoutProperties;
import io.softwarestrategies.tradescout.domain.PerformanceMetrics;
import io.softwarestrategies.tradescout.domain.Trade;
import io.softwarestrategies.tradescout.dto.PerformanceSnapshot;
import io.softwarestrategies.tradescout.dto.QuarterlyReport;
import io.softwarestrategies.tradescout.repository.PerformanceMetricsRepository;
import io.softwarestrategies.tradescout.repository.TradeRepository;
import io.softwarestrategies.tradescout.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for tracking and analyzing trading performance
 */
@Service
public class PerformanceTrackingService {

	private static final Logger log = LoggerFactory.getLogger(PerformanceTrackingService.class);

	private final TradeRepository tradeRepository;
	private final PerformanceMetricsRepository metricsRepository;
	private final DateUtil dateUtil;
	private final TradeScoutProperties properties;

	public PerformanceTrackingService(
			TradeRepository tradeRepository,
			PerformanceMetricsRepository metricsRepository,
			DateUtil dateUtil,
			TradeScoutProperties properties) {
		this.tradeRepository = tradeRepository;
		this.metricsRepository = metricsRepository;
		this.dateUtil = dateUtil;
		this.properties = properties;
	}

	/**
	 * Get current performance snapshot
	 */
	public PerformanceSnapshot getCurrentPerformance() {
		var allTrades = tradeRepository.findAll();
		var closedTrades = allTrades.stream()
				.filter(t -> t.getStatus() == Trade.TradeStatus.CLOSED)
				.toList();

		var totalPnL = closedTrades.stream()
				.map(Trade::getPnl)
				.filter(pnl -> pnl != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		var initialCapital = properties.getTrading().getInitialCapital();
		var currentCapital = initialCapital.add(totalPnL);

		var returnPercent = totalPnL
				.divide(initialCapital, 4, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100))
				.doubleValue();

		var winners = closedTrades.stream().filter(Trade::isWinner).count();
		var winRate = closedTrades.isEmpty() ? 0.0 :
				(double) winners / closedTrades.size() * 100.0;

		var openTrades = (int) tradeRepository.countOpenTrades();

		// Calculate if on pace for annual target
		var targetPercent = properties.getTrading().getAnnualTargetPercent();
		var daysIntoYear = LocalDate.now().getDayOfYear();
		var expectedReturn = (targetPercent / 365.0) * daysIntoYear;
		var onPace = returnPercent >= expectedReturn;

		var projectedAnnual = (returnPercent / daysIntoYear) * 365.0;

		var status = determineStatus(returnPercent, winRate, onPace);

		return new PerformanceSnapshot(
				LocalDate.now(),
				currentCapital,
				totalPnL,
				returnPercent,
				closedTrades.size(),
				openTrades,
				winRate,
				onPace,
				projectedAnnual,
				status
		);
	}

	/**
	 * Generate quarterly report
	 */
	@Transactional
	public QuarterlyReport generateQuarterlyReport() {
		var today = LocalDate.now();
		var quarterStart = dateUtil.getQuarterStart(today);
		var quarterEnd = dateUtil.getQuarterEnd(today);

		log.info("Generating quarterly report: {} to {}", quarterStart, quarterEnd);

		// Get trades for the quarter
		var trades = tradeRepository.findClosedTradesBetween(quarterStart, quarterEnd);

		// Calculate metrics
		var metrics = calculateMetrics(
				PerformanceMetrics.PeriodType.QUARTERLY,
				quarterStart,
				quarterEnd,
				trades
		);

		// Save metrics
		metricsRepository.save(metrics);

		// Generate recommendations
		var recommendations = generateRecommendations(metrics, trades);

		// Target analysis
		var targetAnalysis = analyzeTargetProgress(metrics);

		return new QuarterlyReport(
				metrics,
				trades,
				recommendations,
				targetAnalysis
		);
	}

	/**
	 * Calculate performance metrics for a period
	 */
	private PerformanceMetrics calculateMetrics(
			PerformanceMetrics.PeriodType periodType,
			LocalDate periodStart,
			LocalDate periodEnd,
			List<Trade> trades) {

		var metrics = new PerformanceMetrics(periodType, periodStart, periodEnd);

		if (trades.isEmpty()) {
			return metrics;
		}

		// Basic stats
		metrics.setTotalTrades(trades.size());

		var winners = trades.stream().filter(Trade::isWinner).toList();
		var losers = trades.stream().filter(Trade::isLoser).toList();

		metrics.setWinningTrades(winners.size());
		metrics.setLosingTrades(losers.size());
		metrics.setWinRate((double) winners.size() / trades.size() * 100.0);

		// P&L calculations
		var totalPnL = trades.stream()
				.map(Trade::getPnl)
				.filter(pnl -> pnl != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		metrics.setTotalPnl(totalPnL);

		var initialCapital = properties.getTrading().getInitialCapital();
		metrics.setStartingCapital(initialCapital);
		metrics.setEndingCapital(initialCapital.add(totalPnL));

		var returnPercent = totalPnL
				.divide(initialCapital, 4, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100))
				.doubleValue();
		metrics.setReturnPercent(returnPercent);

		// Win/Loss averages
		if (!winners.isEmpty()) {
			var avgWin = winners.stream()
					.map(Trade::getPnl)
					.reduce(BigDecimal.ZERO, BigDecimal::add)
					.divide(BigDecimal.valueOf(winners.size()), 2, RoundingMode.HALF_UP);
			metrics.setAvgWin(avgWin);

			var largestWin = winners.stream()
					.map(Trade::getPnl)
					.max(BigDecimal::compareTo)
					.orElse(BigDecimal.ZERO);
			metrics.setLargestWin(largestWin);
		}

		if (!losers.isEmpty()) {
			var avgLoss = losers.stream()
					.map(Trade::getPnl)
					.reduce(BigDecimal.ZERO, BigDecimal::add)
					.divide(BigDecimal.valueOf(losers.size()), 2, RoundingMode.HALF_UP);
			metrics.setAvgLoss(avgLoss);

			var largestLoss = losers.stream()
					.map(Trade::getPnl)
					.min(BigDecimal::compareTo)
					.orElse(BigDecimal.ZERO);
			metrics.setLargestLoss(largestLoss);
		}

		// Profit factor
		var totalWins = winners.stream()
				.map(Trade::getPnl)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		var totalLosses = losers.stream()
				.map(Trade::getPnl)
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.abs();

		if (totalLosses.compareTo(BigDecimal.ZERO) > 0) {
			var profitFactor = totalWins
					.divide(totalLosses, 2, RoundingMode.HALF_UP)
					.doubleValue();
			metrics.setProfitFactor(profitFactor);
		}

		// Target analysis
		var targetPercent = properties.getTrading().getQuarterlyTargetPercent();
		metrics.setOnPaceForAnnualTarget(returnPercent >= targetPercent);

		var projectedAnnual = returnPercent * 4; // Quarterly * 4 = Annual
		metrics.setProjectedAnnualReturn(projectedAnnual);

		return metrics;
	}

	/**
	 * Generate recommendations based on performance
	 */
	private List<String> generateRecommendations(PerformanceMetrics metrics, List<Trade> trades) {
		var recommendations = new ArrayList<String>();

		// Win rate analysis
		if (metrics.getWinRate() < 55.0) {
			recommendations.add("Win rate below target (55%). Review entry criteria for quality improvement.");
		} else if (metrics.getWinRate() > 70.0) {
			recommendations.add("Excellent win rate! Consider increasing position size if risk allows.");
		}

		// Profit factor analysis
		if (metrics.getProfitFactor() != null) {
			if (metrics.getProfitFactor() < 1.5) {
				recommendations.add("Profit factor below 1.5. Focus on letting winners run longer.");
			} else if (metrics.getProfitFactor() > 2.0) {
				recommendations.add("Strong profit factor! Current risk/reward strategy is working well.");
			}
		}

		// Trading frequency
		if (metrics.getTotalTrades() < 8) {
			recommendations.add("Below minimum trade frequency. Consider loosening filters slightly.");
		} else if (metrics.getTotalTrades() > 25) {
			recommendations.add("High trade frequency. Ensure quality over quantity.");
		}

		// Return analysis
		var targetReturn = properties.getTrading().getQuarterlyTargetPercent();
		if (metrics.getReturnPercent() < targetReturn) {
			var gap = targetReturn - metrics.getReturnPercent();
			recommendations.add(String.format("Behind quarterly target by %.1f%%. Increase position size or trade frequency.", gap));		}

		// Avg win vs avg loss
		if (metrics.getAvgWin() != null && metrics.getAvgLoss() != null) {
			var ratio = metrics.getAvgWin()
					.divide(metrics.getAvgLoss().abs(), 2, RoundingMode.HALF_UP)
					.doubleValue();

			if (ratio < 1.1) {
				recommendations.add("Average winner barely exceeds average loser. Tighten stops or widen targets.");
			}
		}

		if (recommendations.isEmpty()) {
			recommendations.add("Performance is solid. Continue current strategy.");
		}

		return recommendations;
	}

	/**
	 * Analyze progress toward annual target
	 */
	private QuarterlyReport.TargetAnalysis analyzeTargetProgress(PerformanceMetrics metrics) {
		var annualTarget = properties.getTrading().getAnnualTargetPercent();
		var currentReturn = metrics.getReturnPercent();
		var projectedAnnual = metrics.getProjectedAnnualReturn();

		var onPace = projectedAnnual >= annualTarget;

		// Calculate needed monthly return for rest of year
		var currentQuarter = (LocalDate.now().getMonthValue() - 1) / 3;
		var remainingQuarters = 4 - currentQuarter - 1;
		var neededReturn = annualTarget - currentReturn;
		var neededMonthly = remainingQuarters > 0 ?
				neededReturn / (remainingQuarters * 3) : 0.0;
		var assessment = onPace ?
				String.format("✅ On pace! Projected annual return: %.1f%%", projectedAnnual) :
				String.format("⚠️ Behind pace. Need %.2f%% monthly to hit %.1f%% target", neededMonthly, annualTarget);

		return new QuarterlyReport.TargetAnalysis(
				onPace,
				projectedAnnual,
				neededMonthly,
				assessment
		);
	}

	/**
	 * Determine overall status based on metrics
	 */
	private String determineStatus(double returnPercent, double winRate, boolean onPace) {
		if (onPace && winRate >= 60.0) {
			return "Excellent - On target with strong win rate";
		} else if (onPace) {
			return "Good - On pace for annual target";
		} else if (returnPercent > 0) {
			return "Fair - Profitable but below target";
		} else {
			return "Needs Improvement - Below expectations";
		}
	}
}