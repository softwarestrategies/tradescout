package io.softwarestrategies.tradescout.dto;

import io.softwarestrategies.tradescout.domain.PerformanceMetrics;
import io.softwarestrategies.tradescout.domain.Trade;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Comprehensive quarterly performance report
 */
public record QuarterlyReport(
		PerformanceMetrics metrics,
		List<Trade> trades,
		List<String> recommendations,
		TargetAnalysis targetAnalysis
) {

	public record TargetAnalysis(
			boolean onPaceFor15Percent,
			double currentPace,
			double neededMonthlyReturn,
			String assessment
	) {}

	/**
	 * Generate executive summary
	 */
	public String getExecutiveSummary() {
		return String.format("""
		Quarterly Performance Summary
		════════════════════════════════════════
		Period: %s to %s
		
		Trades: %d
		Win Rate: %.2f%%
		Total P&L: $%s
		Return: %.2f%%
		
		Target Assessment: %s
		Projected Annual: %.2f%%
		
		Top Recommendations:
		%s
		════════════════════════════════════════
		""",
				metrics.getPeriodStart(),
				metrics.getPeriodEnd(),
				metrics.getTotalTrades(),
				metrics.getWinRate(),
				metrics.getTotalPnl(),
				metrics.getReturnPercent(),
				targetAnalysis.assessment(),
				metrics.getProjectedAnnualReturn(),
				recommendations.stream()
						.limit(3)
						.map(r -> "  • " + r)
						.collect(Collectors.joining("\n"))
		);
	}
}