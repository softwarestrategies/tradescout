package io.softwarestrategies.tradescout.util;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Statistical utility methods for calculations
 */
@Component
public class StatisticsUtil {

	/**
	 * Calculate mean (average) of a list of doubles
	 */
	public double calculateMean(List<Double> values) {
		if (values == null || values.isEmpty()) {
			return 0.0;
		}
		return values.stream()
				.mapToDouble(Double::doubleValue)
				.average()
				.orElse(0.0);
	}

	/**
	 * Calculate standard deviation
	 */
	public double calculateStandardDeviation(List<Double> values) {
		if (values == null || values.size() < 2) {
			return 0.0;
		}

		var mean = calculateMean(values);
		var variance = values.stream()
				.mapToDouble(v -> Math.pow(v - mean, 2))
				.average()
				.orElse(0.0);

		return Math.sqrt(variance);
	}

	/**
	 * Calculate Z-score for a value given mean and standard deviation
	 */
	public double calculateZScore(double value, double mean, double stdDev) {
		if (stdDev == 0.0) {
			return 0.0;
		}
		return (value - mean) / stdDev;
	}

	/**
	 * Calculate percentile rank of a value in a dataset
	 */
	public double calculatePercentile(double value, List<Double> values) {
		if (values == null || values.isEmpty()) {
			return 0.0;
		}

		long countBelow = values.stream()
				.filter(v -> v < value)
				.count();

		return (double) countBelow / values.size() * 100.0;
	}

	/**
	 * Calculate Sharpe Ratio (risk-adjusted return)
	 * Assumes 2% risk-free rate
	 */
	public double calculateSharpeRatio(List<Double> returns) {
		if (returns == null || returns.size() < 2) {
			return 0.0;
		}

		var riskFreeRate = 0.02 / 252; // Daily risk-free rate (2% annual / 252 trading days)
		var avgReturn = calculateMean(returns);
		var stdDev = calculateStandardDeviation(returns);

		if (stdDev == 0.0) {
			return 0.0;
		}

		return (avgReturn - riskFreeRate) / stdDev;
	}
}