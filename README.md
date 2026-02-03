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
- ✅ Responsive UI with Svelte and Flowbite-Svelte
- ✅ PostgreSQL for relational data storage
- ✅ MongoDB for document storage
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
docker-compose up -d
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
docker-compose up -d postgres mongodb valkey
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

## Configuration

### Backend Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/paypal_clone
spring.datasource.username=postgres
spring.datasource.password=postgres

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/paypal_clone

# Redis/Valkey
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Spring AI (Uncomment and add your API key)
# spring.ai.openai.api-key=${OPENAI_API_KEY}
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
