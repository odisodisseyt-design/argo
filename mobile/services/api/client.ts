import { credentials } from '@/services/storage/credentials';

const API_URL = process.env.EXPO_PUBLIC_API_URL || 'https://v3310666.hosted-by-vdsina.ru';
export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  if (!API_URL) throw new Error('EXPO_PUBLIC_API_URL is not configured');
  const token = await credentials.get();
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 30000);
  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}`, {
      ...init,
      signal: controller.signal,
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...init.headers
      }
    });
  } catch (error: any) {
    if (error?.name === 'AbortError') {
      throw new Error('Ответ не пришёл вовремя. Проверь соединение и попробуй ещё раз.');
    }
    throw new Error('Нет связи. Проверь соединение и попробуй ещё раз.');
  } finally {
    clearTimeout(timeout);
  }
  if (response.status === 401) {
    await credentials.clear();
    throw new Error('Сессия истекла. Войди заново.');
  }
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw Object.assign(new Error('Не удалось выполнить действие. Попробуй ещё раз.'), {
      status: response.status, body
    });
  }
  const body = await response.json().catch(() => ({}));
  if (body && typeof body === 'object' && 'error' in body) {
    const safe: Record<string, string> = {
      INVALID: 'Неверный логин или пароль.',
      ALREADY_EXISTS: 'Такой аккаунт уже есть.',
      NOT_IN_CAMPAIGN: 'Игра не найдена.',
      STEP_CHANGED: 'Шаг изменился. Попробуй ещё раз.',
      TEXT_TOO_LONG: 'Приказ слишком длинный.',
      RATE_LIMITED: 'Слишком много действий. Подожди немного.',
      NOT_FOUND: 'Этого предмета больше нет.',
      NOT_USABLE: 'Сейчас это не нужно.',
      NOT_EQUIPPABLE: 'Это нельзя надеть.',
      OUT_OF_STOCK: 'Товар закончился.',
      INSUFFICIENT_RESOURCE: 'Не хватает монет.',
      NOT_AVAILABLE: 'Этот товар пока недоступен.',
      STEP_RESOLVING: 'Мир ещё описывает предыдущий шаг. Подожди немного.',
    };
    const code = String((body as any).error || '');
    throw Object.assign(new Error(safe[code] || 'Не удалось выполнить действие.'), {
      status: response.status, body,
    });
  }
  return body as T;
}
