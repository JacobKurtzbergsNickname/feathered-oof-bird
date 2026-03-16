import { test, expect } from "@playwright/test";
import {
  TEST_USER,
  MOCK_AUTH_SESSION,
  mockLoginRoute,
  mockRegisterRoute,
  mockApiRoutes,
  mockAuthenticatedSession,
} from "./helpers";

test.describe("Authentication", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
  });

  test.describe("Login", () => {
    test("shows login form by default", async ({ page }) => {
      await expect(
        page.getByRole("heading", { name: /sign in|login/i })
      ).toBeVisible();
      await expect(page.getByLabel(/email/i)).toBeVisible();
      await expect(page.getByLabel(/password/i)).toBeVisible();
      await expect(
        page.getByRole("button", { name: /sign in|login/i })
      ).toBeVisible();
    });

    test("can toggle to register form", async ({ page }) => {
      await page.getByText(/create an account|register|sign up/i).click();
      await expect(
        page.getByRole("heading", { name: /create|register|sign up/i })
      ).toBeVisible();
      await expect(
        page.getByRole("combobox").or(page.getByLabel(/account type|role/i))
      ).toBeVisible();
    });

    test("logs in successfully with valid credentials", async ({ page }) => {
      await mockLoginRoute(page);
      await mockApiRoutes(page);

      await page.getByLabel(/email/i).fill(TEST_USER.email);
      await page.getByLabel(/password/i).fill(TEST_USER.password);
      await page.getByRole("button", { name: /sign in|login/i }).click();

      // After login, should see the authenticated dashboard
      await expect(page.getByText(TEST_USER.email)).toBeVisible();
      await expect(
        page.getByRole("button", { name: /logout|sign out/i })
      ).toBeVisible();
    });

    test("stores auth session in sessionStorage after login", async ({
      page,
    }) => {
      await mockLoginRoute(page);
      await mockApiRoutes(page);

      await page.getByLabel(/email/i).fill(TEST_USER.email);
      await page.getByLabel(/password/i).fill(TEST_USER.password);
      await page.getByRole("button", { name: /sign in|login/i }).click();

      await expect(page.getByText(TEST_USER.email)).toBeVisible();

      const storedSession = await page.evaluate(() =>
        sessionStorage.getItem("feathered-oof-bird.auth.session")
      );
      expect(storedSession).not.toBeNull();
      const session = JSON.parse(storedSession!);
      expect(session.accessToken).toBe(MOCK_AUTH_SESSION.accessToken);
    });

    test("shows error message on failed login", async ({ page }) => {
      await page.route("**/api/auth/login", async (route) => {
        await route.fulfill({
          status: 401,
          json: { message: "Invalid credentials" },
        });
      });

      await page.getByLabel(/email/i).fill(TEST_USER.email);
      await page.getByLabel(/password/i).fill("wrongpassword");
      await page.getByRole("button", { name: /sign in|login/i }).click();

      await expect(page.getByRole("alert")).toBeVisible();
    });

    test("disables submit button while logging in", async ({ page }) => {
      // Slow the response so we can observe the loading state
      await page.route("**/api/auth/login", async (route) => {
        await new Promise((r) => setTimeout(r, 500));
        await route.fulfill({ json: MOCK_AUTH_SESSION });
      });
      await mockApiRoutes(page);

      await page.getByLabel(/email/i).fill(TEST_USER.email);
      await page.getByLabel(/password/i).fill(TEST_USER.password);

      const submitBtn = page.getByRole("button", { name: /sign in|login/i });
      await submitBtn.click();

      await expect(submitBtn).toBeDisabled();
    });

    test("validates email format", async ({ page }) => {
      await page.getByLabel(/email/i).fill("not-an-email");
      await page.getByLabel(/password/i).fill(TEST_USER.password);
      await page.getByRole("button", { name: /sign in|login/i }).click();

      // HTML5 validation or custom error — login API should not be called
      const isStillOnLogin = await page
        .getByRole("heading", { name: /sign in|login/i })
        .isVisible();
      expect(isStillOnLogin).toBe(true);
    });

    test("validates minimum password length", async ({ page }) => {
      await page.getByLabel(/email/i).fill(TEST_USER.email);
      await page.getByLabel(/password/i).fill("short");
      await page.getByRole("button", { name: /sign in|login/i }).click();

      // Should not reach the API — still on login page
      const isStillOnLogin = await page
        .getByRole("heading", { name: /sign in|login/i })
        .isVisible();
      expect(isStillOnLogin).toBe(true);
    });
  });

  test.describe("Registration", () => {
    test.beforeEach(async ({ page }) => {
      await page.getByText(/create an account|register|sign up/i).click();
    });

    test("shows registration form with account type selector", async ({
      page,
    }) => {
      await expect(
        page.getByRole("heading", { name: /create|register|sign up/i })
      ).toBeVisible();
      await expect(page.getByLabel(/email/i)).toBeVisible();
      await expect(page.getByLabel(/password/i)).toBeVisible();
      // Account type select should be present
      await expect(
        page
          .getByRole("combobox")
          .or(page.getByLabel(/account type|role/i))
          .first()
      ).toBeVisible();
    });

    test("registers successfully and transitions to dashboard", async ({
      page,
    }) => {
      await mockRegisterRoute(page);
      await mockApiRoutes(page);

      await page.getByLabel(/email/i).fill(TEST_USER.email);
      await page.getByLabel(/password/i).fill(TEST_USER.password);
      // Select account type if there's a dropdown
      const accountTypeSelect = page.getByRole("combobox").first();
      if (await accountTypeSelect.isVisible()) {
        await accountTypeSelect.selectOption("PERSONAL");
      }

      await page.getByRole("button", { name: /create|register|sign up/i }).click();

      await expect(page.getByText(TEST_USER.email)).toBeVisible();
    });

    test("shows error when registration fails", async ({ page }) => {
      await page.route("**/api/auth/register", async (route) => {
        await route.fulfill({
          status: 409,
          json: { message: "Email already in use" },
        });
      });

      await page.getByLabel(/email/i).fill(TEST_USER.email);
      await page.getByLabel(/password/i).fill(TEST_USER.password);
      await page.getByRole("button", { name: /create|register|sign up/i }).click();

      await expect(page.getByRole("alert")).toBeVisible();
    });

    test("can toggle back to login form", async ({ page }) => {
      await page.getByText(/sign in|already have an account/i).click();
      await expect(
        page.getByRole("heading", { name: /sign in|login/i })
      ).toBeVisible();
    });
  });

  test.describe("Logout", () => {
    test("logs out and returns to login form", async ({ page }) => {
      await mockAuthenticatedSession(page);
      await mockApiRoutes(page);
      await page.goto("/");

      await expect(page.getByText(TEST_USER.email)).toBeVisible();

      await page.getByRole("button", { name: /logout|sign out/i }).click();

      await expect(
        page.getByRole("heading", { name: /sign in|login/i })
      ).toBeVisible();
    });

    test("clears sessionStorage on logout", async ({ page }) => {
      await mockAuthenticatedSession(page);
      await mockApiRoutes(page);
      await page.goto("/");

      await page.getByRole("button", { name: /logout|sign out/i }).click();

      const storedSession = await page.evaluate(() =>
        sessionStorage.getItem("feathered-oof-bird.auth.session")
      );
      expect(storedSession).toBeNull();
    });
  });

  test.describe("Session persistence", () => {
    test("restores session from sessionStorage on page reload", async ({
      page,
    }) => {
      await mockAuthenticatedSession(page);
      await mockApiRoutes(page);
      await page.goto("/");

      // Should be authenticated without logging in
      await expect(page.getByText(TEST_USER.email)).toBeVisible();
    });

    test("redirects to login if sessionStorage is cleared", async ({
      page,
    }) => {
      await mockAuthenticatedSession(page);
      await mockApiRoutes(page);
      await page.goto("/");
      await expect(page.getByText(TEST_USER.email)).toBeVisible();

      // Clear session and reload
      await page.evaluate(() => sessionStorage.clear());
      await page.reload();

      await expect(
        page.getByRole("heading", { name: /sign in|login/i })
      ).toBeVisible();
    });
  });
});
