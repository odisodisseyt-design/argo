import React from 'react';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { usePalette } from '@/theme/usePalette';
import { useCurrentGame } from '@/features/game/useCurrentGame';

const KINDS: Record<string, string> = {
  exit: 'выход', mob: 'угроза', npc: 'человек', item: 'предмет',
  object: 'предмет', corpse: 'тело', container: 'тайник', resource: 'ресурс',
};
const WEATHER: Record<string, string> = {
  rain: 'дождь', clear: 'ясно', overcast: 'пасмурно', fog: 'туман', snow: 'снег', storm: 'гроза',
};
const DAYTIME: Record<string, string> = {
  day: 'день', morning: 'утро', evening: 'вечер', night: 'ночь',
};

export default function Nearby() {
  const p = usePalette();
  const { data } = useCurrentGame();
  const nearby = data?.anchor?.nearby || [];
  return (
    <Screen>
      <TopBar title="Объекты рядом" />
      <Panel>
        <AppText variant="label" style={{ color: p.accent }}>МЕСТОПОЛОЖЕНИЕ</AppText>
        <AppText variant="heading">{data?.anchor?.location?.name || 'Неизвестно'}</AppText>
        <AppText variant="muted">
          {WEATHER[data?.living?.weather] || ''} · {DAYTIME[data?.living?.time_of_day] || ''}
        </AppText>
      </Panel>
      {nearby.map((item: any, index: number) => (
        <Panel key={`${item.id}-${index}`}>
          <AppText variant="heading">{item.name}</AppText>
          <AppText variant="muted">
            {item.distance_m || '?'} м · {item.direction || ''} · {KINDS[item.kind] || 'объект'}
          </AppText>
        </Panel>
      ))}
      {!nearby.length && <AppText variant="muted">Рядом ничего не обнаружено. Осмотрись.</AppText>}
    </Screen>
  );
}
