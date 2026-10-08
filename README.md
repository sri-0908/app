# BTCFi Yield Optimizer Agent

An autonomous AI agent built for the **GOAT Network**, designed to maximize Bitcoin yields. The agent continuously monitors various yield-generating protocols and automatically rebalances user portfolios based on risk-adjusted APY thresholds.

## Tech Stack
- **Backend**: Java 21, Spring Boot 3.2.0, Web3j, Spring Scheduler
- **Frontend**: React 18 (Vite), Ethers.js v6
- **Database**: PostgreSQL 15
- **Infrastructure**: Docker, docker-compose

## Features
- **Live Yield Scanning**: Fetches real-time APY from GOAT Network RPC (`getPoolAPY`) every 60 seconds.
- **AI Rule-Based Decision Engine**: Moves user positions only if the yield delta exceeds a threshold (e.g., > 1.5%) and respects risk scoring.
- **Micropayments**: Charges a tiny BTC fee on the GOAT Network after every successful rebalance action.
- **ERC-8004 Identity**: Registers the AI agent natively on-chain as a verifiable actor.
- **Web3 Dashboard**: View your current portfolio, APY, total earnings, and agent transaction history.

## Project Structure
```
btcfi-yield-optimizer/
├── backend/                  # Spring Boot API & Agent Logic
│   ├── src/main/java/com/btcfi/
│   │   ├── config/           # Web3j & CORS config
│   │   ├── controller/       # REST API Endpoints
│   │   ├── dto/              # API Data Transfer Objects
│   │   ├── model/            # JPA Entities (User, Pool, Tx)
│   │   ├── repository/       # PostgreSQL Repositories
│   │   ├── scheduler/        # 60s Cron Job
│   │   └── service/          # Core Agent AI logic
│   └── pom.xml
├── frontend/                 # React UI
│   ├── src/
│   │   ├── components/       # Dashboard UI
│   │   ├── services/         # API & Ethers logic
│   │   └── App.jsx           # Main React component
│   └── package.json
├── docker-compose.yml        # Multi-container orchestration
└── README.md
```

## Quick Start (Docker)
1. Install [Docker](https://docs.docker.com/get-docker/).
2. From the root directory, start the stack:
   ```bash
   docker-compose up --build -d
   ```
3. Open your browser:
   - **Frontend UI**: http://localhost:3000
   - **Backend API**: http://localhost:8080/api/pools

## Development Setup (Manual)
### Database
1. Run PostgreSQL locally or via docker:
   ```bash
   docker run --name btcfi-db -e POSTGRES_PASSWORD=btcfi_password -e POSTGRES_USER=btcfi_user -e POSTGRES_DB=btcfi_db -p 5432:5432 -d postgres:15-alpine
   ```

### Backend
1. Ensure Java 21+ and Maven are installed.
2. Navigate to `/backend`.
3. Set your `.env` variables or use the default `application.yml` fallbacks.
4. Run:
   ```bash
   ./mvnw spring-boot:run
   ```

### Frontend
1. Ensure Node.js 18+ is installed.
2. Navigate to `/frontend`.
3. Install dependencies and run:
   ```bash
   npm install
   npm run dev
   ```

## Agent Configuration
The agent's logic can be tuned in `backend/src/main/resources/application.yml`:
- `agent.rebalance.apy-threshold`: The APY % difference required to trigger a move (default: 1.5%).
- `agent.rebalance.scan-interval-ms`: Frequency of blockchain polling (default: 60000ms).
- `goat.network.agent-private-key`: Your agent's private key for signing rebalance and identity TXs.
