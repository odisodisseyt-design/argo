import { credentials } from '@/services/storage/credentials';

const API_URL = process.env.EXPO_PUBLIC_API_URL || 'https://v3310666.hosted-by-vdsina.ru';
export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  if (!API_URL) throw new Error('EXPO_PUBLIC_API_URL is not configured');
  const token = await credentials.get();
  const response = await fetch(`${API_URL}${path}`, {
    ...init,
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init.headers
    }
  });
  if (response.status === 401) {
    await credentials.clear();
    throw new Error('Сессия истекла. Войди заново.');
  }
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    const detail = typeof body.detail === 'string' ? body.detail : '';
    throw Object.assign(new Error(body.message || detail || 'Сервер временно недоступен.'), {
      status: response.status, body
    });
  }
  return response.json() as Promise<T>;
}
