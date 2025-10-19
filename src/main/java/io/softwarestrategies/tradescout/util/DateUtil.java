package io.softwarestrategies.tradescout.util;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Date utility methods for trading calendar
 */
@Component
public class DateUtil {

	/**
	 * Check if date is a Friday
	 */
	public boolean isFriday(LocalDate date) {
		return date.getDayOfWeek() == DayOfWeek.FRIDAY;
	}

	/**
	 * Check if date is a weekend
	 */
	public boolean isWeekend(LocalDate date) {
		var dayOfWeek = date.getDayOfWeek();
		return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
	}

	/**
	 * Get the last trading day (most recent weekday)
	 */
	public LocalDate getLastTradingDay() {
		var date = LocalDate.now();
		while (isWeekend(date)) {
			date = date.minusDays(1);
		}
		return date;
	}

	/**
	 * Get the start date of the current quarter
	 */
	public LocalDate getQuarterStart(LocalDate date) {
		var month = date.getMonth();
		var quarterStartMonth = switch (month) {
			case JANUARY, FEBRUARY, MARCH -> 1;
			case APRIL, MAY, JUNE -> 4;
			case JULY, AUGUST, SEPTEMBER -> 7;
			case OCTOBER, NOVEMBER, DECEMBER -> 10;
		};
		return LocalDate.of(date.getYear(), quarterStartMonth, 1);
	}

	/**
	 * Get the end date of the current quarter
	 */
	public LocalDate getQuarterEnd(LocalDate date) {
		return getQuarterStart(date).plusMonths(3).minusDays(1);
	}

	/**
	 * Get the start date of the current month
	 */
	public LocalDate getMonthStart(LocalDate date) {
		return date.with(TemporalAdjusters.firstDayOfMonth());
	}

	/**
	 * Get the end date of the current month
	 */
	public LocalDate getMonthEnd(LocalDate date) {
		return date.with(TemporalAdjusters.lastDayOfMonth());
	}

	/**
	 * Count trading days between two dates (excludes weekends)
	 */
	public long countTradingDays(LocalDate start, LocalDate end) {
		return start.datesUntil(end.plusDays(1))
				.filter(date -> !isWeekend(date))
				.count();
	}
}