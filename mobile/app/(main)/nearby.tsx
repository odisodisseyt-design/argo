import React from 'react';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { usePalette } from '@/theme/usePalette';
import { useCurrentGame } from '@/features/game/useCurrentGame';

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
        <AppText variant="muted">{data?.living?.weather || ''} · {data?.living?.time_of_day || ''}</AppText>
      </Panel>
      {nearby.map((item: any, index: number) => (
        <Panel key={`${item.id}-${index}`}>
          <AppText variant="heading">{item.name}</AppText>
          <AppText variant="muted">{item.distance_m || '?'} м · {item.direction || ''} · {item.kind || ''}</AppText>
        </Panel>
      ))}
      {!nearby.length && <AppText variant="muted">Рядом ничего не обнаружено. Осмотрись.</AppText>}
    </Screen>
  );
}
