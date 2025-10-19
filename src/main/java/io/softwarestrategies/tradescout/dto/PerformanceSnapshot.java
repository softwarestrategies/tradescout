package io.softwarestrategies.tradescout.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Current performance snapshot
 */
public record PerformanceSnapshot(
		LocalDate asOfDate,
		BigDecimal currentCapital,
		BigDecimal totalPnl,
		Double returnPercent,
		Integer totalTrades,
		Integer openTrades,
		Double winRate,
		Boolean onPaceForTarget,
		Double projectedAnnual,
		String status
) {

	/**
	 * Get status emoji based on performance
	 */
	public String getStatusEmoji() {
		if (onPaceForTarget && winRate >= 60.0) return "🟢";
		if (onPaceForTarget || winRate >= 55.0) return "🟡";
		return "🔴";
	}

	/**
	 * Simple status message
	 */
	public String getStatusMessage() {
		return String.format("%s Peformance as of %s\n", getStatusEmoji(), status);

/*
		return """
            \{getStatusEmoji()} Performance as of \{asOfDate}
            Capital: $\{currentCapital}
            Return: \{returnPercent}%
            Win Rate: \{winRate}%
            Status: \{status}
            """;
*/
	}
}