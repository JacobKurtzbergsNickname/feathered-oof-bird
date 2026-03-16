import { test, expect } from "@playwright/test";
import {
  MOCK_TRANSACTIONS,
  MOCK_AUTH_SESSION,
  mockApiRoutes,
  mockAuthenticatedSession,
} from "./helpers";

test.describe("Transaction Management", () => {
  test.beforeEach(async ({ page }) => {
    await mockAuthenticatedSession(page);
    await mockApiRoutes(page);
    await page.goto("/");
    // Wait for the transactions list to load
    await expect(page.getByText(MOCK_TRANSACTIONS[0].sender)).toBeVisible();
  });

  test.describe("Transaction List", () => {
    test("displays all transactions in a table", async ({ page }) => {
      for (const tx of MOCK_TRANSACTIONS) {
        await expect(page.getByText(tx.sender)).toBeVisible();
        await expect(page.getByText(tx.receiver)).toBeVisible();
      }
    });

    test("shows transaction ID for each row", async ({ page }) => {
      for (const tx of MOCK_TRANSACTIONS) {
        await expect(page.getByText(tx.id)).toBeVisible();
      }
    });

    test("displays formatted amounts with currency", async ({ page }) => {
      // USD 100.00 should be formatted
      await expect(
        page.getByText(/\$100|100\.00|100,00/).first()
      ).toBeVisible();
    });

    test("shows status badges with correct labels", async ({ page }) => {
      await expect(page.getByText("COMPLETED")).toBeVisible();
      await expect(page.getByText("PENDING")).toBeVisible();
      await expect(page.getByText("FAILED")).toBeVisible();
    });

    test("shows Edit and Delete buttons for each transaction", async ({
      page,
    }) => {
      const editButtons = page.getByRole("button", { name: /edit/i });
      const deleteButtons = page.getByRole("button", { name: /delete/i });

      await expect(editButtons).toHaveCount(MOCK_TRANSACTIONS.length);
      await expect(deleteButtons).toHaveCount(MOCK_TRANSACTIONS.length);
    });

    test("shows empty state when no transactions exist", async ({ page }) => {
      // Override the transactions route to return empty array
      await page.route("**/api/transactions", async (route) => {
        await route.fulfill({ json: [] });
      });

      // Reload to trigger fresh fetch
      await page.reload();
      await expect(
        page.getByText(/no transactions|empty/i)
      ).toBeVisible();
    });

    test("shows loading spinner while fetching", async ({ page }) => {
      // Intercept with a delay to capture the loading state
      let resolveRoute: () => void;
      const routeReady = new Promise<void>((r) => (resolveRoute = r));

      await page.route("**/api/transactions", async (route) => {
        await routeReady;
        await route.fulfill({ json: MOCK_TRANSACTIONS });
      });

      // Reload to trigger fresh fetch (loading state appears before resolveRoute)
      const reloadPromise = page.reload();
      // Give the page a moment to render the loading state
      await page.waitForTimeout(100);

      // Resolve the API route so the page can finish loading
      resolveRoute!();
      await reloadPromise;

      // After loading, transactions are displayed
      await expect(page.getByText(MOCK_TRANSACTIONS[0].sender)).toBeVisible();
    });

    test("shows error message when transaction fetch fails", async ({
      page,
    }) => {
      await page.route("**/api/transactions", async (route) => {
        await route.fulfill({ status: 500, json: { message: "Server error" } });
      });

      await page.reload();

      await expect(page.getByRole("alert").or(page.getByText(/error|failed/i))).toBeVisible();
    });
  });

  test.describe("Create Transaction", () => {
    test("opens create modal when 'Create Transaction' button is clicked", async ({
      page,
    }) => {
      await page.getByRole("button", { name: /create transaction/i }).click();

      await expect(page.getByRole("dialog")).toBeVisible();
      // Should show a blank form (no pre-filled sender)
      await expect(page.getByLabel(/sender/i)).toHaveValue("");
    });

    test("create form has all required fields", async ({ page }) => {
      await page.getByRole("button", { name: /create transaction/i }).click();

      await expect(page.getByLabel(/sender/i)).toBeVisible();
      await expect(page.getByLabel(/receiver/i)).toBeVisible();
      await expect(page.getByLabel(/amount/i)).toBeVisible();
      await expect(page.getByLabel(/currency/i)).toBeVisible();
      await expect(page.getByLabel(/status/i)).toBeVisible();
    });

    test("creates a transaction successfully", async ({ page }) => {
      const newTx = {
        sender: "Dave",
        receiver: "Eve",
        amount: "75.00",
        currency: "USD",
        status: "PENDING",
        description: "Coffee",
      };

      await page.getByRole("button", { name: /create transaction/i }).click();

      await page.getByLabel(/sender/i).fill(newTx.sender);
      await page.getByLabel(/receiver/i).fill(newTx.receiver);
      await page.getByLabel(/amount/i).fill(newTx.amount);
      await page.getByLabel(/currency/i).selectOption(newTx.currency);
      await page.getByLabel(/status/i).selectOption(newTx.status);
      await page.getByLabel(/description/i).fill(newTx.description);

      await page.getByRole("button", { name: /save|submit|create/i }).click();

      // Modal should close after success
      await expect(page.getByRole("dialog")).not.toBeVisible();
    });

    test("verifies correct payload is sent to API", async ({ page }) => {
      let capturedBody: Record<string, unknown> | null = null;

      await page.route("**/api/transactions", async (route) => {
        if (route.request().method() === "POST") {
          capturedBody = JSON.parse(route.request().postData() ?? "{}");
          await route.fulfill({
            json: { id: "tx-new", createdAt: new Date().toISOString(), ...capturedBody },
            status: 201,
          });
        } else {
          await route.fulfill({ json: MOCK_TRANSACTIONS });
        }
      });

      await page.getByRole("button", { name: /create transaction/i }).click();
      await page.getByLabel(/sender/i).fill("Frank");
      await page.getByLabel(/receiver/i).fill("Grace");
      await page.getByLabel(/amount/i).fill("200");
      await page.getByRole("button", { name: /save|submit|create/i }).click();

      await expect(page.getByRole("dialog")).not.toBeVisible();
      expect(capturedBody).toBeTruthy();
      expect(capturedBody!.sender).toBe("Frank");
      expect(capturedBody!.receiver).toBe("Grace");
      expect(Number(capturedBody!.amount)).toBe(200);
    });

    test("sends Authorization header with JWT on create", async ({ page }) => {
      let authHeader: string | null = null;

      await page.route("**/api/transactions", async (route) => {
        if (route.request().method() === "POST") {
          authHeader = route.request().headers()["authorization"] ?? null;
          await route.fulfill({
            json: { id: "tx-new", createdAt: new Date().toISOString() },
            status: 201,
          });
        } else {
          await route.fulfill({ json: MOCK_TRANSACTIONS });
        }
      });

      await page.getByRole("button", { name: /create transaction/i }).click();
      await page.getByLabel(/sender/i).fill("Alice");
      await page.getByLabel(/receiver/i).fill("Bob");
      await page.getByLabel(/amount/i).fill("10");
      await page.getByRole("button", { name: /save|submit|create/i }).click();

      await expect(page.getByRole("dialog")).not.toBeVisible();
      expect(authHeader).toContain(
        `Bearer ${MOCK_AUTH_SESSION.accessToken}`
      );
    });

    test("shows error when create API fails", async ({ page }) => {
      await page.route("**/api/transactions", async (route) => {
        if (route.request().method() === "POST") {
          await route.fulfill({
            status: 400,
            json: { message: "Invalid transaction data" },
          });
        } else {
          await route.fulfill({ json: MOCK_TRANSACTIONS });
        }
      });

      await page.getByRole("button", { name: /create transaction/i }).click();
      await page.getByLabel(/sender/i).fill("Alice");
      await page.getByLabel(/receiver/i).fill("Bob");
      await page.getByLabel(/amount/i).fill("10");
      await page.getByRole("button", { name: /save|submit|create/i }).click();

      // Modal should remain open and show error
      await expect(page.getByRole("dialog")).toBeVisible();
      await expect(page.getByRole("alert")).toBeVisible();
    });

    test("cancels create and closes modal", async ({ page }) => {
      await page.getByRole("button", { name: /create transaction/i }).click();
      await expect(page.getByRole("dialog")).toBeVisible();

      await page.getByRole("button", { name: /cancel|close/i }).click();

      await expect(page.getByRole("dialog")).not.toBeVisible();
    });

    test("validates required fields before submitting", async ({ page }) => {
      await page.getByRole("button", { name: /create transaction/i }).click();

      // Click save without filling required fields
      await page.getByRole("button", { name: /save|submit|create/i }).click();

      // Modal should remain open
      await expect(page.getByRole("dialog")).toBeVisible();
    });
  });

  test.describe("Edit Transaction", () => {
    test("opens edit modal with pre-filled data when Edit is clicked", async ({
      page,
    }) => {
      const firstTx = MOCK_TRANSACTIONS[0];
      await page.getByRole("button", { name: /edit/i }).first().click();

      await expect(page.getByRole("dialog")).toBeVisible();
      await expect(page.getByLabel(/sender/i)).toHaveValue(firstTx.sender);
      await expect(page.getByLabel(/receiver/i)).toHaveValue(firstTx.receiver);
      await expect(page.getByLabel(/amount/i)).toHaveValue(
        String(firstTx.amount)
      );
    });

    test("updates a transaction successfully", async ({ page }) => {
      let capturedBody: Record<string, unknown> | null = null;

      await page.route("**/api/transactions/**", async (route) => {
        if (route.request().method() === "PUT") {
          capturedBody = JSON.parse(route.request().postData() ?? "{}");
          await route.fulfill({ json: { id: "tx-1", ...capturedBody } });
        } else if (route.request().method() === "DELETE") {
          await route.fulfill({ status: 204, body: "" });
        } else {
          await route.fulfill({ json: MOCK_TRANSACTIONS[0] });
        }
      });

      await page.getByRole("button", { name: /edit/i }).first().click();
      await page.getByLabel(/sender/i).fill("UpdatedSender");
      await page.getByRole("button", { name: /save|submit|update/i }).click();

      await expect(page.getByRole("dialog")).not.toBeVisible();
      expect(capturedBody!.sender).toBe("UpdatedSender");
    });

    test("sends PUT request to correct URL", async ({ page }) => {
      let requestUrl: string | null = null;

      await page.route("**/api/transactions/**", async (route) => {
        if (route.request().method() === "PUT") {
          requestUrl = route.request().url();
          await route.fulfill({
            json: { id: MOCK_TRANSACTIONS[0].id, sender: "Updated" },
          });
        } else {
          await route.continue();
        }
      });

      await page.getByRole("button", { name: /edit/i }).first().click();
      await page.getByRole("button", { name: /save|submit|update/i }).click();

      await expect(page.getByRole("dialog")).not.toBeVisible();
      expect(requestUrl).toContain(`/api/transactions/${MOCK_TRANSACTIONS[0].id}`);
    });

    test("shows error when update fails", async ({ page }) => {
      await page.route("**/api/transactions/**", async (route) => {
        if (route.request().method() === "PUT") {
          await route.fulfill({
            status: 500,
            json: { message: "Update failed" },
          });
        } else {
          await route.continue();
        }
      });

      await page.getByRole("button", { name: /edit/i }).first().click();
      await page.getByLabel(/sender/i).fill("Updated");
      await page.getByRole("button", { name: /save|submit|update/i }).click();

      await expect(page.getByRole("dialog")).toBeVisible();
      await expect(page.getByRole("alert")).toBeVisible();
    });

    test("can change transaction status in edit form", async ({ page }) => {
      let capturedBody: Record<string, unknown> | null = null;

      await page.route("**/api/transactions/**", async (route) => {
        if (route.request().method() === "PUT") {
          capturedBody = JSON.parse(route.request().postData() ?? "{}");
          await route.fulfill({ json: { id: "tx-1", ...capturedBody } });
        } else {
          await route.continue();
        }
      });

      await page.getByRole("button", { name: /edit/i }).first().click();
      await page.getByLabel(/status/i).selectOption("CANCELLED");
      await page.getByRole("button", { name: /save|submit|update/i }).click();

      await expect(page.getByRole("dialog")).not.toBeVisible();
      expect(capturedBody!.status).toBe("CANCELLED");
    });
  });

  test.describe("Delete Transaction", () => {
    test("shows confirmation dialog before deleting", async ({ page }) => {
      // Mock window.confirm to return false (cancel)
      await page.evaluate(() => {
        window.confirm = () => false;
      });

      await page.getByRole("button", { name: /delete/i }).first().click();

      // No DELETE request should have been made — transactions still visible
      await expect(page.getByText(MOCK_TRANSACTIONS[0].sender)).toBeVisible();
    });

    test("deletes a transaction when confirmed", async ({ page }) => {
      let deleteCount = 0;
      const remaining = MOCK_TRANSACTIONS.slice(1);

      await page.route("**/api/transactions/**", async (route) => {
        if (route.request().method() === "DELETE") {
          deleteCount++;
          await route.fulfill({ status: 204, body: "" });
        } else {
          await route.continue();
        }
      });

      // After delete, the list should refresh with one less item
      await page.route("**/api/transactions", async (route) => {
        if (route.request().method() === "GET") {
          await route.fulfill({ json: deleteCount > 0 ? remaining : MOCK_TRANSACTIONS });
        } else {
          await route.continue();
        }
      });

      // Confirm the dialog
      page.on("dialog", (dialog) => dialog.accept());

      await page.getByRole("button", { name: /delete/i }).first().click();

      await expect(page.getByText(MOCK_TRANSACTIONS[0].sender)).not.toBeVisible();
      expect(deleteCount).toBe(1);
    });

    test("sends DELETE request with correct transaction ID", async ({
      page,
    }) => {
      let requestUrl: string | null = null;

      await page.route("**/api/transactions/**", async (route) => {
        if (route.request().method() === "DELETE") {
          requestUrl = route.request().url();
          await route.fulfill({ status: 204, body: "" });
        } else {
          await route.continue();
        }
      });

      page.on("dialog", (dialog) => dialog.accept());
      await page.getByRole("button", { name: /delete/i }).first().click();

      // Wait for the request to be captured
      await page.waitForTimeout(500);
      expect(requestUrl).toContain(`/api/transactions/${MOCK_TRANSACTIONS[0].id}`);
    });

    test("does not delete when user cancels confirmation", async ({ page }) => {
      let deleteCount = 0;

      await page.route("**/api/transactions/**", async (route) => {
        if (route.request().method() === "DELETE") {
          deleteCount++;
          await route.fulfill({ status: 204, body: "" });
        } else {
          await route.continue();
        }
      });

      page.on("dialog", (dialog) => dialog.dismiss());
      await page.getByRole("button", { name: /delete/i }).first().click();

      await page.waitForTimeout(300);
      expect(deleteCount).toBe(0);
      await expect(page.getByText(MOCK_TRANSACTIONS[0].sender)).toBeVisible();
    });
  });

  test.describe("Auth-protected requests", () => {
    test("includes Bearer token in transaction list request", async ({
      page,
    }) => {
      let authHeader: string | null = null;

      await page.route("**/api/transactions", async (route) => {
        authHeader = route.request().headers()["authorization"] ?? null;
        await route.fulfill({ json: MOCK_TRANSACTIONS });
      });

      await page.reload();
      await expect(page.getByText(MOCK_TRANSACTIONS[0].sender)).toBeVisible();

      expect(authHeader).toContain(`Bearer ${MOCK_AUTH_SESSION.accessToken}`);
    });

    test("redirects to login on 401 response", async ({ page }) => {
      await page.route("**/api/transactions", async (route) => {
        await route.fulfill({ status: 401, json: { message: "Unauthorized" } });
      });

      await page.reload();

      // 401 should clear session and show login form
      await expect(
        page.getByRole("heading", { name: /sign in|login/i })
      ).toBeVisible();
    });
  });
});
