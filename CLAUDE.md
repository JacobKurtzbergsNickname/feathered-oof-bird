# CLAUDE.md — Feathered OOF Bird

AI assistant guide for the **Feathered OOF Bird** project — a PayPal-like portfolio application with a Spring Boot backend and Svelte frontend.

---

## Project Overview

A full-stack financial transaction management system demonstrating modern architectures:
- **Dual-database pattern**: PostgreSQL for writes (OLTP), MongoDB for reads (view model)
- **Stateless JWT auth**: Auth0 (production) or local JWT (development)
- **Redis/Valkey cache**: Valkey 7.2 for caching
- **Component-based SPA**: Svelte + TypeScript + Tailwind CSS

---

## Repository Structure

```
feathered-oof-bird/
├── backend/                     # Spring Boot 3.5 + Java 21
│   ├── src/main/java/com/paypalclone/featheredoofbird/
│   │   ├── payments/            # Transaction CRUD, dual-store pattern
│   │   ├── identity/            # User registration + password hashing
│   │   ├── auth/                # JWT validation (Auth0 + local fallback)
│   │   ├── admin/               # Admin-only endpoints
│   │   ├── ledger/              # Append-only double-entry ledger
│   │   ├── notifications/       # Event notification infrastructure
│   │   ├── risk/                # Risk assessment / fraud detection
│   │   └── shared/              # Security config, app config, env secrets
│   ├── src/test/java/           # JUnit 5 unit + Testcontainers integration tests
│   ├── pom.xml                  # Maven dependencies and plugins
│   ├── checkstyle.xml           # Checkstyle rules
│   └── Dockerfile               # Multi-stage JDK 21 → JRE 21 alpine
├── frontend/                    # Svelte 5 + Vite + TypeScript
│   ├── src/
│   │   ├── App.svelte           # Root component, modal + transaction management
│   │   ├── TransactionList.svelte
│   │   ├── TransactionForm.svelte
│   │   ├── AuthForm.svelte
│   │   ├── transactionService.ts
│   │   ├── authService.ts
│   │   ├── authStore.ts         # Svelte writable store for auth state
│   │   ├── httpClient.ts        # Fetch wrapper with JWT injection
│   │   └── lib/types.ts         # TypeScript interfaces
│   ├── package.json
│   └── Dockerfile               # Multi-stage Node 22 alpine
├── docker-compose.yml           # Postgres, MongoDB, Valkey, backend, frontend
├── scripts/
│   ├── dev-server.sh            # Linux/Mac dev startup
│   └── dev-server.ps1           # Windows PowerShell dev startup
├── docs/                        # Ralph loop workflow docs + PRD JSON
├── paypal_like_portfolio_project_blueprint.md
├── paypal_like_portfolio_project_tickets.md
└── README.md
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend language | Java 21 |
| Backend framework | Spring Boot 3.5 |
| Build tool | Maven 3.9 (wrapper: `./mvnw`) |
| Frontend language | TypeScript + Svelte 5 |
| Frontend build | Vite 7 |
| UI components | Flowbite-Svelte + Tailwind CSS 4 |
| Write database | PostgreSQL 17 |
| Read database | MongoDB 7 |
| Cache | Valkey 7.2 (Redis-compatible) |
| Auth (prod) | Auth0 JWT (OAuth2 Resource Server) |
| Auth (dev) | Local JWT strategy |
| Testing (backend) | JUnit 5, Mockito, Testcontainers |
| Testing (frontend) | Vitest |
| CI | GitHub Actions (`.github/workflows/ci.yml`) |

---

## Development Setup

### Prerequisites
- Java 21, Maven 3.9+
- Node 22+, npm
- Docker + Docker Compose

### Start databases (required for backend)
```bash
docker compose up -d postgres mongodb valkey
```

### Backend
```bash
cd backend
./mvnw spring-boot:run        # Dev server on :8080
```

Or use the convenience script:
```bash
./scripts/dev-server.sh       # Linux/Mac
./scripts/dev-server.ps1      # Windows
```

### Frontend
```bash
cd frontend
npm install
npm run dev                   # Dev server on :5173 (Vite HMR)
```

### Full stack (Docker, production profile)
```bash
docker compose --profile prod up -d
```

---

## Common Commands

### Backend
```bash
./mvnw test                   # Run tests
./mvnw verify                 # Full QA: build + checkstyle + test
./mvnw spotless:apply         # Auto-format code (must run before committing)
./mvnw spotless:check         # Verify formatting (CI gate)
./mvnw checkstyle:check       # Lint check
./mvnw clean package          # Build JAR
```

### Frontend
```bash
npm run dev                   # Dev server
npm run build                 # Production build
npm run lint                  # svelte-check type checking
npm run test                  # Run tests (Vitest)
npm run test:watch            # Watch mode
```

---

## CI/CD Pipeline

**File**: `.github/workflows/ci.yml`

**Triggers**: Push or PR to `main`

**Steps** (runs on `ubuntu-latest`, JDK 21 Temurin):
1. `./mvnw spotless:check` — formatting gate (fails if code is unformatted)
2. `./mvnw verify` — full build + checkstyle + all tests

> Always run `./mvnw spotless:apply` before committing backend Java changes or CI will fail.

---

## Code Conventions

### Backend (Java)

- **Style**: Google Java Format (AOSP variant) enforced by Spotless
- **Formatting**: Run `./mvnw spotless:apply` before every commit
- **Architecture**: Domain-driven layering — `domain` → `application` → `infrastructure`
- **DI**: Constructor injection via Lombok `@RequiredArgsConstructor` (no field injection)
- **Transactions**: `@Transactional(readOnly = true)` on query methods; `@Transactional` on mutations
- **Authorization**: Method-level `@PreAuthorize("hasAuthority('scope')")` on controllers
- **Validation**: Jakarta Validation annotations (`@NotBlank`, `@Positive`, etc.) + `@Valid` on controller params
- **Null safety**: Use `@NonNull` (Lombok/Spring) for parameters; return `Optional<T>` where appropriate
- **No direct field access**: Use Lombok-generated getters/setters via `@Data` / `@Getter` / `@Setter`

### Frontend (TypeScript/Svelte)

- **Styling**: Tailwind CSS utility classes only — no scoped `<style>` blocks
- **State**: Svelte writable stores for global state (auth); component-level reactivity for local state
- **API calls**: Go through service files (`transactionService.ts`, `authService.ts`) — not directly from components
- **HTTP**: Use `httpClient.ts` for all fetch calls (handles JWT header injection)
- **Types**: Define domain interfaces in `lib/types.ts`
- **Async**: `async/await` for all API calls; wrap in `try/catch` with user-facing error messages
- **Naming**: camelCase for functions/variables, PascalCase for components and types

---

## Architecture Notes

### Dual-Database Write/Read Pattern
- **Writes** go to PostgreSQL via JPA (`TransactionRepository`)
- **Reads** come from MongoDB (`MongoTransactionStore`) — optimized for queries
- Sync between stores is handled in the service layer

### Authentication Flow
- JWT validated by Spring Security OAuth2 Resource Server
- Auth provider selected via `AUTH_PROVIDER` environment variable (`auth0` or `local-jwt`)
- Auth0 for production; local JWT for local development without Auth0 credentials
- No sessions — fully stateless (`SessionCreationPolicy.STATELESS`)

### Package Layout (Backend)
Each feature module follows this internal structure:
```
payments/
├── domain/          # Entities, value objects, domain interfaces
├── application/     # Services (business logic)
└── infrastructure/  # Repositories, external integrations
```

ArchUnit tests (in `shared/ArchitectureTest.java`) enforce that layer boundaries are not violated.

---

## Environment Variables

### Backend (`application.properties` / env)
| Variable | Description | Default (dev) |
|---|---|---|
| `AUTH_PROVIDER` | `auth0` or `local-jwt` | `local-jwt` |
| `AUTH0_ISSUER_URI` | Auth0 domain | — |
| `AUTH0_AUDIENCE` | Auth0 API audience | — |
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5532/featheredoofbird` |
| `DB_USERNAME` | Postgres user | `featheredoofbird` |
| `DB_PASSWORD` | Postgres password | — |
| `MONGO_URI` | MongoDB connection string | `mongodb://localhost:27020/featheredoofbird` |
| `REDIS_HOST` | Valkey/Redis host | `localhost` |
| `REDIS_PORT` | Valkey/Redis port | `6379` |
| `REDIS_PASSWORD` | Valkey/Redis password | — |

