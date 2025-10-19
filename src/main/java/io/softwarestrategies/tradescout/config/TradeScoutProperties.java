package io.softwarestrategies.tradescout.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

/**
 * Configuration properties for TradeScout application.
 *
 * Binds to properties prefixed with 'tradescout' in application.yml
 */
@Configuration
@ConfigurationProperties(prefix = "tradescout")
public class TradeScoutProperties {

	private Trading trading = new Trading();

	public Trading getTrading() {
		return trading;
	}

	public void setTrading(Trading trading) {
		this.trading = trading;
	}

	public static class Trading {
		private BigDecimal initialCapital;
		private BigDecimal positionSize;
		private BigDecimal targetProfitPerTrade;
		private BigDecimal stopLossPerTrade;
		private Double annualTargetPercent;
		private Double quarterlyTargetPercent;
		private List<String> watchlist;

		private Detection detection = new Detection();
		private Risk risk = new Risk();
		private Rules rules = new Rules();
		private Schedule schedule = new Schedule();
		private Email email = new Email();

		// Getters and setters
		public BigDecimal getInitialCapital() { return initialCapital; }
		public void setInitialCapital(BigDecimal initialCapital) {
			this.initialCapital = initialCapital;
		}

		public BigDecimal getPositionSize() { return positionSize; }
		public void setPositionSize(BigDecimal positionSize) {
			this.positionSize = positionSize;
		}

		public BigDecimal getTargetProfitPerTrade() { return targetProfitPerTrade; }
		public void setTargetProfitPerTrade(BigDecimal targetProfitPerTrade) {
			this.targetProfitPerTrade = targetProfitPerTrade;
		}

		public BigDecimal getStopLossPerTrade() { return stopLossPerTrade; }
		public void setStopLossPerTrade(BigDecimal stopLossPerTrade) {
			this.stopLossPerTrade = stopLossPerTrade;
		}

		public Double getAnnualTargetPercent() { return annualTargetPercent; }
		public void setAnnualTargetPercent(Double annualTargetPercent) {
			this.annualTargetPercent = annualTargetPercent;
		}

		public Double getQuarterlyTargetPercent() { return quarterlyTargetPercent; }
		public void setQuarterlyTargetPercent(Double quarterlyTargetPercent) {
			this.quarterlyTargetPercent = quarterlyTargetPercent;
		}

		public List<String> getWatchlist() { return watchlist; }
		public void setWatchlist(List<String> watchlist) {
			this.watchlist = watchlist;
		}

		public Detection getDetection() { return detection; }
		public void setDetection(Detection detection) { this.detection = detection; }

		public Risk getRisk() { return risk; }
		public void setRisk(Risk risk) { this.risk = risk; }

		public Rules getRules() { return rules; }
		public void setRules(Rules rules) { this.rules = rules; }

		public Schedule getSchedule() { return schedule; }
		public void setSchedule(Schedule schedule) { this.schedule = schedule; }

		public Email getEmail() { return email; }
		public void setEmail(Email email) { this.email = email; }
	}

	public static class Detection {
		private Double minConfidence;
		private Double minPriceZscore;
		private Double minVolumeZscore;
		private Integer lookbackDays;

		public Double getMinConfidence() { return minConfidence; }
		public void setMinConfidence(Double minConfidence) {
			this.minConfidence = minConfidence;
		}

		public Double getMinPriceZscore() { return minPriceZscore; }
		public void setMinPriceZscore(Double minPriceZscore) {
			this.minPriceZscore = minPriceZscore;
		}

		public Double getMinVolumeZscore() { return minVolumeZscore; }
		public void setMinVolumeZscore(Double minVolumeZscore) {
			this.minVolumeZscore = minVolumeZscore;
		}

		public Integer getLookbackDays() { return lookbackDays; }
		public void setLookbackDays(Integer lookbackDays) {
			this.lookbackDays = lookbackDays;
		}
	}

	public static class Risk {
		private Integer maxTradesPerWeek;
		private Integer maxTradesPerMonth;
		private BigDecimal maxDailyLoss;
		private BigDecimal maxMonthlyLoss;
		private Integer maxConsecutiveLosses;

