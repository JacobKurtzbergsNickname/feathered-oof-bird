/**
 * Sends a bearer token to the API and verifies the auth flow works.
 *
 * Usage:
 *   npx tsx src/test-api.ts --token <access_token>
 *
 * Piped from get-token.ts:
 *   npx tsx src/test-api.ts --token $(npx tsx src/get-token.ts)
 *
 * Or all-in-one:
 *   npm run test-flow
 */

import "dotenv/config";

// ---------------------------------------------------------------------------
// Config
// ---------------------------------------------------------------------------

function parseArgs(): { token: string; baseUrl: string } {
  const tokenIdx = process.argv.indexOf("--token");
  if (tokenIdx === -1 || !process.argv[tokenIdx + 1]) {
    console.error("Usage: npx tsx src/test-api.ts --token <access_token>");
    process.exit(1);
  }

  const token = process.argv[tokenIdx + 1];
  const baseUrl = (process.env.API_BASE_URL ?? "http://localhost:8080").replace(
    /\/$/,
    ""
  );

  return { token, baseUrl };
}

// ---------------------------------------------------------------------------
// Test runner
// ---------------------------------------------------------------------------

interface TestCase {
  name: string;
  method: string;
  path: string;
  expectedStatus: number;
  body?: Record<string, unknown>;
  requiresAuth: boolean;
}

const TEST_CASES: TestCase[] = [
  {
    name: "Public health check",
    method: "GET",
    path: "/actuator/health",
    expectedStatus: 200,
    requiresAuth: false,
  },
  {
    name: "Unauthenticated request is rejected",
    method: "GET",
    path: "/api/transactions",
    expectedStatus: 401,
    requiresAuth: false,
  },
  {
    name: "GET /api/transactions (authenticated)",
    method: "GET",
    path: "/api/transactions",
    expectedStatus: 200,
    requiresAuth: true,
  },
  {
    name: "POST /api/transactions (write permission)",
    method: "POST",
    path: "/api/transactions",
    expectedStatus: 201,
    requiresAuth: true,
    body: {
      sender: "Auth0 Test",
      receiver: "API Test",
      amount: 1.0,
      currency: "USD",
      description: "Auth flow smoke test",
      status: "PENDING",
    },
  },
  {
    name: "GET /api/admin/status (admin permission)",
    method: "GET",
    path: "/api/admin/status",
    expectedStatus: 200,
    requiresAuth: true,
  },
];

interface TestResult {
  name: string;
  passed: boolean;
  actual: number;
  expected: number;
  detail?: string;
}

async function runTest(
  test: TestCase,
  token: string,
  baseUrl: string
): Promise<TestResult> {
  const url = `${baseUrl}${test.path}`;
  const headers: Record<string, string> = {};

  if (test.requiresAuth) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  if (test.body) {
    headers["Content-Type"] = "application/json";
  }

  try {
    const res = await fetch(url, {
      method: test.method,
      headers,
      body: test.body ? JSON.stringify(test.body) : undefined,
    });

    const passed = res.status === test.expectedStatus;
    let detail: string | undefined;

    if (!passed) {
      const text = await res.text();
      detail = text.length > 200 ? text.slice(0, 200) + "…" : text;
    }

    return {
      name: test.name,
      passed,
      actual: res.status,
      expected: test.expectedStatus,
      detail,
    };
  } catch (err) {
    return {
      name: test.name,
      passed: false,
      actual: 0,
      expected: test.expectedStatus,
      detail: `Network error: ${err instanceof Error ? err.message : err}`,
    };
  }
}

// ---------------------------------------------------------------------------
// Output
// ---------------------------------------------------------------------------

function printResults(results: TestResult[]): void {
  const maxName = Math.max(...results.map((r) => r.name.length));

  console.log("\n  Auth0 → API  Test Results");
  console.log("  " + "─".repeat(maxName + 24));

  for (const r of results) {
    const icon = r.passed ? "✓" : "✗";
    const status = r.passed ? "PASS" : "FAIL";
    const padding = " ".repeat(maxName - r.name.length);
    console.log(
      `  ${icon}  ${r.name}${padding}  ${status}  (${r.actual} / expected ${r.expected})`
    );
    if (r.detail) {
      console.log(`     └─ ${r.detail}`);
    }
  }

  console.log("  " + "─".repeat(maxName + 24));
  const passed = results.filter((r) => r.passed).length;
  console.log(`  ${passed}/${results.length} passed\n`);
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------

async function main(): Promise<void> {
  const { token, baseUrl } = parseArgs();

  console.log(`\n  Target API : ${baseUrl}`);
  console.log(`  Token      : ${token.slice(0, 20)}…`);

  const results: TestResult[] = [];
  for (const test of TEST_CASES) {
    results.push(await runTest(test, token, baseUrl));
  }

  printResults(results);

  const allPassed = results.every((r) => r.passed);
  process.exit(allPassed ? 0 : 1);
}

main();
