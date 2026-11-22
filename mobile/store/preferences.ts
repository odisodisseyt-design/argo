import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';
import type { ThemeName } from '@/types/api';

interface PreferencesState {
  theme: ThemeName;
  fontScale: number;
  haptics: boolean;
  illustrations: boolean;
  sound: boolean;
  setTheme: (theme: ThemeName) => void;
  setFontScale: (scale: number) => void;
  toggle: (key: 'haptics' | 'illustrations' | 'sound') => void;
}
export const usePreferences = create<PreferencesState>()(persist((set) => ({
  theme: 'system', fontScale: 1, haptics: true, illustrations: true, sound: true,
  setTheme: (theme) => set({ theme }),
  setFontScale: (fontScale) => set({ fontScale }),
  toggle: (key) => set((s) => ({ [key]: !s[key] }))
}), { name: 'argo-preferences', storage: createJSONStorage(() => AsyncStorage) }));
