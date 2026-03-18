import { beforeEach, describe, expect, it } from 'vitest';
import type { AuthSession } from '../lib/auth';
import {
  clearStoredAuthSession,
  getAccessToken,
  loadStoredAuthSession,
  storeAuthSession,
} from './authStorage';

const mockSession: AuthSession = {
  accessToken: 'test-token-abc',
  tokenType: 'Bearer',
  expiresAt: '2026-12-31T23:59:59Z',
  user: {
    id: 1,
    email: 'test@example.com',
    role: 'PERSONAL',
    status: 'ACTIVE',
  },
};

describe('authStorage', () => {
  beforeEach(() => {
    clearStoredAuthSession();
  });

  describe('loadStoredAuthSession', () => {
    it('returns null when nothing has been stored', () => {
      expect(loadStoredAuthSession()).toBeNull();
    });

    it('returns the session after storeAuthSession is called', () => {
      storeAuthSession(mockSession);

      expect(loadStoredAuthSession()).toEqual(mockSession);
    });

    it('returns null after clearStoredAuthSession is called', () => {
      storeAuthSession(mockSession);
      clearStoredAuthSession();

      expect(loadStoredAuthSession()).toBeNull();
    });
  });

  describe('storeAuthSession', () => {
    it('persists the session so it can be retrieved', () => {
      storeAuthSession(mockSession);

      expect(loadStoredAuthSession()).toEqual(mockSession);
    });

    it('overwrites any previously stored session', () => {
      storeAuthSession(mockSession);
      const updated: AuthSession = { ...mockSession, accessToken: 'updated-token' };
      storeAuthSession(updated);

      expect(loadStoredAuthSession()?.accessToken).toBe('updated-token');
    });
  });

  describe('clearStoredAuthSession', () => {
    it('removes a stored session', () => {
      storeAuthSession(mockSession);
      clearStoredAuthSession();

      expect(loadStoredAuthSession()).toBeNull();
    });

    it('is safe to call when nothing is stored', () => {
      expect(() => clearStoredAuthSession()).not.toThrow();
    });
  });

  describe('getAccessToken', () => {
    it('returns null when no session is stored', () => {
      expect(getAccessToken()).toBeNull();
    });

    it('returns the access token from the stored session', () => {
      storeAuthSession(mockSession);

      expect(getAccessToken()).toBe('test-token-abc');
    });

    it('returns null after session is cleared', () => {
      storeAuthSession(mockSession);
      clearStoredAuthSession();

      expect(getAccessToken()).toBeNull();
    });
  });
});
