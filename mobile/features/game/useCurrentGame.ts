import { useQuery } from '@tanstack/react-query';
import { campaign, steps } from './api';
import { useGame } from './store';

export function useCurrentGame() {
  const id = useGame((s) => s.activeCampaignId);
  const game = useQuery({ queryKey: ['campaign', id], queryFn: () => campaign(id!), enabled: Boolean(id) });
  const history = useQuery({ queryKey: ['steps', id], queryFn: () => steps(id!), enabled: Boolean(id) });
  return { id, game, history, data: game.data };
}
