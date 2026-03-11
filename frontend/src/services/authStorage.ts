import type { AuthSession } from '../lib/auth';

const SESSION_KEY = 'feathered-oof-bird.auth.session';

let inMemorySession: AuthSession | null = null;

function canUseSessionStorage() {
  return typeof window !== 'undefined' && typeof window.sessionStorage !== 'undefined';
}

export function loadStoredAuthSession(): AuthSession | null {
  if (inMemorySession) {
    return inMemorySession;
  }

  if (!canUseSessionStorage()) {
    return null;
  }

  const rawValue = window.sessionStorage.getItem(SESSION_KEY);
  if (!rawValue) {
    return null;
  }

  try {
    inMemorySession = JSON.parse(rawValue) as AuthSession;
    return inMemorySession;
  } catch {
    window.sessionStorage.removeItem(SESSION_KEY);
    return null;
  }
}

export function storeAuthSession(session: AuthSession) {
  inMemorySession = session;
  if (canUseSessionStorage()) {
    window.sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
  }
}

export function clearStoredAuthSession() {
  inMemorySession = null;
  if (canUseSessionStorage()) {
    window.sessionStorage.removeItem(SESSION_KEY);
  }
}

export function getAccessToken() {
  return loadStoredAuthSession()?.accessToken ?? null;
}