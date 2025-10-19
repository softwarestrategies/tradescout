#!/bin/bash

echo "═══════════════════════════════════════════════════════════"
echo "    _____              _       _____                 _     "
echo "   |_   _| __ __ _  __| | ___ / ____|               | |    "
echo "     | || '__/ _\` |/ _\` |/ _ \\___ \ ___ ___  _   _| |_   "
echo "     | || | | (_| | (_| |  __/___) / __/ _ \| | | | __|  "
echo "     |_||_|  \__,_|\__,_|\___|____/ \___\___/|_| |_|\__|  "
echo "                                                           "
echo "       Professional Trading Opportunity Scanner           "
echo "                 by Software Strategies                   "
echo "═══════════════════════════════════════════════════════════"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if .env-sample exists
if [ ! -f .env-sample ]; then
    echo -e "${RED}❌ .env file not found!${NC}"
    echo -e "${YELLOW}📝 Creating from template...${NC}"
    cp .env-sample.example .env-sample
    echo -e "${GREEN}✅ .env created${NC}"
    echo ""
    echo -e "${YELLOW}⚠️  Please edit .env with your email credentials:${NC}"
    echo "   nano .env"
    echo ""
    exit 1
fi

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running${NC}"
    echo "Please start Docker Desktop and try again"
    exit 1
fi

echo ""
echo -e "${GREEN}🐳 Starting PostgreSQL...${NC}"
docker-compose up -d postgres

echo -e "${YELLOW}⏳ Waiting for database to be ready...${NC}"
sleep 5

# Wait for postgres to be ready
MAX_TRIES=30
TRIES=0
until docker-compose exec -T postgres pg_isready -U tradescout_user -d tradescout > /dev/null 2>&1; do
    TRIES=$((TRIES+1))
    if [ $TRIES -eq $MAX_TRIES ]; then
        echo -e "${RED}❌ Database failed to start${NC}"
        docker-compose logs postgres
        exit 1
    fi
    echo "   Waiting for database... ($TRIES/$MAX_TRIES)"
    sleep 2
done

echo -e "${GREEN}✅ Database is ready!${NC}"
echo ""
echo -e "${GREEN}🚀 Starting TradeScout application...${NC}"
docker-compose up -d tradescout

echo -e "${YELLOW}⏳ Waiting for application to start...${NC}"
sleep 5

# Wait for application to be ready
MAX_TRIES=30
TRIES=0
until curl -s http://localhost:8080/api/actuator/health > /dev/null 2>&1; do
    TRIES=$((TRIES+1))
    if [ $TRIES -eq $MAX_TRIES ]; then
        echo -e "${RED}❌ Application failed to start${NC}"
        echo "Viewing logs..."
        docker-compose logs tradescout
        exit 1
    fi
    echo "   Waiting for application... ($TRIES/$MAX_TRIES)"
    sleep 2
done

echo ""
echo "═══════════════════════════════════════════════════════════"
echo -e "${GREEN}✅ TradeScout is now running!${NC}"
echo ""
echo "🌐 Services:"
echo "   Application:  http://localhost:8080/api"
echo "   Health Check: http://localhost:8080/api/actuator/health"
echo "   Metrics:      http://localhost:8080/api/actuator/metrics"
echo ""
echo "📊 Useful Commands:"
echo "   View logs:         docker-compose logs -f tradescout"
echo "   Stop all:          docker-compose down"
echo "   Restart:           docker-compose restart tradescout"
echo "   View DB (pgAdmin): docker-compose --profile tools up -d pgadmin"
echo ""
echo "🔍 First Time Setup:"
echo "   Initialize data:   curl -X POST http://localhost:8080/api/maintenance/initialize"
echo "   Manual scan:       curl -X POST http://localhost:8080/api/opportunities/scan"
echo "   Check health:      curl http://localhost:8080/api/health"
echo "═══════════════════════════════════════════════════════════"