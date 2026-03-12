import { writable } from 'svelte/store';
import type { AuthSession } from '../lib/auth';
import {
  clearStoredAuthSession,
  loadStoredAuthSession,
  storeAuthSession,
} from '../services/authStorage';

const store = writable<AuthSession | null>(loadStoredAuthSession());

export const authSession = {
  subscribe: store.subscribe,
  set(session: AuthSession) {
    storeAuthSession(session);
    store.set(session);
  },
  clear() {
    clearStoredAuthSession();
    store.set(null);
  },
};