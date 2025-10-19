package io.softwarestrategies.tradescout.scheduler;

import io.softwarestrategies.tradescout.service.MarketDataService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Quartz job that runs daily maintenance tasks
 * - Updates today's stock data
 * - Recalculates volatility metrics
 * - Cleans up old data
 */
@Component
public class DailyMaintenanceJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(DailyMaintenanceJob.class);

	private final MarketDataService marketDataService;

	public DailyMaintenanceJob(MarketDataService marketDataService) {
		this.marketDataService = marketDataService;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("═══════════════════════════════════════════════════════");
		log.info("Starting Daily Maintenance Job");
		log.info("═══════════════════════════════════════════════════════");

		var startTime = System.currentTimeMillis();

		try {
			// Update today's market data
			log.info("Step 1: Updating today's market data");
			marketDataService.updateTodaysData();

			// Recalculate volatility metrics
			log.info("Step 2: Recalculating volatility metrics");
			marketDataService.calculateMetricsForAll();

			// Clean up old data (keep 365 days)
			log.info("Step 3: Cleaning up old data");
			marketDataService.cleanupOldData(365);

			var duration = (System.currentTimeMillis() - startTime) / 1000;
			log.info("Daily maintenance complete in {}s", duration);

		} catch (Exception e) {
			log.error("Daily maintenance job failed", e);
			throw new JobExecutionException(e);
		}

		log.info("═══════════════════════════════════════════════════════");
	}
}