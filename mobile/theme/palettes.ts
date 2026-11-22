import type { ThemeName } from '@/types/api';

export const palettes = {
  system: {
    label: 'SYSTEM', background: '#080D15', surface: '#101925', surface2: '#162333',
    border: '#26384B', text: '#EDF5FF', muted: '#91A4B9', accent: '#55C8F2',
    accentSoft: '#16394A', gold: '#D7B56D', success: '#67D7A0', danger: '#FF777E',
    purple: '#B69BFF', input: '#0B131E'
  },
  fantasy: {
    label: 'FANTASY', background: '#15100D', surface: '#211914', surface2: '#302219',
    border: '#57402C', text: '#F6EBDD', muted: '#BDAA91', accent: '#D9A85F',
    accentSoft: '#47311F', gold: '#E5C27B', success: '#91B88A', danger: '#D97968',
    purple: '#B99AD8', input: '#1A130F'
  },
  scifi: {
    label: 'SCI-FI', background: '#071116', surface: '#0D2029', surface2: '#12313D',
    border: '#1E5362', text: '#E7FBFF', muted: '#8DBAC4', accent: '#54F0D0',
    accentSoft: '#123C3D', gold: '#C6F17A', success: '#54D9A8', danger: '#FF788F',
    purple: '#86A9FF', input: '#091A21'
  },
  dark: {
    label: 'DARK', background: '#0D0B10', surface: '#19151E', surface2: '#241D2B',
    border: '#413448', text: '#F3ECF7', muted: '#AA9BAF', accent: '#C6A0E8',
    accentSoft: '#35243F', gold: '#D8B36C', success: '#83C9A1', danger: '#E77D8B',
    purple: '#C6A0E8', input: '#120F17'
  }
} as const;

export type Palette = typeof palettes[ThemeName];
