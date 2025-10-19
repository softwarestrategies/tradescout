-- TradeScout Database Schema
-- Created for PostgreSQL 16

-- Stock History Table
CREATE TABLE stock_history (
                               id BIGSERIAL PRIMARY KEY,
                               symbol VARCHAR(10) NOT NULL,
                               trade_date DATE NOT NULL,
                               open_price DECIMAL(10, 2) NOT NULL,
                               high_price DECIMAL(10, 2) NOT NULL,
                               low_price DECIMAL(10, 2) NOT NULL,
                               close_price DECIMAL(10, 2) NOT NULL,
                               volume BIGINT NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT uk_stock_history_symbol_date UNIQUE (symbol, trade_date)
);

CREATE INDEX idx_stock_history_symbol_date ON stock_history(symbol, trade_date);
CREATE INDEX idx_stock_history_date ON stock_history(trade_date);

COMMENT ON TABLE stock_history IS 'Daily OHLC stock price data';

-- Volatility Metrics Table
CREATE TABLE volatility_metrics (
                                    id BIGSERIAL PRIMARY KEY,
                                    symbol VARCHAR(10) NOT NULL,
                                    calculation_date DATE NOT NULL,
                                    lookback_days INTEGER NOT NULL,
                                    avg_daily_range_pct DOUBLE PRECISION,
                                    stddev_daily_range_pct DOUBLE PRECISION,
                                    avg_max_drop_pct DOUBLE PRECISION,
                                    stddev_max_drop_pct DOUBLE PRECISION,
                                    avg_daily_change_pct DOUBLE PRECISION,
                                    stddev_daily_change_pct DOUBLE PRECISION,
                                    avg_volume BIGINT,
                                    stddev_volume BIGINT,
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    CONSTRAINT uk_volatility_metrics UNIQUE (symbol, calculation_date, lookback_days)
);

CREATE INDEX idx_volatility_metrics_symbol ON volatility_metrics(symbol, calculation_date);

COMMENT ON TABLE volatility_metrics IS 'Calculated volatility statistics for anomaly detection';

-- Trades Table
CREATE TABLE trades (
                        id BIGSERIAL PRIMARY KEY,
                        symbol VARCHAR(10) NOT NULL,
                        entry_date DATE NOT NULL,
                        entry_price DECIMAL(10, 2) NOT NULL,
                        target_price DECIMAL(10, 2) NOT NULL,
                        stop_price DECIMAL(10, 2) NOT NULL,
                        position_size INTEGER NOT NULL,
                        entry_reasoning VARCHAR(1000),
                        confidence_score DOUBLE PRECISION,
                        exit_date DATE,
                        exit_price DECIMAL(10, 2),
                        pnl DECIMAL(10, 2),
                        pnl_percent DOUBLE PRECISION,
                        status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
                        exit_reason VARCHAR(50),
                        lessons_learned VARCHAR(2000),
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trades_entry_date ON trades(entry_date);
CREATE INDEX idx_trades_symbol ON trades(symbol);
CREATE INDEX idx_trades_status ON trades(status);

COMMENT ON TABLE trades IS 'Trading positions and performance tracking';

-- Performance Metrics Table
CREATE TABLE performance_metrics (
                                     id BIGSERIAL PRIMARY KEY,
                                     period_type VARCHAR(20) NOT NULL,
                                     period_start DATE NOT NULL,
                                     period_end DATE NOT NULL,
                                     starting_capital DECIMAL(12, 2),
                                     ending_capital DECIMAL(12, 2),
                                     total_trades INTEGER,
                                     winning_trades INTEGER,
                                     losing_trades INTEGER,
                                     win_rate DOUBLE PRECISION,
                                     total_pnl DECIMAL(12, 2),
                                     return_percent DOUBLE PRECISION,
                                     avg_win DECIMAL(10, 2),
                                     avg_loss DECIMAL(10, 2),
                                     largest_win DECIMAL(10, 2),
                                     largest_loss DECIMAL(10, 2),
                                     profit_factor DOUBLE PRECISION,
                                     max_drawdown DECIMAL(10, 2),
                                     max_drawdown_percent DOUBLE PRECISION,
                                     sharpe_ratio DOUBLE PRECISION,
                                     on_pace_for_annual_target BOOLEAN,
                                     projected_annual_return DOUBLE PRECISION,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_performance_period ON performance_metrics(period_type, period_end);

COMMENT ON TABLE performance_metrics IS 'Aggregated performance statistics by time period';

-- Create trigger for updated_at on trades
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_trades_updated_at
    BEFORE UPDATE ON trades
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();