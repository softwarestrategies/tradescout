package io.softwarestrategies.tradescout.scheduler;

import io.softwarestrategies.tradescout.service.EmailService;
import io.softwarestrategies.tradescout.service.PerformanceTrackingService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Quartz job that generates and emails quarterly performance reports
 */
@Component
public class QuarterlyReportJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(QuarterlyReportJob.class);

	private final PerformanceTrackingService performanceService;
	private final EmailService emailService;

	public QuarterlyReportJob(
			PerformanceTrackingService performanceService,
			EmailService emailService) {
		this.performanceService = performanceService;
		this.emailService = emailService;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("═══════════════════════════════════════════════════════");
		log.info("Starting Quarterly Report Job");
		log.info("═══════════════════════════════════════════════════════");

		try {
			// Generate quarterly report
			log.info("Generating quarterly report");
			var report = performanceService.generateQuarterlyReport();

			// Send email
			log.info("Sending quarterly report email");
			emailService.sendQuarterlyReport(report);

			// Log summary
			log.info("Quarterly Report Summary:");
			log.info("  Period: {} to {}",
					report.metrics().getPeriodStart(),
					report.metrics().getPeriodEnd());
			log.info("  Total Trades: {}", report.metrics().getTotalTrades());
			log.info("  Win Rate: {}%", String.format("%.1f", report.metrics().getWinRate()));
			log.info("  Total P&L: ${}", report.metrics().getTotalPnl());
			log.info("  Return: {}%", String.format("%.2f", report.metrics().getReturnPercent()));

			log.info("Quarterly report job complete");

		} catch (Exception e) {
			log.error("Quarterly report job failed", e);
			throw new JobExecutionException(e);
		}

		log.info("═══════════════════════════════════════════════════════");
	}
}