package io.softwarestrategies.tradescout.service;

import io.softwarestrategies.tradescout.config.TradeScoutProperties;
import io.softwarestrategies.tradescout.domain.VolatilityMetrics;
import io.softwarestrategies.tradescout.dto.OpportunitySignal;
import io.softwarestrategies.tradescout.repository.VolatilityMetricsRepository;
import io.softwarestrategies.tradescout.util.StatisticsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import yahoofinance.YahooFinance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for detecting unusual market movements (anomalies)
 * that could represent trading opportunities
 */
@Service
public class AnomalyDetectionService {

	private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionService.class);

	private final VolatilityMetricsRepository metricsRepository;
	private final StatisticsUtil statisticsUtil;
	private final TradeScoutProperties properties;

	public AnomalyDetectionService(
			VolatilityMetricsRepository metricsRepository,
			StatisticsUtil statisticsUtil,
			TradeScoutProperties properties) {
		this.metricsRepository = metricsRepository;
		this.statisticsUtil = statisticsUtil;
		this.properties = properties;
	}

	/**
	 * Scan all watchlist stocks for opportunities
	 */
	public List<OpportunitySignal> scanForOpportunities() {
		var watchlist = properties.getTrading().getWatchlist();
		var opportunities = new ArrayList<OpportunitySignal>();

		log.info("Scanning {} stocks for opportunities", watchlist.size());

		for (var symbol : watchlist) {
			try {
				var signal = analyzeStock(symbol);
				if (signal != null && signal.isOpportunity()) {
					opportunities.add(signal);
					log.info("Opportunity detected: {} ({}% confidence)",
							symbol, String.format("%.0f", signal.confidence()));
				}
			} catch (Exception e) {
				log.error("Error analyzing {}: {}", symbol, e.getMessage());
			}
		}

		log.info("Scan complete: {} opportunities found", opportunities.size());
		return opportunities;
	}

	/**
	 * Analyze a single stock for anomalies
	 */
	public OpportunitySignal analyzeStock(String symbol) throws Exception {
		// Get current quote
		var stock = YahooFinance.get(symbol);
		var quote = stock.getQuote();

		if (quote.getPrice() == null || quote.getOpen() == null) {
			log.debug("Skipping {} - no valid price data", symbol);
			return null;
		}

		var currentPrice = quote.getPrice();
		var todayOpen = quote.getOpen();
		var todayHigh = quote.getDayHigh();
		var todayLow = quote.getDayLow();
		var currentVolume = quote.getVolume();

		// Calculate current drop from open
		var currentDropPct = currentPrice.subtract(todayOpen)
				.divide(todayOpen, 4, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100))
				.doubleValue();

		// Get historical metrics
		var metricsOpt = metricsRepository.findLatestBySymbol(symbol);

		if (metricsOpt.isEmpty()) {
			log.debug("No metrics found for {}", symbol);
			return null;
		}

		var metrics = metricsOpt.get();

		// Calculate Z-scores
		var priceZScore = statisticsUtil.calculateZScore(
				currentDropPct,
				metrics.getAvgMaxDropPct(),
				metrics.getStddevMaxDropPct()
		);

		var volumeZScore = statisticsUtil.calculateZScore(
				currentVolume.doubleValue(),
				metrics.getAvgVolume().doubleValue(),
				metrics.getStddevVolume().doubleValue()
		);

		// Determine if this is an opportunity
		var minPriceZScore = properties.getTrading().getDetection().getMinPriceZscore();
		var minVolumeZScore = properties.getTrading().getDetection().getMinVolumeZscore();
		var minConfidence = properties.getTrading().getDetection().getMinConfidence();

		var isUnusualDrop = priceZScore < minPriceZScore;
		var isLowVolume = volumeZScore < minVolumeZScore;

		// Calculate confidence
		var confidence = calculateConfidence(priceZScore, volumeZScore, metrics);

		var isOpportunity = isUnusualDrop && isLowVolume && confidence >= minConfidence;

		// Build reasoning
		var reason = buildReason(currentDropPct, priceZScore, volumeZScore, metrics);

		// Create historical context
		var historicalContext = new OpportunitySignal.HistoricalContext(
				metrics.getAvgMaxDropPct(),
				metrics.getStddevMaxDropPct(),
				metrics.getAvgVolume(),
				metrics.getStddevVolume(),
				metrics.getAvgDailyChangePct()
		);

		return new OpportunitySignal(
				symbol,
				currentPrice,
				todayOpen,
				todayHigh,
				todayLow,
				currentVolume,
				currentDropPct,
				priceZScore,
				volumeZScore,
				confidence,
				isOpportunity,
				reason,
				historicalContext,
				Instant.now()
		);
	}

	/**
	 * Calculate confidence score for the signal
	 */
	private double calculateConfidence(double priceZScore, double volumeZScore,
									   VolatilityMetrics metrics) {
		var confidence = 0.0;

		// Base confidence on how unusual the price drop is (0-50 points)
		// -2σ = 0 points, -3σ = 25 points, -4σ = 50 points
		var priceConfidence = (Math.abs(priceZScore) - 2.0) * 25.0;
		confidence += Math.max(0, Math.min(50, priceConfidence));

		// Add confidence if volume is low (0-30 points)
		// -1σ = 10 points, -2σ = 20 points, -3σ = 30 points
		if (volumeZScore < 0) {
			var volumeConfidence = Math.abs(volumeZScore) * 10.0;
			confidence += Math.max(0, Math.min(30, volumeConfidence));
		}

		// Bonus: Recent trend was positive (0-20 points)
		if (metrics.getAvgDailyChangePct() != null && metrics.getAvgDailyChangePct() > 0) {
			var trendBonus = Math.min(20, metrics.getAvgDailyChangePct() * 2);
			confidence += trendBonus;
		}

		return Math.max(0, Math.min(100, confidence));
	}

	/**
	 * Build human-readable reasoning for the signal
	 */
	private String buildReason(double currentDrop, double priceZScore,
							   double volumeZScore,
							   VolatilityMetrics metrics) {
		return String.format("""
				Drop: %.2f%% from open
				Historical avg: %.2f%% ± %.2f%%
				Price Z-Score: %.2fσ below normal
				Volume Z-Score: %.2fσ %s average
				%s
				""",
				currentDrop,
				metrics.getAvgMaxDropPct(), metrics.getStddevMaxDropPct(),
				priceZScore,
				volumeZScore,
				volumeZScore < 0 ? "below" : "above",
				volumeZScore < 0 ? "Low volume suggests overreaction, not fundamental issue" : ""
		);
	}
}