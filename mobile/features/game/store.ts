import { create } from 'zustand';
import AsyncStorage from '@react-native-async-storage/async-storage';

interface GameState {
  activeCampaignId: string | null;
  features: Record<string, boolean>;
  setActiveCampaign: (id: string) => Promise<void>;
  setFeatures: (features: Record<string, boolean>) => void;
}

export const useGame = create<GameState>((set) => ({
  activeCampaignId: null,
  features: {},
  setActiveCampaign: async (id) => {
    await AsyncStorage.setItem('argo.active_campaign', id);
    set({ activeCampaignId: id });
  },
  setFeatures: (features) => set({ features }),
}));

export async function restoreActiveCampaign() {
  const id = await AsyncStorage.getItem('argo.active_campaign');
  if (id) useGame.setState({ activeCampaignId: id });
}
