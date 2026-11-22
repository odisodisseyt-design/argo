// Hand-aligned with the server DTO until OpenAPI generation is wired in.
export type CampaignPhase = 'orders' | 'waiting' | 'resolving' | 'results';
export type ThemeName = 'system' | 'fantasy' | 'scifi' | 'dark';

export interface ParticipantStatus {
  name: string;
  submitted: boolean;
  isYou: boolean;
}

export interface StepStatus {
  campaignId: string;
  step: number;
  phase: string;
  deadline: string | null;
  participants: ParticipantStatus[];
  resolved_at: string | null;
  results_ready: boolean;
  my_turn: boolean;
}
