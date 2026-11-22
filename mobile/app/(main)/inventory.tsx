import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { usePalette } from '@/theme/usePalette';
import { useCurrentGame } from '@/features/game/useCurrentGame';

export default function Inventory() {
  const p = usePalette();
  const { data } = useCurrentGame();
  const items = data?.character?.inventory || [];
  const equipment = data?.character?.equipment || {};
  return (
    <Screen>
      <TopBar title="Инвентарь" />
      <Panel>
        <AppText variant="label" style={{ color: p.accent }}>СНАРЯЖЕНИЕ</AppText>
        {Object.entries(equipment).length ? Object.entries(equipment).map(([slot, item]) => (
          <View key={slot} style={s.row}><AppText variant="muted">{slot}</AppText><AppText>{String(item)}</AppText></View>
        )) : <AppText variant="muted">Пусто</AppText>}
      </Panel>
      <AppText variant="heading">Предметы · {items.length}</AppText>
      <View style={s.grid}>
        {items.map((item: any) => (
          <Panel key={item.id} style={s.item}>
            <AppText variant="heading">{item.name}</AppText>
            <AppText variant="muted">{item.quantity || 1} шт · {item.weight_kg || 0} кг</AppText>
          </Panel>
        ))}
        {!items.length && <AppText variant="muted">В инвентаре пока пусто.</AppText>}
      </View>
    </Screen>
  );
}

const s = StyleSheet.create({
  row: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 7 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  item: { width: '47%', minHeight: 92 },
});
