# Feathered OOF Bird
PayPal clone for portfolio

## Overview

This is a full-stack PayPal-like transaction management system built with:
- **Backend**: Spring Boot 3.2 with Java 17
- **Frontend**: Svelte with Vite and Flowbite-Svelte UI components
- **Databases**: PostgreSQL (transactional data), MongoDB (document storage), Valkey (Redis alternative for caching)
- **AI**: Spring AI for generative AI capabilities
- **Containerization**: Docker Compose for easy deployment

## Features

- ✅ Transaction CRUD operations (Create, Read, Update, Delete)
- ✅ REST API with Spring Boot
- ✅ Auth0-backed JWT protection for `/api/**`
- ✅ Responsive UI with Svelte and Flowbite-Svelte
- ✅ PostgreSQL for write-optimized transaction storage
- ✅ MongoDB for read-optimized transaction queries
- ✅ Valkey (Redis-compatible) for caching
- ✅ Spring AI integration for generative AI support
- ✅ Docker Compose for easy setup

## Prerequisites

- Java 17 or higher
- Maven 3.9+
- Node.js 20+
- Docker and Docker Compose (for containerized deployment)

## Quick Start

### Option 1: Using Docker Compose (Recommended)

1. Clone the repository:
```bash
git clone https://github.com/JacobKurtzbergsNickname/feathered-oof-bird.git
cd feathered-oof-bird
```

2. Start all services with Docker Compose:
```bash
docker compose --profile app up -d
```

This will start:
- PostgreSQL on port 5432
- MongoDB on port 27017
- Valkey on port 6379
- Backend API on port 8080
- Frontend on port 5173

3. Access the application:
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080/api/transactions

### Option 2: Local Development

#### 1. Start Infrastructure Services

Start only the databases and cache:
```bash
docker compose up -d postgres mongodb valkey
```

#### Optional: Use the dev helper script

This script starts the databases, builds the frontend, copies it into the backend static assets, and runs the backend.

```bash
./scripts/dev-server.sh
```

On Windows PowerShell:

```powershell
./scripts/dev-server.ps1
```

#### 2. Run Backend

```bash
cd backend
mvn spring-boot:run
```

The backend will start on http://localhost:8080

#### 3. Run Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will start on http://localhost:5173

## Project Structure

```
feathered-oof-bird/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/paypalclone/featheredoofbird/
│   │   │   │   ├── controller/      # REST Controllers
│   │   │   │   ├── model/           # Entity Models
│   │   │   │   ├── repository/      # Data Repositories
│   │   │   │   ├── service/         # Business Logic
│   │   │   │   └── FeatheredOofBirdApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/              # Svelte Components
│   │   ├── services/                # API Services
│   │   ├── App.svelte
│   │   └── main.js
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml
└── README.md
```

## API Endpoints

### Transactions

- `GET /api/transactions` - Get all transactions
- `GET /api/transactions/{id}` - Get transaction by ID
- `GET /api/transactions/sender/{sender}` - Get transactions by sender
- `GET /api/transactions/receiver/{receiver}` - Get transactions by receiver
- `GET /api/transactions/status/{status}` - Get transactions by status
- `POST /api/transactions` - Create a new transaction
- `PUT /api/transactions/{id}` - Update a transaction
- `DELETE /api/transactions/{id}` - Delete a transaction

### Admin

- `GET /api/admin/status` - Admin-only status check (requires `admin:all` scope)

### Transaction Model

```json
{
  "id": 1,
  "sender": "John Doe",
  "receiver": "Jane Smith",
  "amount": 100.50,
  "currency": "USD",
  "description": "Payment for services",
  "status": "COMPLETED",
  "createdAt": "2026-02-02T23:00:00",
  "updatedAt": "2026-02-02T23:00:00"
}
```

Status values: `PENDING`, `COMPLETED`, `FAILED`, `CANCELLED`

## Data Architecture (Postgres Writes + Mongo Reads)

The backend uses a dual-store pattern for transactions:

- **Write path**: `TransactionRepository` writes to PostgreSQL via the `PostgresTransactionStore`.
- **Read path**: queries are served from MongoDB via the `MongoTransactionStore`.
- **Syncing logic**: write operations (create/update/delete) persist to Postgres and immediately upsert/delete the MongoDB read model so reads stay current.

This keeps the `Transaction` domain model unchanged while letting each database optimize for its purpose.

## Testing

The backend test suite includes:

- **Unit tests** for repository/store behavior using mocks.
- **Integration tests** that run PostgreSQL and MongoDB with Testcontainers to validate sync behavior end-to-end.

To run backend tests (Docker required for Testcontainers):

```bash
cd backend
mvn test
```

## Configuration

### Backend Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/paypal_clone
spring.datasource.username=postgres
spring.datasource.password=postgres

# Auth0 JWT Resource Server
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://YOUR_TENANT.eu.auth0.com/
auth0.audience=https://your-api-audience

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/paypal_clone

# Redis/Valkey
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Spring AI (Uncomment and add your API key)
# spring.ai.openai.api-key=${OPENAI_API_KEY}
```

## Auth0 JWT Resource Server

This API is configured as a stateless Auth0-backed resource server. `/api/**` endpoints require a valid Auth0 access token, while `/actuator/health` and `/public/**` remain open.

### Auth0 Setup Checklist

1. **Create an API**
   - Auth0 Dashboard → Applications → APIs → Create API
   - Set **Identifier** to match `auth0.audience`.
2. **(Optional) Enable RBAC**
   - Enable RBAC and "Add Permissions in the Access Token".
   - Create permissions like `read:transactions` or `write:transactions`.
3. **Request a Token**
   - Ensure the token uses `audience` = your API Identifier and include required scopes.

### Smoke Tests

```bash
# 1) Public health
curl http://localhost:8080/actuator/health

# 2) Authenticated GET (requires a valid Auth0 access token)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/transactions

# 3) Protected write (requires write scope or permission)
curl -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"sender":"Ada","receiver":"Linus","amount":42,"currency":"USD","status":"PENDING"}' \
  http://localhost:8080/api/transactions

# 4) Admin-only endpoint (requires admin:all scope)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/admin/status
```

### Frontend Configuration

Edit `frontend/.env`:

```
VITE_API_URL=http://localhost:8080
```

## Technologies Used

### Backend
- Spring Boot 3.2.11
- Spring Data JPA
- Spring Data MongoDB
- Spring Data Redis
- Spring AI
- PostgreSQL
- Lombok
- Jakarta Validation

### Frontend
- Svelte
- Vite
- Flowbite-Svelte (UI Component Library)
- Tailwind CSS

### Infrastructure
- Docker & Docker Compose
- PostgreSQL 16
- MongoDB 7
- Valkey 7.2 (Redis alternative)

## Development

### Building for Production

#### Backend
```bash
cd backend
mvn clean package
```

#### Frontend
```bash
cd frontend
npm run build
```

### Running Tests

#### Backend
```bash
cd backend
mvn test
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is for portfolio purposes.

## Author

Jacob Kurtzberg's Nickname