		public Integer getMaxTradesPerWeek() { return maxTradesPerWeek; }
		public void setMaxTradesPerWeek(Integer maxTradesPerWeek) {
			this.maxTradesPerWeek = maxTradesPerWeek;
		}

		public Integer getMaxTradesPerMonth() { return maxTradesPerMonth; }
		public void setMaxTradesPerMonth(Integer maxTradesPerMonth) {
			this.maxTradesPerMonth = maxTradesPerMonth;
		}

		public BigDecimal getMaxDailyLoss() { return maxDailyLoss; }
		public void setMaxDailyLoss(BigDecimal maxDailyLoss) {
			this.maxDailyLoss = maxDailyLoss;
		}

		public BigDecimal getMaxMonthlyLoss() { return maxMonthlyLoss; }
		public void setMaxMonthlyLoss(BigDecimal maxMonthlyLoss) {
			this.maxMonthlyLoss = maxMonthlyLoss;
		}

		public Integer getMaxConsecutiveLosses() { return maxConsecutiveLosses; }
		public void setMaxConsecutiveLosses(Integer maxConsecutiveLosses) {
			this.maxConsecutiveLosses = maxConsecutiveLosses;
		}
	}

	public static class Rules {
		private Boolean noFridayEntries;
		private Boolean noEarningsWeek;
		private Boolean noFomcWeek;
		private Boolean requireManualReview;

		public Boolean getNoFridayEntries() { return noFridayEntries; }
		public void setNoFridayEntries(Boolean noFridayEntries) {
			this.noFridayEntries = noFridayEntries;
		}

		public Boolean getNoEarningsWeek() { return noEarningsWeek; }
		public void setNoEarningsWeek(Boolean noEarningsWeek) {
			this.noEarningsWeek = noEarningsWeek;
		}

		public Boolean getNoFomcWeek() { return noFomcWeek; }
		public void setNoFomcWeek(Boolean noFomcWeek) {
			this.noFomcWeek = noFomcWeek;
		}

		public Boolean getRequireManualReview() { return requireManualReview; }
		public void setRequireManualReview(Boolean requireManualReview) {
			this.requireManualReview = requireManualReview;
		}
	}

	public static class Schedule {
		private String dailyMaintenanceCron;
		private String dailyMaintenanceTimezone;
		private String quarterlyReportCron;

		public String getDailyMaintenanceCron() { return dailyMaintenanceCron; }
		public void setDailyMaintenanceCron(String dailyMaintenanceCron) {
			this.dailyMaintenanceCron = dailyMaintenanceCron;
		}

		public String getDailyMaintenanceTimezone() { return dailyMaintenanceTimezone; }
		public void setDailyMaintenanceTimezone(String dailyMaintenanceTimezone) {
			this.dailyMaintenanceTimezone = dailyMaintenanceTimezone;
		}

		public String getQuarterlyReportCron() { return quarterlyReportCron; }
		public void setQuarterlyReportCron(String quarterlyReportCron) {
			this.quarterlyReportCron = quarterlyReportCron;
		}
	}

	public static class Email {
		private Boolean enabled;
		private String to;
		private String from;
		private Integer alertCooldownMinutes;
		private Boolean includeLinks;

		public Boolean getEnabled() { return enabled; }
		public void setEnabled(Boolean enabled) { this.enabled = enabled; }

		public String getTo() { return to; }
		public void setTo(String to) { this.to = to; }

		public String getFrom() { return from; }
		public void setFrom(String from) { this.from = from; }

		public Integer getAlertCooldownMinutes() { return alertCooldownMinutes; }
		public void setAlertCooldownMinutes(Integer alertCooldownMinutes) {
			this.alertCooldownMinutes = alertCooldownMinutes;
		}

		public Boolean getIncludeLinks() { return includeLinks; }
		public void setIncludeLinks(Boolean includeLinks) {
			this.includeLinks = includeLinks;
		}
	}
}