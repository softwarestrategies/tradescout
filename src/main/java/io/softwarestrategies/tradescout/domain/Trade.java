package io.softwarestrategies.tradescout.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity representing a trading position.
 * Tracks entry, exit, and P&L for each trade.
 */
@Entity
@Table(name = "trades",
		indexes = {
				@Index(name = "idx_trades_entry_date", columnList = "entry_date"),
				@Index(name = "idx_trades_symbol", columnList = "symbol"),
				@Index(name = "idx_trades_status", columnList = "status")
		})
public class Trade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 10)
	private String symbol;

	@Column(name = "entry_date", nullable = false)
	private LocalDate entryDate;

	@Column(name = "entry_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal entryPrice;

	@Column(name = "target_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal targetPrice;

	@Column(name = "stop_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal stopPrice;

	@Column(name = "position_size", nullable = false)
	private Integer positionSize;

	@Column(name = "entry_reasoning", length = 1000)
	private String entryReasoning;

	@Column(name = "confidence_score")
	private Double confidenceScore;

	// Exit information
	@Column(name = "exit_date")
	private LocalDate exitDate;

	@Column(name = "exit_price", precision = 10, scale = 2)
	private BigDecimal exitPrice;

	@Column(name = "pnl", precision = 10, scale = 2)
	private BigDecimal pnl;

	@Column(name = "pnl_percent")
	private Double pnlPercent;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private TradeStatus status = TradeStatus.OPEN;

	@Enumerated(EnumType.STRING)
	@Column(name = "exit_reason", length = 50)
	private ExitReason exitReason;

	@Column(name = "lessons_learned", length = 2000)
	private String lessonsLearned;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	protected void onCreate() {
		createdAt = Instant.now();
		updatedAt = Instant.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = Instant.now();
	}

	public enum TradeStatus {
		OPEN, CLOSED, CANCELLED
	}

	public enum ExitReason {
		TARGET_HIT, STOP_HIT, TIME_EXIT, MANUAL_EXIT, CANCELLED
	}

	/**
	 * Calculate P&L when trade is closed
	 */
	public void calculatePnL() {
		if (exitPrice != null && entryPrice != null && positionSize != null) {
			var priceChange = exitPrice.subtract(entryPrice);
			this.pnl = priceChange.multiply(BigDecimal.valueOf(positionSize));
			this.pnlPercent = priceChange
					.divide(entryPrice, 4, RoundingMode.HALF_UP)
					.multiply(BigDecimal.valueOf(100))
					.doubleValue();
		}
	}

	/**
	 * Close the trade with exit information
	 */
	public void close(BigDecimal exitPrice, ExitReason reason) {
		this.exitPrice = exitPrice;
		this.exitDate = LocalDate.now();
		this.exitReason = reason;
		this.status = TradeStatus.CLOSED;
		calculatePnL();
	}

	/**
	 * Check if trade is a winner
	 */
	@Transient
	public boolean isWinner() {
		return pnl != null && pnl.compareTo(BigDecimal.ZERO) > 0;
	}

	/**
	 * Check if trade is a loser
	 */
	@Transient
	public boolean isLoser() {
		return pnl != null && pnl.compareTo(BigDecimal.ZERO) < 0;
	}

	// Constructors
	public Trade() {}

	public Trade(String symbol, LocalDate entryDate, BigDecimal entryPrice,
				 BigDecimal targetPrice, BigDecimal stopPrice, Integer positionSize) {
		this.symbol = symbol;
		this.entryDate = entryDate;
		this.entryPrice = entryPrice;
		this.targetPrice = targetPrice;
		this.stopPrice = stopPrice;
		this.positionSize = positionSize;
	}

	// Getters and setters
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getSymbol() { return symbol; }
	public void setSymbol(String symbol) { this.symbol = symbol; }

	public LocalDate getEntryDate() { return entryDate; }
	public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

	public BigDecimal getEntryPrice() { return entryPrice; }
	public void setEntryPrice(BigDecimal entryPrice) { this.entryPrice = entryPrice; }

	public BigDecimal getTargetPrice() { return targetPrice; }
	public void setTargetPrice(BigDecimal targetPrice) {
		this.targetPrice = targetPrice;
	}

	public BigDecimal getStopPrice() { return stopPrice; }
	public void setStopPrice(BigDecimal stopPrice) { this.stopPrice = stopPrice; }

	public Integer getPositionSize() { return positionSize; }
	public void setPositionSize(Integer positionSize) {
		this.positionSize = positionSize;
	}

	public String getEntryReasoning() { return entryReasoning; }
	public void setEntryReasoning(String entryReasoning) {
		this.entryReasoning = entryReasoning;
	}

	public Double getConfidenceScore() { return confidenceScore; }
	public void setConfidenceScore(Double confidenceScore) {
		this.confidenceScore = confidenceScore;
	}

	public LocalDate getExitDate() { return exitDate; }
	public void setExitDate(LocalDate exitDate) { this.exitDate = exitDate; }

	public BigDecimal getExitPrice() { return exitPrice; }
	public void setExitPrice(BigDecimal exitPrice) {
		this.exitPrice = exitPrice;
		calculatePnL();
	}

	public BigDecimal getPnl() { return pnl; }
	public void setPnl(BigDecimal pnl) { this.pnl = pnl; }

	public Double getPnlPercent() { return pnlPercent; }
	public void setPnlPercent(Double pnlPercent) { this.pnlPercent = pnlPercent; }

	public TradeStatus getStatus() { return status; }
	public void setStatus(TradeStatus status) { this.status = status; }

	public ExitReason getExitReason() { return exitReason; }
	public void setExitReason(ExitReason exitReason) {
		this.exitReason = exitReason;
	}

	public String getLessonsLearned() { return lessonsLearned; }
	public void setLessonsLearned(String lessonsLearned) {
		this.lessonsLearned = lessonsLearned;
	}

	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Trade trade = (Trade) o;
		return Objects.equals(id, trade.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return String.format("Trade{id='%s', symbol=%s, entryPrice=%s, status=%s}", symbol, entryPrice, entryPrice, status);
	}
}