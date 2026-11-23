import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { usePalette } from '@/theme/usePalette';
import { useCurrentGame } from '@/features/game/useCurrentGame';

const KIND: Record<string, string> = { main: 'ГЛАВНОЕ', side: 'ДОПОЛНИТЕЛЬНОЕ' };

export default function Quests() {
  const p = usePalette();
  const { data } = useCurrentGame();
  const quests = data?.quests || [];
  const active = quests.filter((q: any) => !q.complete);
  const done = quests.filter((q: any) => q.complete);

  const card = (quest: any) => (
    <Panel key={quest.id} style={{ gap: 7, opacity: quest.complete ? 0.6 : 1 }}>
      <AppText variant="label" style={{ letterSpacing: 1.5, color: quest.complete ? p.success : p.accent }}>
        {quest.complete ? 'ВЫПОЛНЕНО' : KIND[quest.kind] || 'ЗАДАНИЕ'}
      </AppText>
      <AppText variant="heading">{quest.title || quest.name}</AppText>
      {quest.description ? <AppText variant="muted">{quest.description}</AppText> : null}
      {(quest.objectives || []).map((obj: any, i: number) => (
        <View key={i} style={s.objective}>
          <AppText style={{ color: obj.done ? p.success : p.muted }}>{obj.done ? '✓' : '○'}</AppText>
          <AppText variant="muted">{obj.text}</AppText>
        </View>
      ))}
    </Panel>
  );

  return (
    <Screen>
      <TopBar title="Задания" eyebrow={`${active.length} активных`} />
      {active.map(card)}
      {done.length ? <AppText variant="heading">Выполненные</AppText> : null}
      {done.map(card)}
      {!quests.length && <AppText variant="muted">Активных заданий нет. Исследуй мир.</AppText>}
    </Screen>
  );
}

const s = StyleSheet.create({
  objective: { flexDirection: 'row', gap: 8, alignItems: 'flex-start' },
});
