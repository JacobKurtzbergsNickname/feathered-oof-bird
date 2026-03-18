import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { AuthSession } from '../lib/auth';
import { apiFetch, readErrorMessage } from './httpClient';
import { clearStoredAuthSession, getAccessToken, storeAuthSession } from './authStorage';

const mockSession: AuthSession = {
  accessToken: 'bearer-token-xyz',
  tokenType: 'Bearer',
  expiresAt: '2026-12-31T23:59:59Z',
  user: {
    id: 1,
    email: 'test@example.com',
    role: 'PERSONAL',
    status: 'ACTIVE',
  },
};

describe('httpClient', () => {
  const originalFetch = globalThis.fetch;

  beforeEach(() => {
    vi.restoreAllMocks();
    clearStoredAuthSession();
  });

  afterEach(() => {
    globalThis.fetch = originalFetch;
  });

  describe('apiFetch', () => {
    it('prepends API_URL to relative paths', async () => {
      globalThis.fetch = vi.fn(
        async () => new Response(null, { status: 200 }),
      ) as typeof fetch;

      await apiFetch('/api/test');

      const [url] = (globalThis.fetch as ReturnType<typeof vi.fn>).mock.calls[0];
      expect(url).toContain('/api/test');
    });

    it('passes through absolute URLs unchanged', async () => {
      globalThis.fetch = vi.fn(
        async () => new Response(null, { status: 200 }),
      ) as typeof fetch;

      await apiFetch('http://other-host/path');

      const [url] = (globalThis.fetch as ReturnType<typeof vi.fn>).mock.calls[0];
      expect(url).toBe('http://other-host/path');
    });

    it('adds Authorization header when a session is stored', async () => {
      storeAuthSession(mockSession);
      globalThis.fetch = vi.fn(
        async () => new Response(null, { status: 200 }),
      ) as typeof fetch;

      await apiFetch('/api/protected');

      const [, init] = (globalThis.fetch as ReturnType<typeof vi.fn>).mock.calls[0];
      expect((init.headers as Headers).get('Authorization')).toBe('Bearer bearer-token-xyz');
    });

    it('omits Authorization header when no session is stored', async () => {
      globalThis.fetch = vi.fn(
        async () => new Response(null, { status: 200 }),
      ) as typeof fetch;

      await apiFetch('/api/public');

      const [, init] = (globalThis.fetch as ReturnType<typeof vi.fn>).mock.calls[0];
      expect((init.headers as Headers).get('Authorization')).toBeNull();
    });

    it('merges caller-supplied headers with the Authorization header', async () => {
      storeAuthSession(mockSession);
      globalThis.fetch = vi.fn(
        async () => new Response(null, { status: 200 }),
      ) as typeof fetch;

      await apiFetch('/api/test', {
        headers: { 'Content-Type': 'application/json' },
      });

      const [, init] = (globalThis.fetch as ReturnType<typeof vi.fn>).mock.calls[0];
      const headers = init.headers as Headers;
      expect(headers.get('Content-Type')).toBe('application/json');
      expect(headers.get('Authorization')).toBe('Bearer bearer-token-xyz');
    });

    it('clears the auth session on a 401 response', async () => {
      storeAuthSession(mockSession);
      globalThis.fetch = vi.fn(
        async () => new Response(null, { status: 401 }),
      ) as typeof fetch;

      await apiFetch('/api/protected');

      expect(getAccessToken()).toBeNull();
    });

    it('does not clear session for non-401 error responses', async () => {
      storeAuthSession(mockSession);
      globalThis.fetch = vi.fn(
        async () => new Response(null, { status: 403 }),
      ) as typeof fetch;

      await apiFetch('/api/protected');

      expect(getAccessToken()).toBe('bearer-token-xyz');
    });

    it('returns the response object', async () => {
      globalThis.fetch = vi.fn(
        async () => new Response(JSON.stringify({ ok: true }), { status: 200 }),
      ) as typeof fetch;

      const response = await apiFetch('/api/test');

      expect(response.status).toBe(200);
    });
  });

  describe('readErrorMessage', () => {
    it('returns the message field from a JSON response', async () => {
      const response = new Response(JSON.stringify({ message: 'Custom error' }), {
        status: 400,
        headers: { 'Content-Type': 'application/json' },
      });

      const msg = await readErrorMessage(response, 'fallback');

      expect(msg).toBe('Custom error');
    });

    it('returns the fallback when JSON has no message field', async () => {
      const response = new Response(JSON.stringify({ error: 'oops' }), {
        status: 400,
        headers: { 'Content-Type': 'application/json' },
      });

      const msg = await readErrorMessage(response, 'fallback');

      expect(msg).toBe('fallback');
    });

    it('returns the plain text body when Content-Type is not JSON', async () => {
      const response = new Response('Something went wrong', {
        status: 500,
        headers: { 'Content-Type': 'text/plain' },
      });

      const msg = await readErrorMessage(response, 'fallback');

      expect(msg).toBe('Something went wrong');
    });

    it('returns the fallback when the response body is empty', async () => {
      const response = new Response('', { status: 500 });

      const msg = await readErrorMessage(response, 'fallback');

      expect(msg).toBe('fallback');
    });
  });
});
