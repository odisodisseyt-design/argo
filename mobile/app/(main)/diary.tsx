import React, { useMemo, useState } from 'react';
import { View, Pressable, StyleSheet } from 'react-native';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { usePalette } from '@/theme/usePalette';
import { useCurrentGame } from '@/features/game/useCurrentGame';

const STATUS: Record<string, string> = {
  canon: 'Случилось', pending: 'Предположение', rejected: 'Не сбылось',
};

export default function Diary() {
  const p = usePalette();
  const { data } = useCurrentGame();
  const [showAll, setShowAll] = useState(false);
  const journal = data?.journal || [];
  const visible = showAll ? journal : journal.slice(0, 30);
  const groups = useMemo(() => {
    const result: Array<{ step: number; entries: any[] }> = [];
    for (const entry of visible) {
      const step = Number(entry.step_no) || 0;
      const last = result[result.length - 1];
      if (last && last.step === step) last.entries.push(entry);
      else result.push({ step, entries: [entry] });
    }
    return result;
  }, [visible]);

  return (
    <Screen>
      <TopBar title="Дневник" eyebrow={`${journal.length} записей`} />
      <Panel>
        <AppText variant="label" style={{ color: p.accent }}>ХРОНИКА</AppText>
        <AppText variant="muted">То, что уже вошло в историю этого мира.</AppText>
      </Panel>

      {groups.map((group) => (
        <View key={group.step} style={s.group}>
          <AppText variant="label" style={{ color: p.muted }}>
            {group.step ? `ШАГ ${group.step}` : 'НАЧАЛО'}
          </AppText>
          {group.entries.map((entry) => (
            <Panel key={entry.id} style={s.entry}>
              <AppText>{entry.text}</AppText>
              {entry.status && entry.status !== 'canon' ? (
                <AppText variant="muted">{STATUS[entry.status] || 'Запись'}</AppText>
              ) : null}
            </Panel>
          ))}
        </View>
      ))}
      {!journal.length && <AppText variant="muted">Первые события появятся после твоего приказа.</AppText>}

      {journal.length > 30 && (
        <Pressable onPress={() => setShowAll(!showAll)}>
          <Panel>
            <AppText style={{ color: p.accent, textAlign: 'center' }}>
              {showAll ? 'Свернуть' : `Показать все записи (${journal.length})`}
            </AppText>
          </Panel>
        </Pressable>
      )}
    </Screen>
  );
}

const s = StyleSheet.create({
  group: { gap: 7 },
  entry: { gap: 4 },
});
