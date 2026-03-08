# Auth0 Authentication Flow Test Scripts

Two TypeScript scripts to verify the end-to-end Auth0 → API authentication flow.

## Prerequisites

- Node.js 18+ (uses native `fetch`)
- An Auth0 **Machine-to-Machine** application configured with the API audience and the required permissions (`read:transactions`, `write:transactions`, `admin:all`).

## Setup

```bash
cd scripts/auth-test
npm install
cp .env.example .env
# Fill in your Auth0 credentials in .env
```

### Environment variables

| Variable             | Description                                             | Required |
| -------------------- | ------------------------------------------------------- | -------- |
| `AUTH0_DOMAIN`       | Auth0 tenant domain (e.g. `your-tenant.eu.auth0.com`)  | Yes      |
| `AUTH0_CLIENT_ID`    | M2M application client ID                               | Yes      |
| `AUTH0_CLIENT_SECRET`| M2M application client secret                           | Yes      |
| `AUTH0_AUDIENCE`     | API identifier / audience                                | Yes      |
| `API_BASE_URL`       | Backend URL (defaults to `http://localhost:8080`)        | No       |

## Usage

### 1. Get a token

```bash
npx tsx src/get-token.ts
```

Outputs the bare access token to stdout. Add `--verbose` to print token metadata to stderr:

```bash
npx tsx src/get-token.ts --verbose
```

### 2. Test the API

Pass a token directly:

```bash
npx tsx src/test-api.ts --token <access_token>
```

### 3. Full flow (piped)

Fetch a token and immediately test the API:

```bash
npx tsx src/test-api.ts --token $(npx tsx src/get-token.ts)
```

Or use the npm shortcut:

```bash
npm run test-flow
```

## What it tests

| # | Test                                    | Endpoint                 | Expected |
| - | --------------------------------------- | ------------------------ | -------- |
| 1 | Public health check (no token)          | `GET /actuator/health`   | 200      |
| 2 | Unauthenticated request is rejected     | `GET /api/transactions`  | 401      |
| 3 | Authenticated read                      | `GET /api/transactions`  | 200      |
| 4 | Write with permission                   | `POST /api/transactions` | 201      |
| 5 | Admin endpoint                          | `GET /api/admin/status`  | 200      |

The script exits with code `0` if all tests pass, `1` otherwise.
