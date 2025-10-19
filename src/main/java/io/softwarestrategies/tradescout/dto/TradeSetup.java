package io.softwarestrategies.tradescout.dto;

import java.math.BigDecimal;

/**
 * Represents a concrete trade setup ready for execution
 */
public record TradeSetup(
		String symbol,
		BigDecimal entryPrice,
		BigDecimal targetPrice,
		BigDecimal stopPrice,
		Integer positionSize,
		BigDecimal riskAmount,
		BigDecimal profitTarget,
		Double confidence,
		String reasoning
) {

	/**
	 * Calculate risk/reward ratio
	 */
	public double getRiskRewardRatio() {
		if (riskAmount.compareTo(BigDecimal.ZERO) == 0) {
			return 0.0;
		}
		return profitTarget.divide(riskAmount, 2, java.math.RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * Get target percentage gain
	 */
	public double getTargetGainPercent() {
		return targetPrice.subtract(entryPrice)
				.divide(entryPrice, 4, java.math.RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100))
				.doubleValue();
	}

	/**
	 * Get stop loss percentage
	 */
	public double getStopLossPercent() {
		return entryPrice.subtract(stopPrice)
				.divide(entryPrice, 4, java.math.RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100))
				.doubleValue();
	}
}