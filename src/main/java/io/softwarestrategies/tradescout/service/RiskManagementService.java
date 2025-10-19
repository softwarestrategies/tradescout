package io.softwarestrategies.tradescout.service;

import io.softwarestrategies.tradescout.config.TradeScoutProperties;
import io.softwarestrategies.tradescout.domain.Trade;
import io.softwarestrategies.tradescout.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Service for enforcing risk management rules
 */
@Service
public class RiskManagementService {

	private static final Logger log = LoggerFactory.getLogger(RiskManagementService.class);

	private final TradeRepository tradeRepository;
	private final TradeScoutProperties properties;

	public RiskManagementService(
			TradeRepository tradeRepository,
			TradeScoutProperties properties) {
		this.tradeRepository = tradeRepository;
		this.properties = properties;
	}

	/**
	 * Check if we can take a new trade based on all risk rules
	 */
	public boolean canTakeNewTrade() {
		var reasons = new java.util.ArrayList<String>();

		if (!checkWeeklyLimit()) {
			reasons.add("Weekly trade limit reached");
		}

		if (!checkMonthlyLimit()) {
			reasons.add("Monthly trade limit reached");
		}

		if (!checkDailyLoss()) {
			reasons.add("Daily loss limit reached");
		}

		if (!checkMonthlyLoss()) {
			reasons.add("Monthly loss limit reached");
		}

		if (!checkConsecutiveLosses()) {
			reasons.add("Too many consecutive losses");
		}

		if (!reasons.isEmpty()) {
			log.warn("Cannot take new trade: {}", String.join(", ", reasons));
			return false;
		}

		return true;
	}

	/**
	 * Check weekly trade limit
	 */
	private boolean checkWeeklyLimit() {
		var maxTrades = properties.getTrading().getRisk().getMaxTradesPerWeek();
		var weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		var weekEnd = weekStart.plusDays(6);

		var tradeCount = tradeRepository.countTradesInPeriod(weekStart, weekEnd);

		log.debug("Weekly trades: {} / {}", tradeCount, maxTrades);
		return tradeCount < maxTrades;
	}

	/**
	 * Check monthly trade limit
	 */
	private boolean checkMonthlyLimit() {
		var maxTrades = properties.getTrading().getRisk().getMaxTradesPerMonth();
		var monthStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
		var monthEnd = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

		var tradeCount = tradeRepository.countTradesInPeriod(monthStart, monthEnd);

		log.debug("Monthly trades: {} / {}", tradeCount, maxTrades);
		return tradeCount < maxTrades;
	}

	/**
	 * Check daily loss limit
	 */
	private boolean checkDailyLoss() {
		var maxLoss = properties.getTrading().getRisk().getMaxDailyLoss();
		var today = LocalDate.now();

		var todayTrades = tradeRepository.findClosedTradesBetween(today, today);
		var dailyPnL = todayTrades.stream()
				.map(Trade::getPnl)
				.filter(pnl -> pnl != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		var withinLimit = dailyPnL.compareTo(maxLoss.negate()) > 0;

		if (!withinLimit) {
			log.warn("Daily loss limit reached: ${}", dailyPnL);
		}

		return withinLimit;
	}

	/**
	 * Check monthly loss limit
	 */
	private boolean checkMonthlyLoss() {
		var maxLoss = properties.getTrading().getRisk().getMaxMonthlyLoss();
		var monthStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
		var monthEnd = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

		var monthTrades = tradeRepository.findClosedTradesBetween(monthStart, monthEnd);
		var monthlyPnL = monthTrades.stream()
				.map(Trade::getPnl)
				.filter(pnl -> pnl != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		var withinLimit = monthlyPnL.compareTo(maxLoss.negate()) > 0;

		if (!withinLimit) {
			log.warn("Monthly loss limit reached: ${}", monthlyPnL);
		}

		return withinLimit;
	}

	/**
	 * Check consecutive losses limit
	 */
	private boolean checkConsecutiveLosses() {
		var maxConsecutive = properties.getTrading().getRisk().getMaxConsecutiveLosses();
		var recentLosers = tradeRepository.findRecentLosers();

		if (recentLosers.isEmpty()) {
			return true;
		}

		// Count consecutive losses from most recent
		var consecutiveLosses = 0;
		for (var trade : recentLosers) {
			if (trade.isLoser()) {
				consecutiveLosses++;
			} else {
				break; // Stop at first winner
			}
		}

		var canTrade = consecutiveLosses < maxConsecutive;

		if (!canTrade) {
			log.warn("Consecutive loss limit reached: {} losses in a row", consecutiveLosses);
		}

		return canTrade;
	}

	/**
	 * Get current risk status summary
	 */
	public String getRiskStatus() {
		return String.format("""
		Risk Management Status
		════════════════════════════════════════
		Weekly Trades: %d / %d
		Monthly Trades: %d / %d
		Daily P&L: $%s
		Monthly P&L: $%s
		Consecutive Losses: %d
		Can Trade: %s
		════════════════════════════════════════
		""",
				getWeeklyTradeCount(),
				properties.getTrading().getRisk().getMaxTradesPerWeek(),
				getMonthlyTradeCount(),
				properties.getTrading().getRisk().getMaxTradesPerMonth(),
				getDailyPnL(),
				getMonthlyPnL(),
				getConsecutiveLosses(),
				canTakeNewTrade() ? "YES" : "NO"
		);
	}

	private long getWeeklyTradeCount() {
		var weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		var weekEnd = weekStart.plusDays(6);
		return tradeRepository.countTradesInPeriod(weekStart, weekEnd);
	}

	private long getMonthlyTradeCount() {
		var monthStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
		var monthEnd = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
		return tradeRepository.countTradesInPeriod(monthStart, monthEnd);
	}

	private BigDecimal getDailyPnL() {
		var today = LocalDate.now();
		var todayTrades = tradeRepository.findClosedTradesBetween(today, today);
		return todayTrades.stream()
				.map(Trade::getPnl)
				.filter(pnl -> pnl != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal getMonthlyPnL() {
		var monthStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
		var monthEnd = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
		var monthTrades = tradeRepository.findClosedTradesBetween(monthStart, monthEnd);
		return monthTrades.stream()
				.map(Trade::getPnl)
				.filter(pnl -> pnl != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private int getConsecutiveLosses() {
		var recentLosers = tradeRepository.findRecentLosers();
		var count = 0;
		for (var trade : recentLosers) {
			if (trade.isLoser()) {
				count++;
			} else {
				break;
			}
		}
		return count;
	}
}