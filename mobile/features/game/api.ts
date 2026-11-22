import { apiRequest } from '@/services/api/client';
import { credentials } from '@/services/storage/credentials';

export interface GameState {
  id: string;
  name: string;
  step_no: number;
  phase: string;
  waiting_for: string[];
  my_turn: boolean;
  anchor: any;
  character: any;
  living: any;
  presentation: any;
  lore: string;
  journal: any[];
  npcs: any[];
  map_nodes: any[];
  quests: any[];
  shop_items: any[];
  class_selection: any;
  model: any;
  language: string;
  pack_version: string;
  ui_manifest?: { features?: Record<string, boolean>; [key: string]: any };
}

export interface StepStatus {
  campaignId: string;
  step: number;
  phase: string;
  deadline: string | null;
  participants: Array<{ name: string; submitted: boolean; isYou: boolean }>;
  resolved_at: string | null;
  results_ready: boolean;
  my_turn: boolean;
}

export interface StepRecord {
  step_no: number;
  prose: string;
  outcome_text: string;
  created_at: string;
}

export async function login(username: string, password: string) {
  const result = await apiRequest<{ token: string; user: { id: number; username: string } }>(
    '/api/auth/login',
    { method: 'POST', body: JSON.stringify({ username, password }) },
  );
  await credentials.set(result.token);
  return result;
}

export async function register(username: string, password: string) {
  const result = await apiRequest<{ token: string; user: { id: number; username: string } }>(
    '/api/auth/register',
    { method: 'POST', body: JSON.stringify({ username, password }) },
  );
  await credentials.set(result.token);
  return result;
}

export async function logout() {
  await apiRequest('/api/auth/logout', { method: 'POST' }).catch(() => undefined);
  await credentials.clear();
}

export async function chooseClass(campaignId: string, classId: string) {
  return apiRequest<{ ok: boolean; class: any; character: any }>('/api/class/choose', {
    method: 'POST',
    body: JSON.stringify({ campaign_id: campaignId, class_id: classId }),
  });
}

export async function deleteCampaign(id: string) {
  return apiRequest<{ ok: boolean }>('/api/campaign/delete', {
    method: 'POST',
    body: JSON.stringify({ campaign_id: id }),
  });
}

export async function myCampaigns() {
  return apiRequest<{ campaigns: GameState[] }>('/api/campaigns/mine');
}

export async function campaign(id: string) {
  return apiRequest<GameState>(`/api/campaign/${id}`);
}

export async function stepStatus(id: string) {
  return apiRequest<StepStatus>(`/api/campaign/${id}/step/status`);
}

export async function steps(id: string) {
  return apiRequest<{ steps: StepRecord[] }>(`/api/campaign/${id}/steps?limit=20`);
}

export async function models() {
  return apiRequest<{ models: Array<{ id: string; title: string; strength: number; speed: number; cost: string; recommended_for: string; available: boolean }> }>('/api/models');
}

export async function newCampaign(name: string, modelId: string) {
  return apiRequest<{ campaign_id: string }>('/api/campaign/new', {
    method: 'POST',
    body: JSON.stringify({ name, pack_id: 'refuge', model_id: modelId }),
  });
}

export async function events(id: string, after: number) {
  return apiRequest<{ events: Array<{ seq: number; event: string; payload: any }> }>(
    `/api/campaign/${id}/events?after=${after}`,
  );
}

export async function submitTurn(campaignId: string, command: string, key: string) {
  return apiRequest<{ order_saved: boolean; clarifying_question?: string; preview?: unknown }>(
    '/api/turn',
    {
      method: 'POST',
      headers: { 'Idempotency-Key': key },
      body: JSON.stringify({ campaign_id: campaignId, command }),
    },
  );
}
