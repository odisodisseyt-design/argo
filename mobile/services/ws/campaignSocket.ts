import { credentials } from '@/services/storage/credentials';

export interface WsMessage { event: string; payload: any }

function wsBase() {
  const api = process.env.EXPO_PUBLIC_API_URL || '';
  const ws = (process.env.EXPO_PUBLIC_WS_URL || api)
    .replace(/^http:/, 'ws:')
    .replace(/^https:/, 'wss:');
  if (!/^wss?:\/\//.test(ws)) {
    throw new Error('Не настроен адрес сервера (EXPO_PUBLIC_API_URL)');
  }
  return ws;
}

export function createCampaignSocket(
  campaignId: string,
  onMessage: (message: WsMessage) => void,
) {
  let socket: WebSocket | null = null;
  let stopped = false;
  let attempt = 0;
  let timer: ReturnType<typeof setTimeout> | undefined;

  const connect = async () => {
    if (stopped) return;
    const token = await credentials.get();
    if (!token) {
      if (!stopped) timer = setTimeout(connect, 2000);
      return;
    }
    socket = new WebSocket(`${wsBase()}/ws/campaign/${campaignId}?token=${encodeURIComponent(token)}`);
    socket.onopen = () => { attempt = 0; };
    socket.onmessage = (event) => {
      try { onMessage(JSON.parse(String(event.data)) as WsMessage); } catch { /* ignore */ }
    };
    socket.onclose = () => {
      if (stopped) return;
      const delay = Math.min(30_000, 800 * (2 ** attempt++));
      timer = setTimeout(connect, delay);
    };
    socket.onerror = () => socket?.close();
  };
  void connect();

  return {
    close: () => {
      stopped = true;
      if (timer) clearTimeout(timer);
      socket?.close();
    },
  };
}
