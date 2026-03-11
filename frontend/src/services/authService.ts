import type { AuthSession, LoginRequest, RegisterRequest } from '../lib/auth';
import { authSession } from '../stores/authStore';
import { readErrorMessage } from './httpClient';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

async function submitAuthRequest(path: string, payload: LoginRequest | RegisterRequest) {
  const response = await fetch(`${API_URL}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, 'Authentication request failed.'));
  }

  const session = (await response.json()) as AuthSession;
  authSession.set(session);
  return session;
}

export const authService = {
  async register(payload: RegisterRequest) {
    return submitAuthRequest('/api/auth/register', payload);
  },

  async login(payload: LoginRequest) {
    return submitAuthRequest('/api/auth/login', payload);
  },

  logout() {
    authSession.clear();
  },
};