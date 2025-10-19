package io.softwarestrategies.tradescout.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a detected trading opportunity signal
 */
public record OpportunitySignal(
		String symbol,
		BigDecimal currentPrice,
		BigDecimal todayOpen,
		BigDecimal todayHigh,
		BigDecimal todayLow,
		Long currentVolume,
		Double currentDropPct,
		Double priceZScore,
		Double volumeZScore,
		Double confidence,
		Boolean isOpportunity,
		String reason,
		HistoricalContext historicalContext,
		Instant timestamp
) {

	public record HistoricalContext(
			Double avgMaxDropPct,
			Double stddevMaxDropPct,
			Long avgVolume,
			Long stddevVolume,
			Double last5DaysAvgChange
	) {}

	/**
	 * Check if this signal meets the minimum confidence threshold
	 */
	public boolean meetsThreshold(double minConfidence) {
		return confidence != null && confidence >= minConfidence;
	}

	/**
	 * Check if volume is unusually low
	 */
	public boolean isLowVolume() {
		return volumeZScore != null && volumeZScore < -1.0;
	}

	/**
	 * Get a human-readable summary
	 */
	public String getSummary() {
		return String.format("OpportunitySignal { %s: %s percent drop on low volume  Confidence: %s  Price Z-Score: %s  Volume Z-Score: %s }",
				symbol, currentDropPct, confidence, priceZScore, volumeZScore);
	}
}