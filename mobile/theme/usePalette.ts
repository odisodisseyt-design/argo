import { palettes } from '@/theme/palettes';
import { usePreferences } from '@/store/preferences';
export function usePalette() {
  const theme = usePreferences((s) => s.theme);
  return palettes[theme];
}
