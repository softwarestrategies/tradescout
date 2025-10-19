package io.softwarestrategies.tradescout.service;

import io.softwarestrategies.tradescout.config.TradeScoutProperties;
import io.softwarestrategies.tradescout.dto.OpportunitySignal;
import io.softwarestrategies.tradescout.dto.TradeSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing trading opportunities and alerts
 */
@Service
public class OpportunityService {

	private static final Logger log = LoggerFactory.getLogger(OpportunityService.class);

	// Track last alert time per symbol to avoid spam
	private final Map<String, Instant> lastAlertTime = new HashMap<>();

	private final AnomalyDetectionService anomalyDetectionService;
	private final EmailService emailService;
	private final RiskManagementService riskManagementService;
	private final TradeScoutProperties properties;

	public OpportunityService(
			AnomalyDetectionService anomalyDetectionService,
			EmailService emailService,
			RiskManagementService riskManagementService,
			TradeScoutProperties properties) {
		this.anomalyDetectionService = anomalyDetectionService;
		this.emailService = emailService;
		this.riskManagementService = riskManagementService;
		this.properties = properties;
	}

	/**
	 * Scan for opportunities and send alerts
	 */
	public List<OpportunitySignal> scanAndAlert() {
		log.info("Starting opportunity scan");

		var opportunities = anomalyDetectionService.scanForOpportunities();

		if (opportunities.isEmpty()) {
			log.info("No opportunities found");
			return opportunities;
		}

		// Filter by trading rules
		var filtered = opportunities.stream()
				.filter(this::passesRules)
				.filter(this::shouldAlert)
				.toList();

		log.info("{} opportunities passed filters", filtered.size());

		// Send alerts
		for (var opportunity : filtered) {
			try {
				if (properties.getTrading().getEmail().getEnabled()) {
					var tradeSetup = generateTradeSetup(opportunity);
					emailService.sendOpportunityAlert(opportunity, tradeSetup);
					lastAlertTime.put(opportunity.symbol(), Instant.now());
					log.info("Alert sent for {}", opportunity.symbol());
				}
			} catch (Exception e) {
				log.error("Failed to send alert for {}: {}",
						opportunity.symbol(), e.getMessage());
			}
		}

		return filtered;
	}

	/**
	 * Check if opportunity passes trading rules
	 */
	private boolean passesRules(OpportunitySignal signal) {
		var rules = properties.getTrading().getRules();

		// Check if it's Friday and we don't trade Fridays
		if (rules.getNoFridayEntries()) {
			var today = LocalDate.now(ZoneId.of(
					properties.getTrading().getSchedule().getDailyMaintenanceTimezone()
			));
			if (today.getDayOfWeek() == DayOfWeek.FRIDAY) {
				log.debug("Skipping {} - no Friday entries", signal.symbol());
				return false;
			}
		}

		// Check risk management limits
		if (!riskManagementService.canTakeNewTrade()) {
			log.debug("Skipping {} - risk limits reached", signal.symbol());
			return false;
		}

		return true;
	}

	/**
	 * Check if we should alert for this opportunity (cooldown period)
	 */
	private boolean shouldAlert(OpportunitySignal signal) {
		var lastAlert = lastAlertTime.get(signal.symbol());

		if (lastAlert == null) {
			return true;
		}

		var cooldownMinutes = properties.getTrading().getEmail().getAlertCooldownMinutes();
		var minutesSinceLastAlert = (Instant.now().getEpochSecond() - lastAlert.getEpochSecond()) / 60;

		if (minutesSinceLastAlert < cooldownMinutes) {
			log.debug("Skipping {} - alert cooldown active ({} min remaining)",
					signal.symbol(), cooldownMinutes - minutesSinceLastAlert);
			return false;
		}

		return true;
	}

	/**
	 * Generate a concrete trade setup from an opportunity signal
	 */
	public TradeSetup generateTradeSetup(OpportunitySignal signal) {
		var entryPrice = signal.currentPrice();
		var positionSize = calculatePositionSize(entryPrice);

		// Calculate target and stop based on configured amounts
		var targetProfit = properties.getTrading().getTargetProfitPerTrade();
		var stopLoss = properties.getTrading().getStopLossPerTrade();

		var pricePerShare = entryPrice;
		var targetGainPerShare = targetProfit.divide(
				BigDecimal.valueOf(positionSize), 2, RoundingMode.HALF_UP
		);
		var stopLossPerShare = stopLoss.divide(
				BigDecimal.valueOf(positionSize), 2, RoundingMode.HALF_UP
		);

		var targetPrice = pricePerShare.add(targetGainPerShare);
		var stopPrice = pricePerShare.subtract(stopLossPerShare);

		var reasoning = String.format("""
		Anomaly detected with %.0f%% confidence
		
		Analysis:
		- Price dropped %.2f%% from open
		- Z-Score: %.2fσ (bottom %.1f%% of days)
		- Volume: %d (%.2fσ below average)
		
		Historical Context:
		- Typical drop: %.2f%% ± %.2f%%
		- Average volume: %d
		
		This appears to be an overreaction on thin trading volume.
		Manual review required before executing trade.
		""",
				signal.confidence(),
				signal.currentDropPct(),
				signal.priceZScore(),
				getPercentile(signal.priceZScore()),
				signal.currentVolume(),
				signal.volumeZScore(),
				signal.historicalContext().avgMaxDropPct(),
				signal.historicalContext().stddevMaxDropPct(),
				signal.historicalContext().avgVolume()
		);

		return new TradeSetup(
				signal.symbol(),
				entryPrice,
				targetPrice,
				stopPrice,
				positionSize,
				stopLoss,
				targetProfit,
				signal.confidence(),
				reasoning
		);
	}

	/**
	 * Calculate position size based on entry price and configured position size
	 */
	private int calculatePositionSize(BigDecimal entryPrice) {
		var positionSize = properties.getTrading().getPositionSize();
		return positionSize.divide(entryPrice, 0, RoundingMode.DOWN).intValue();
	}

	/**
	 * Get approximate percentile from Z-score
	 */
	private double getPercentile(double zScore) {
		if (zScore < -3.0) return 0.1;
		if (zScore < -2.5) return 0.6;
		if (zScore < -2.0) return 2.5;
		if (zScore < -1.5) return 7.0;
		return 16.0;
	}
}