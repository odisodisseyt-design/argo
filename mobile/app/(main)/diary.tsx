import React from 'react';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { useCurrentGame } from '@/features/game/useCurrentGame';

export default function Diary() {
  const { data } = useCurrentGame();
  const journal = data?.journal || [];
  return (
    <Screen>
      <TopBar title="Дневник" />
      <Panel>
        <AppText variant="label">ХРОНИКА МИРА</AppText>
        <AppText variant="muted">Записи создаёт сервер. Клиент их только читает.</AppText>
      </Panel>
      {journal.map((entry) => (
        <Panel key={entry.id}>
          <AppText>{entry.text}</AppText>
        </Panel>
      ))}
      {!journal.length && <AppText variant="muted">Первые события появятся после хода.</AppText>}
    </Screen>
  );
}
