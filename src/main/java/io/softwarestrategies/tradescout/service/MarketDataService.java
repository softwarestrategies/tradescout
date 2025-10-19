package io.softwarestrategies.tradescout.service;

import io.softwarestrategies.tradescout.config.TradeScoutProperties;
import io.softwarestrategies.tradescout.domain.StockHistory;
import io.softwarestrategies.tradescout.domain.VolatilityMetrics;
import io.softwarestrategies.tradescout.repository.StockHistoryRepository;
import io.softwarestrategies.tradescout.repository.VolatilityMetricsRepository;
import io.softwarestrategies.tradescout.util.StatisticsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for fetching and storing market data from Yahoo Finance
 */
@Service
public class MarketDataService {

	private static final Logger log = LoggerFactory.getLogger(MarketDataService.class);

	private final StockHistoryRepository stockHistoryRepository;
	private final VolatilityMetricsRepository volatilityMetricsRepository;
	private final StatisticsUtil statisticsUtil;
	private final TradeScoutProperties properties;

	public MarketDataService(
			StockHistoryRepository stockHistoryRepository,
			VolatilityMetricsRepository volatilityMetricsRepository,
			StatisticsUtil statisticsUtil,
			TradeScoutProperties properties) {
		this.stockHistoryRepository = stockHistoryRepository;
		this.volatilityMetricsRepository = volatilityMetricsRepository;
		this.statisticsUtil = statisticsUtil;
		this.properties = properties;
	}

	/**
	 * Load initial historical data for all watchlist stocks
	 */
	@Transactional
	public void loadInitialData() {
		var watchlist = properties.getTrading().getWatchlist();
		var lookbackDays = properties.getTrading().getDetection().getLookbackDays();

		log.info("Loading initial data for {} symbols, {} days back",
				watchlist.size(), lookbackDays);

		var startTime = System.currentTimeMillis();
		var successCount = 0;
		var failCount = 0;

		for (int i = 0; i < watchlist.size(); i++) {
			var symbol = watchlist.get(i);
			log.info("[{}/{}] Loading {}", i + 1, watchlist.size(), symbol);

			try {
				loadHistoryForSymbol(symbol, lookbackDays);
				successCount++;

				// Rate limiting - be nice to Yahoo Finance
				Thread.sleep(250);
			} catch (Exception e) {
				log.error("Failed to load data for {}: {}", symbol, e.getMessage());
				failCount++;
			}
		}

		var duration = (System.currentTimeMillis() - startTime) / 1000;
		log.info("Initial data load complete: {} success, {} failed, {}s",
				successCount, failCount, duration);

		// Calculate initial metrics
		calculateMetricsForAll();
	}

	/**
	 * Load historical data for a single symbol
	 */
	@Transactional
	public void loadHistoryForSymbol(String symbol, int daysBack) throws Exception {
		var from = Calendar.getInstance();
		from.add(Calendar.DAY_OF_YEAR, -daysBack);

		var to = Calendar.getInstance();

		log.debug("Fetching {} days of history for {}", daysBack, symbol);

		var stock = YahooFinance.get(symbol, from, to, Interval.DAILY);
		var history = stock.getHistory();

		if (history == null || history.isEmpty()) {
			throw new RuntimeException("No data returned for {%s}".formatted(symbol));
		}

		var saved = 0;
		for (var quote : history) {
			var date = LocalDate.ofInstant(
					quote.getDate().toInstant(),
					ZoneId.systemDefault()
			);

			var existing = stockHistoryRepository.findBySymbolAndTradeDate(symbol, date);

			if (existing.isEmpty()) {
				var stockHistory = new StockHistory(
						symbol,
						date,
						quote.getOpen(),
						quote.getHigh(),
						quote.getLow(),
						quote.getClose(),
						quote.getVolume()
				);

				stockHistoryRepository.save(stockHistory);
				saved++;
			}
		}

		log.debug("Saved {} new records for {}", saved, symbol);
	}

	/**
	 * Update today's data for all watchlist stocks
	 */
	@Transactional
	public void updateTodaysData() {
		var watchlist = properties.getTrading().getWatchlist();

		log.info("Updating today's data for {} symbols", watchlist.size());

		var successCount = 0;
		var skipCount = 0;

		for (var symbol : watchlist) {
			try {
				var stock = YahooFinance.get(symbol);
				var quote = stock.getQuote();

				if (quote.getPrice() == null || quote.getPrice().compareTo(BigDecimal.ZERO) == 0) {
					log.debug("Skipping {} - no valid price data", symbol);
					skipCount++;
					continue;
				}

				var today = LocalDate.now();
				var existing = stockHistoryRepository.findBySymbolAndTradeDate(symbol, today);

				StockHistory stockHistory;
				if (existing.isPresent()) {
					stockHistory = existing.get();
					stockHistory.setOpenPrice(quote.getOpen());
					stockHistory.setHighPrice(quote.getDayHigh());
					stockHistory.setLowPrice(quote.getDayLow());
					stockHistory.setClosePrice(quote.getPrice());
					stockHistory.setVolume(quote.getVolume());
				} else {
					stockHistory = new StockHistory(
							symbol,
							today,
							quote.getOpen(),
							quote.getDayHigh(),
							quote.getDayLow(),
							quote.getPrice(),
							quote.getVolume()
					);
				}

				stockHistoryRepository.save(stockHistory);
				successCount++;

				Thread.sleep(200); // Rate limiting

			} catch (Exception e) {
				log.error("Failed to update {} today's data: {}", symbol, e.getMessage());
			}
		}

		log.info("Today's data update complete: {} updated, {} skipped",
				successCount, skipCount);
	}

