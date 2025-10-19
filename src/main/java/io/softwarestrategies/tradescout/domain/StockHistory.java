package io.softwarestrategies.tradescout.domain;

import static org.yaml.snakeyaml.nodes.Tag.STR;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity representing daily stock price history.
 * Stores OHLC (Open, High, Low, Close) data and volume.
 */
@Entity
@Table(name = "stock_history",
		indexes = {
				@Index(name = "idx_stock_history_symbol_date", columnList = "symbol,trade_date"),
				@Index(name = "idx_stock_history_date", columnList = "trade_date")
		},
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_stock_history_symbol_date",
						columnNames = {"symbol", "trade_date"})
		})
public class StockHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 10)
	private String symbol;

	@Column(name = "trade_date", nullable = false)
	private LocalDate tradeDate;

	@Column(name = "open_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal openPrice;

	@Column(name = "high_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal highPrice;

	@Column(name = "low_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal lowPrice;

	@Column(name = "close_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal closePrice;

	@Column(nullable = false)
	private Long volume;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = Instant.now();
	}

	// Calculated fields (not persisted)

	/**
	 * Calculate daily range as percentage of open price
	 * (high - low) / open * 100
	 */
	@Transient
	public double getDailyRangePercent() {
		if (openPrice.compareTo(BigDecimal.ZERO) == 0) {
			return 0.0;
		}
		return highPrice.subtract(lowPrice)
				.divide(openPrice, 4, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100))
				.doubleValue();
	}

	/**
	 * Calculate max intraday drop from open
	 * (low - open) / open * 100
	 */
	@Transient
	public double getMaxDropPercent() {
		if (openPrice.compareTo(BigDecimal.ZERO) == 0) {
			return 0.0;
		}
		return lowPrice.subtract(openPrice)
				.divide(openPrice, 4, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100))
				.doubleValue();
	}

	/**
	 * Calculate daily change from open to close
	 * (close - open) / open * 100
	 */
	@Transient
	public double getDailyChangePercent() {
		if (openPrice.compareTo(BigDecimal.ZERO) == 0) {
			return 0.0;
		}
		return closePrice.subtract(openPrice)
				.divide(openPrice, 4, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100))
				.doubleValue();
	}

	// Constructors
	public StockHistory() {}

	public StockHistory(String symbol, LocalDate tradeDate, BigDecimal openPrice,
						BigDecimal highPrice, BigDecimal lowPrice,
						BigDecimal closePrice, Long volume) {
		this.symbol = symbol;
		this.tradeDate = tradeDate;
		this.openPrice = openPrice;
		this.highPrice = highPrice;
		this.lowPrice = lowPrice;
		this.closePrice = closePrice;
		this.volume = volume;
	}

	// Getters and setters
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getSymbol() { return symbol; }
	public void setSymbol(String symbol) { this.symbol = symbol; }

	public LocalDate getTradeDate() { return tradeDate; }
	public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }

	public BigDecimal getOpenPrice() { return openPrice; }
	public void setOpenPrice(BigDecimal openPrice) { this.openPrice = openPrice; }

	public BigDecimal getHighPrice() { return highPrice; }
	public void setHighPrice(BigDecimal highPrice) { this.highPrice = highPrice; }

	public BigDecimal getLowPrice() { return lowPrice; }
	public void setLowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; }

	public BigDecimal getClosePrice() { return closePrice; }
	public void setClosePrice(BigDecimal closePrice) { this.closePrice = closePrice; }

	public Long getVolume() { return volume; }
	public void setVolume(Long volume) { this.volume = volume; }

	public Instant getCreatedAt() { return createdAt; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		StockHistory that = (StockHistory) o;
		return Objects.equals(symbol, that.symbol) &&
				Objects.equals(tradeDate, that.tradeDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(symbol, tradeDate);
	}

	@Override
	public String toString() {
		return String.format("StockHistory{symbol='%s', date=%s, close=%s}", symbol, tradeDate, closePrice);
	}
}