import React, { useState } from 'react';
import { View, StyleSheet, Pressable } from 'react-native';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { usePalette } from '@/theme/usePalette';
import { useCurrentGame } from '@/features/game/useCurrentGame';

export default function Inventory() {
  const p = usePalette();
  const { data } = useCurrentGame();
  const [expanded, setExpanded] = useState(false);
  const items = [...(data?.character?.inventory || [])].sort((a: any, b: any) =>
    String(a.name || '').localeCompare(String(b.name || ''), 'ru'));
  const equipment = data?.character?.equipment || {};
  const visible = expanded ? items : items.slice(0, 8);

  return (
    <Screen>
      <TopBar title="Инвентарь" eyebrow={`${items.length} предметов`} />
      <Panel>
        <AppText variant="label" style={{ color: p.accent }}>СНАРЯЖЕНИЕ</AppText>
        {Object.entries(equipment).length ? Object.entries(equipment).map(([slot, item]) => (
          <View key={slot} style={s.row}><AppText variant="muted">{slot}</AppText><AppText>{String(item)}</AppText></View>
        )) : <AppText variant="muted">Пусто</AppText>}
      </Panel>
      <AppText variant="heading">Предметы · {items.length}</AppText>
      <View style={s.grid}>
        {visible.map((item: any) => (
          <Panel key={item.id} style={s.item}>
            <AppText variant="heading">{item.name}</AppText>
            <AppText variant="muted">{item.quantity || 1} шт · {item.weight_kg || 0} кг</AppText>
          </Panel>
        ))}
        {!items.length && <AppText variant="muted">В инвентаре пока пусто.</AppText>}
      </View>
      {items.length > 8 && (
        <Pressable onPress={() => setExpanded(!expanded)}>
          <Panel>
            <AppText style={{ color: p.accent, textAlign: 'center' }}>
              {expanded ? 'Свернуть' : `Показать ещё ${items.length - 8}`}
            </AppText>
          </Panel>
        </Pressable>
      )}
      <AppText variant="muted">Состав инвентаря приходит от сервера. Клиент не меняет предметы сам.</AppText>
    </Screen>
  );
}

const s = StyleSheet.create({
  row: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 7 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  item: { width: '47%', minHeight: 92 },
});
