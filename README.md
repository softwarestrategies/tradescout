# TradeScout 🔍

> Professional trading opportunity scanner by Software Strategies

**Built with Java 25** | Spring Boot 3.4 | PostgreSQL 16 | Docker

## Quick Start
```bash
# 1. Clone and setup
git clone <your-repo>
cd tradescout
cp .env-sample .env-sample
nano .env-sample  # Add your email credentials

# 2. Start everything
./start-tradescout.sh

# 3. Initialize historical data (first time only)
curl -X POST http://localhost:8080/api/maintenance/initialize

# 4. Test manual scan
curl -X POST http://localhost:8080/api/opportunities/scan
```

## What It Does

- 📊 Scans 20+ mega-cap stocks for unusual price movements
- 🎯 Detects anomalies using statistical Z-score analysis
- 📧 Sends email alerts for high-confidence setups (70%+)
- 🛡️ Enforces strict risk management rules
- 📈 Tracks performance with quarterly reports
- ⏰ Runs daily at 7 PM PT automatically

## Architecture
```
Java 25 + Virtual Threads
    ↓
Spring Boot 3.4
    ↓
Yahoo Finance API → Anomaly Detection → Risk Management → Email Alert
    ↓                       ↓                    ↓              ↓
PostgreSQL 16         Z-Score Analysis    Trade Limits    Gmail SMTP
```

## Configuration

Edit `src/main/resources/application.yml`:
```yaml
tradescout:
  trading:
    initial-capital: 58000.00
    position-size: 35000.00
    target-profit-per-trade: 600.00
    annual-target-percent: 15.0
    watchlist:
      - AAPL
      - MSFT
      # ... more
```

## API Endpoints
```bash
GET  /api/health              # System health
GET  /api/opportunities       # Current opportunities
GET  /api/performance/current # Current performance
GET  /api/performance/quarterly # Quarterly report
POST /api/opportunities/scan  # Manual scan
POST /api/maintenance/initialize # Load history (first time)
```

## Development
```bash
# Run locally (no Docker)
./mvnw spring-boot:run

# Run tests
./mvnw test

# Build JAR
./mvnw clean package

# View logs
docker-compose logs -f tradescout
```

## Requirements

- Java 25
- Docker & Docker Compose
- Maven 3.9+
- Gmail account (for alerts)

## Performance Targets

- Annual Return: 15-20%
- Win Rate: 60-65%
- Trades/Month: 4-8
- Risk/Reward: 1.0+

## License

Private use only. © Software Strategies.

## Disclaimer

Trading involves risk. Past performance ≠ future results.