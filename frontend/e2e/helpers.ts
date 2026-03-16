import { Page } from "@playwright/test";

export const TEST_USER = {
  email: "testuser@example.com",
  password: "password123",
  role: "PERSONAL",
};

export const MOCK_AUTH_SESSION = {
  accessToken: "mock-jwt-token",
  tokenType: "Bearer",
  expiresAt: new Date(Date.now() + 3600 * 1000).toISOString(),
  user: {
    id: "user-1",
    email: TEST_USER.email,
    role: "PERSONAL",
    status: "ACTIVE",
  },
};

export const MOCK_TRANSACTIONS = [
  {
    id: "tx-1",
    sender: "Alice",
    receiver: "Bob",
    amount: 100.0,
    currency: "USD",
    description: "Lunch",
    status: "COMPLETED",
    createdAt: "2024-01-15T10:30:00Z",
  },
  {
    id: "tx-2",
    sender: "Charlie",
    receiver: "Alice",
    amount: 250.5,
    currency: "EUR",
    description: "Rent split",
    status: "PENDING",
    createdAt: "2024-01-16T14:00:00Z",
  },
  {
    id: "tx-3",
    sender: "Bob",
    receiver: "Charlie",
    amount: 50.0,
    currency: "GBP",
    description: null,
    status: "FAILED",
    createdAt: "2024-01-17T09:00:00Z",
  },
];

/**
 * Mock all API routes with sane defaults. Individual tests can override
 * specific routes by calling page.route() before this helper.
 */
export async function mockAuthenticatedSession(page: Page) {
  // Store auth session in sessionStorage before navigation
  await page.addInitScript((session) => {
    sessionStorage.setItem(
      "feathered-oof-bird.auth.session",
      JSON.stringify(session)
    );
  }, MOCK_AUTH_SESSION);
}

export async function mockApiRoutes(page: Page) {
  // Transactions list
  await page.route("**/api/transactions", async (route) => {
    if (route.request().method() === "GET") {
      await route.fulfill({ json: MOCK_TRANSACTIONS });
    } else if (route.request().method() === "POST") {
      const body = JSON.parse(route.request().postData() ?? "{}");
      await route.fulfill({
        json: { id: "tx-new", createdAt: new Date().toISOString(), ...body },
        status: 201,
      });
    } else {
      await route.continue();
    }
  });

  // Individual transaction operations
  await page.route("**/api/transactions/**", async (route) => {
    const method = route.request().method();
    if (method === "GET") {
      await route.fulfill({ json: MOCK_TRANSACTIONS[0] });
    } else if (method === "PUT") {
      const body = JSON.parse(route.request().postData() ?? "{}");
      const id = route.request().url().split("/").pop();
      await route.fulfill({ json: { id, ...body } });
    } else if (method === "DELETE") {
      await route.fulfill({ status: 204, body: "" });
    } else {
      await route.continue();
    }
  });
}

export async function mockLoginRoute(
  page: Page,
  response = MOCK_AUTH_SESSION
) {
  await page.route("**/api/auth/login", async (route) => {
    await route.fulfill({ json: response });
  });
}

export async function mockRegisterRoute(
  page: Page,
  response = MOCK_AUTH_SESSION
) {
  await page.route("**/api/auth/register", async (route) => {
    await route.fulfill({ json: response, status: 201 });
  });
}