	/**
	 * Calculate volatility metrics for all watchlist stocks
	 */
	@Transactional
	public void calculateMetricsForAll() {
		var watchlist = properties.getTrading().getWatchlist();
		var lookbackDays = properties.getTrading().getDetection().getLookbackDays();

		log.info("Calculating volatility metrics for {} symbols", watchlist.size());

		for (var symbol : watchlist) {
			try {
				calculateMetricsForSymbol(symbol, lookbackDays);
			} catch (Exception e) {
				log.error("Failed to calculate metrics for {}: {}", symbol, e.getMessage());
			}
		}

		log.info("Metrics calculation complete");
	}

	/**
	 * Calculate volatility metrics for a single symbol
	 */
	@Transactional
	public void calculateMetricsForSymbol(String symbol, int lookbackDays) {
		var startDate = LocalDate.now().minusDays(lookbackDays);
		var history = stockHistoryRepository.findRecentHistory(symbol, startDate);

		if (history.isEmpty()) {
			log.warn("No historical data found for {}", symbol);
			return;
		}

		// Calculate statistics
		var dailyRanges = new ArrayList<Double>();
		var maxDrops = new ArrayList<Double>();
		var dailyChanges = new ArrayList<Double>();
		var volumes = new ArrayList<Long>();

		for (var day : history) {
			dailyRanges.add(day.getDailyRangePercent());
			maxDrops.add(day.getMaxDropPercent());
			dailyChanges.add(day.getDailyChangePercent());
			volumes.add(day.getVolume());
		}

		var metrics = new VolatilityMetrics(symbol, LocalDate.now(), lookbackDays);

		// Price metrics
		metrics.setAvgDailyRangePct(statisticsUtil.calculateMean(dailyRanges));
		metrics.setStddevDailyRangePct(statisticsUtil.calculateStandardDeviation(dailyRanges));

		metrics.setAvgMaxDropPct(statisticsUtil.calculateMean(maxDrops));
		metrics.setStddevMaxDropPct(statisticsUtil.calculateStandardDeviation(maxDrops));

		metrics.setAvgDailyChangePct(statisticsUtil.calculateMean(dailyChanges));
		metrics.setStddevDailyChangePct(statisticsUtil.calculateStandardDeviation(dailyChanges));

		// Volume metrics
		var avgVolume = volumes.stream()
				.mapToLong(Long::longValue)
				.average()
				.orElse(0.0);
		metrics.setAvgVolume((long) avgVolume);

		var volumeDoubles = volumes.stream()
				.map(Long::doubleValue)
				.collect(Collectors.toList());
		var stddevVolume = statisticsUtil.calculateStandardDeviation(volumeDoubles);
		metrics.setStddevVolume((long) stddevVolume);

		// Save or update
		var existing = volatilityMetricsRepository.findBySymbolAndCalculationDateAndLookbackDays(
				symbol, LocalDate.now(), lookbackDays
		);

		if (existing.isPresent()) {
			var existingMetrics = existing.get();
			existingMetrics.setAvgDailyRangePct(metrics.getAvgDailyRangePct());
			existingMetrics.setStddevDailyRangePct(metrics.getStddevDailyRangePct());
			existingMetrics.setAvgMaxDropPct(metrics.getAvgMaxDropPct());
			existingMetrics.setStddevMaxDropPct(metrics.getStddevMaxDropPct());
			existingMetrics.setAvgDailyChangePct(metrics.getAvgDailyChangePct());
			existingMetrics.setStddevDailyChangePct(metrics.getStddevDailyChangePct());
			existingMetrics.setAvgVolume(metrics.getAvgVolume());
			existingMetrics.setStddevVolume(metrics.getStddevVolume());
			volatilityMetricsRepository.save(existingMetrics);
		} else {
			volatilityMetricsRepository.save(metrics);
		}

		log.debug("Calculated metrics for {}: avgDrop={}%, stdDev={}%",
				symbol,
				String.format("%.2f", metrics.getAvgMaxDropPct()),
				String.format("%.2f", metrics.getStddevMaxDropPct()));
	}

	/**
	 * Clean up old historical data beyond retention period
	 */
	@Transactional
	public void cleanupOldData(int retentionDays) {
		var cutoffDate = LocalDate.now().minusDays(retentionDays);

		log.info("Cleaning up data older than {}", cutoffDate);

		var symbols = stockHistoryRepository.findAllDistinctSymbols();

		for (var symbol : symbols) {
			stockHistoryRepository.deleteBySymbolAndTradeDateBefore(symbol, cutoffDate);
		}

		volatilityMetricsRepository.deleteByCalculationDateBefore(cutoffDate.minusDays(30));

		log.info("Cleanup complete");
	}
}