### Frontend
| Variable | Description | Default |
|---|---|---|
| `VITE_API_URL` | Backend base URL | `http://localhost:8080` |

---

## Testing

### Backend
- **Unit tests**: Mockito mocks for services and repositories
- **Integration tests**: Testcontainers spins up real PostgreSQL + MongoDB containers automatically
- **Architecture tests**: ArchUnit validates layer separation (no skipping allowed)
- Run with: `./mvnw test`

### Frontend
- **Unit tests**: Vitest
- **Type checking**: `npm run lint` (svelte-check)

> Testcontainers requires Docker to be running when executing backend integration tests.

---

## Key Files Quick Reference

| File | Purpose |
|---|---|
| `backend/src/main/java/.../payments/application/TransactionService.java` | Core business logic for transactions |
| `backend/src/main/java/.../payments/domain/Transaction.java` | Transaction entity + validation |
| `backend/src/main/java/.../shared/SecurityConfig.java` | Spring Security + OAuth2 setup |
| `backend/src/main/java/.../shared/config/AppConfig.java` | Auth provider selection |
| `backend/src/test/java/.../shared/ArchitectureTest.java` | Layer boundary enforcement |
| `frontend/src/httpClient.ts` | Fetch wrapper with auth headers |
| `frontend/src/authStore.ts` | Global auth state (Svelte store) |
| `frontend/src/lib/types.ts` | TypeScript domain interfaces |
| `docker-compose.yml` | All services + health checks |
| `.github/workflows/ci.yml` | CI pipeline definition |

---

## Common Pitfalls

1. **Spotless failure in CI**: Always run `./mvnw spotless:apply` before committing Java files.
2. **Testcontainers in CI**: Docker must be available — GitHub Actions `ubuntu-latest` has Docker built in, so this is fine.
3. **Auth0 in dev**: Set `AUTH_PROVIDER=local-jwt` (already the default in dev profile) to avoid needing Auth0 credentials locally.
4. **Port conflicts**: Postgres uses `5532` (not `5432`), MongoDB uses `27020` (not `27017`) to avoid conflicts with local installs.
5. **Frontend env**: Create `frontend/.env.local` with `VITE_API_URL` if the backend runs on a non-default port.
6. **ArchUnit violations**: The architecture tests in `ArchitectureTest.java` will fail if you import across layer boundaries (e.g., infrastructure importing from application of another module).
