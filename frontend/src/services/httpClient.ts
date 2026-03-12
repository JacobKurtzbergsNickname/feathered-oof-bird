import { getAccessToken } from './authStorage';
import { authSession } from '../stores/authStore';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

function buildUrl(path: string) {
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path;
  }
  return `${API_URL}${path}`;
}

function mergeHeaders(headers?: HeadersInit) {
  const merged = new Headers(headers);
  const token = getAccessToken();

  if (token) {
    merged.set('Authorization', `Bearer ${token}`);
  }

  return merged;
}

export async function apiFetch(path: string, init: RequestInit = {}) {
  const response = await fetch(buildUrl(path), {
    ...init,
    headers: mergeHeaders(init.headers),
  });

  if (response.status === 401) {
    authSession.clear();
  }

  return response;
}

export async function readErrorMessage(response: Response, fallbackMessage: string) {
  const contentType = response.headers.get('Content-Type') ?? '';
  if (contentType.includes('application/json')) {
    const payload = (await response.json()) as { message?: string };
    return payload.message ?? fallbackMessage;
  }

  const text = await response.text();
  return text || fallbackMessage;
}