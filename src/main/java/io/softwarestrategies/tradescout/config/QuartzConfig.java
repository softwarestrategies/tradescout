package io.softwarestrategies.tradescout.config;

import io.softwarestrategies.tradescout.scheduler.DailyMaintenanceJob;
import io.softwarestrategies.tradescout.scheduler.QuarterlyReportJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz scheduler configuration
 */
@Configuration
public class QuartzConfig {

	private final TradeScoutProperties properties;

	public QuartzConfig(TradeScoutProperties properties) {
		this.properties = properties;
	}

	/**
	 * Daily maintenance job - runs at 7 PM Pacific daily
	 */
	@Bean
	public JobDetail dailyMaintenanceJobDetail() {
		return JobBuilder.newJob(DailyMaintenanceJob.class)
				.withIdentity("dailyMaintenanceJob")
				.withDescription("Daily market data update and maintenance")
				.storeDurably()
				.build();
	}

	@Bean
	public Trigger dailyMaintenanceTrigger() {
		var cron = properties.getTrading().getSchedule().getDailyMaintenanceCron();
		var timezone = properties.getTrading().getSchedule().getDailyMaintenanceTimezone();

		return TriggerBuilder.newTrigger()
				.forJob(dailyMaintenanceJobDetail())
				.withIdentity("dailyMaintenanceTrigger")
				.withDescription("Triggers daily maintenance at 7 PM Pacific")
				.withSchedule(
						CronScheduleBuilder.cronSchedule(cron)
								.inTimeZone(java.util.TimeZone.getTimeZone(timezone))
				)
				.build();
	}

	/**
	 * Quarterly report job - runs first day of quarter at 8 AM
	 */
	@Bean
	public JobDetail quarterlyReportJobDetail() {
		return JobBuilder.newJob(QuarterlyReportJob.class)
				.withIdentity("quarterlyReportJob")
				.withDescription("Generate and email quarterly performance report")
				.storeDurably()
				.build();
	}

	@Bean
	public Trigger quarterlyReportTrigger() {
		var cron = properties.getTrading().getSchedule().getQuarterlyReportCron();

		return TriggerBuilder.newTrigger()
				.forJob(quarterlyReportJobDetail())
				.withIdentity("quarterlyReportTrigger")
				.withDescription("Triggers quarterly report on first day of quarter")
				.withSchedule(CronScheduleBuilder.cronSchedule(cron))
				.build();
	}
}