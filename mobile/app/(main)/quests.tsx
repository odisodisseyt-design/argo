import React from 'react';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { useCurrentGame } from '@/features/game/useCurrentGame';

export default function Quests() {
  const { data } = useCurrentGame();
  const quests = data?.quests || [];
  return (
    <Screen>
      <TopBar title="Задания" />
      {quests.map((quest: any, index: number) => (
        <Panel key={quest.id || index}>
          <AppText variant="label" style={{ letterSpacing: 1.5 }}>{quest.kind || 'ЗАДАНИЕ'}</AppText>
          <AppText variant="heading">{quest.title || quest.name}</AppText>
          <AppText variant="muted">{quest.description || quest.desc || ''}</AppText>
        </Panel>
      ))}
      {!quests.length && <AppText variant="muted">Активных заданий нет. Исследуй мир.</AppText>}
    </Screen>
  );
}
