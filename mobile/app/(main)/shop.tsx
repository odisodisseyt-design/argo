import React from 'react';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { useCurrentGame } from '@/features/game/useCurrentGame';

export default function Shop() {
  const { data } = useCurrentGame();
  const items = data?.shop_items || [];
  return (
    <Screen>
      <TopBar title="Магазин" />
      <Panel>
        <AppText variant="muted">Кошелёк: {JSON.stringify(data?.character?.currency || {})}</AppText>
      </Panel>
      {items.map((item: any) => (
        <Panel key={item.id}>
          <AppText variant="heading">{item.name}</AppText>
          <AppText variant="muted">{item.description || ''}</AppText>
        </Panel>
      ))}
      {!items.length && <AppText variant="muted">Торговец пока не появился.</AppText>}
    </Screen>
  );
}
