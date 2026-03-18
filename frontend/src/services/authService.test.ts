import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { AuthSession } from '../lib/auth';
import { authService } from './authService';
import { clearStoredAuthSession, getAccessToken, storeAuthSession } from './authStorage';

const mockSession: AuthSession = {
  accessToken: 'session-jwt-token',
  tokenType: 'Bearer',
  expiresAt: '2026-12-31T23:59:59Z',
  user: {
    id: 1,
    email: 'user@example.com',
    role: 'PERSONAL',
    status: 'ACTIVE',
  },
};

describe('authService', () => {
  const originalFetch = globalThis.fetch;

  beforeEach(() => {
    vi.restoreAllMocks();
    clearStoredAuthSession();
  });

  afterEach(() => {
    globalThis.fetch = originalFetch;
  });

  describe('login', () => {
    it('calls POST /api/auth/login', async () => {
      globalThis.fetch = vi.fn(
        async () =>
          new Response(JSON.stringify(mockSession), {
            status: 200,
            headers: { 'Content-Type': 'application/json' },
          }),
      ) as typeof fetch;

      await authService.login({ email: 'user@example.com', password: 'password123' });

      const [url, init] = (globalThis.fetch as ReturnType<typeof vi.fn>).mock.calls[0];
      expect(url).toContain('/api/auth/login');
      expect(init.method).toBe('POST');
    });

    it('sends credentials in the request body', async () => {
      globalThis.fetch = vi.fn(
        async () =>
          new Response(JSON.stringify(mockSession), {
            status: 200,
            headers: { 'Content-Type': 'application/json' },
          }),
      ) as typeof fetch;

      await authService.login({ email: 'user@example.com', password: 'password123' });

      const [, init] = (globalThis.fetch as ReturnType<typeof vi.fn>).mock.calls[0];
      const body = JSON.parse(init.body as string);
      expect(body.email).toBe('user@example.com');
      expect(body.password).toBe('password123');
    });

    it('stores the auth session on success', async () => {
      globalThis.fetch = vi.fn(
        async () =>
          new Response(JSON.stringify(mockSession), {
            status: 200,
            headers: { 'Content-Type': 'application/json' },
          }),
      ) as typeof fetch;

      await authService.login({ email: 'user@example.com', password: 'password123' });

      expect(getAccessToken()).toBe('session-jwt-token');
    });

    it('returns the session on success', async () => {
      globalThis.fetch = vi.fn(
        async () =>
          new Response(JSON.stringify(mockSession), {
            status: 200,
            headers: { 'Content-Type': 'application/json' },
          }),
      ) as typeof fetch;

      const result = await authService.login({
        email: 'user@example.com',
        password: 'password123',
      });

      expect(result.accessToken).toBe('session-jwt-token');
    });

    it('throws with server message on 401', async () => {
      globalThis.fetch = vi.fn(
        async () =>
          new Response(JSON.stringify({ message: 'Invalid credentials' }), {
            status: 401,
            headers: { 'Content-Type': 'application/json' },
          }),
      ) as typeof fetch;

      await expect(
        authService.login({ email: 'bad@example.com', password: 'wrong' }),
      ).rejects.toThrow('Invalid credentials');
    });

    it('throws a fallback message when server provides no message', async () => {
      globalThis.fetch = vi.fn(
        async () => new Response(null, { status: 500 }),
      ) as typeof fetch;

      await expect(
        authService.login({ email: 'user@example.com', password: 'password123' }),
      ).rejects.toThrow();
    });

    it('does not store a session when login fails', async () => {
      globalThis.fetch = vi.fn(
        async () => new Response(null, { status: 401 }),
      ) as typeof fetch;

      await authService.login({ email: 'user@example.com', password: 'wrong' }).catch(() => {});

      expect(getAccessToken()).toBeNull();
    });
  });

  describe('register', () => {
    it('calls POST /api/auth/register', async () => {
      globalThis.fetch = vi.fn(
        async () =>
          new Response(JSON.stringify(mockSession), {
            status: 201,
            headers: { 'Content-Type': 'application/json' },
          }),
      ) as typeof fetch;

      await authService.register({
        email: 'new@example.com',
        password: 'password123',
        role: 'PERSONAL',
      });

      const [url, init] = (globalThis.fetch as ReturnType<typeof vi.fn>).mock.calls[0];
      expect(url).toContain('/api/auth/register');
      expect(init.method).toBe('POST');
    });

    it('sends registration payload in the request body', async () => {
      globalThis.fetch = vi.fn(
        async () =>
          new Response(JSON.stringify(mockSession), {
            status: 201,
            headers: { 'Content-Type': 'application/json' },
          }),
      ) as typeof fetch;

      await authService.register({
        email: 'new@example.com',
        password: 'password123',
        role: 'BUSINESS',
      });

      const [, init] = (globalThis.fetch as ReturnType<typeof vi.fn>).mock.calls[0];
      const body = JSON.parse(init.body as string);
      expect(body.email).toBe('new@example.com');
      expect(body.role).toBe('BUSINESS');
    });

    it('stores the auth session on successful registration', async () => {
      globalThis.fetch = vi.fn(
        async () =>
          new Response(JSON.stringify(mockSession), {
            status: 201,
            headers: { 'Content-Type': 'application/json' },
          }),
      ) as typeof fetch;

      await authService.register({
        email: 'new@example.com',
        password: 'password123',
        role: 'PERSONAL',
      });

      expect(getAccessToken()).toBe('session-jwt-token');
    });

    it('throws with server message on 409 conflict', async () => {
      globalThis.fetch = vi.fn(
        async () =>
          new Response(JSON.stringify({ message: 'Email already in use' }), {
            status: 409,
            headers: { 'Content-Type': 'application/json' },
          }),
      ) as typeof fetch;

      await expect(
        authService.register({
          email: 'taken@example.com',
          password: 'password123',
          role: 'PERSONAL',
        }),
      ).rejects.toThrow('Email already in use');
    });
  });

  describe('logout', () => {
    it('clears the stored auth session', () => {
      storeAuthSession(mockSession);
      expect(getAccessToken()).toBe('session-jwt-token');

      authService.logout();

      expect(getAccessToken()).toBeNull();
    });

    it('is safe to call when not logged in', () => {
      expect(() => authService.logout()).not.toThrow();
    });
  });
});
