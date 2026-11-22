import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { usePalette } from '@/theme/usePalette';
import { useCurrentGame } from '@/features/game/useCurrentGame';

export default function MapScreen() {
  const p = usePalette();
  const { data } = useCurrentGame();
  const nodes = data?.map_nodes || [];
  const here = data?.anchor?.location?.name || 'Неизвестное место';
  return (
    <Screen>
      <TopBar title="Карта" />
      <View style={[s.map, { backgroundColor: p.surface, borderColor: p.border }]}>
        <View style={s.grid}>
          {Array.from({ length: 64 }, (_, i) => (
            <View key={i} style={[s.cell, { borderColor: p.border, backgroundColor: i % 7 === 0 ? p.surface2 : 'transparent' }]} />
          ))}
        </View>
        <View style={[s.pin, { backgroundColor: p.accent }]}><AppText style={{ color: '#07131B', fontWeight: '900' }}>Я</AppText></View>
        <View style={s.mapLabel}><AppText variant="label">{here}</AppText></View>
      </View>
      <Panel>
        <AppText variant="heading">Известные места</AppText>
        {nodes.map((node: any) => (
          <View key={node.id} style={s.row}>
            <AppText>{node.name}</AppText>
            <AppText variant="muted">{node.visited ? 'посещено' : 'неизвестно'}</AppText>
          </View>
        ))}
        {!nodes.length && <AppText variant="muted">Карта откроется по мере исследования мира.</AppText>}
      </Panel>
    </Screen>
  );
}

const s = StyleSheet.create({
  map: { height: 320, borderWidth: 1, borderRadius: 20, overflow: 'hidden', justifyContent: 'center', alignItems: 'center' },
  grid: { position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, flexDirection: 'row', flexWrap: 'wrap' },
  cell: { width: '12.5%', height: '12.5%', borderWidth: 0.5 },
  pin: { width: 38, height: 38, borderRadius: 20, alignItems: 'center', justifyContent: 'center', borderWidth: 4, borderColor: '#FFFFFF44' },
  mapLabel: { position: 'absolute', bottom: 14, left: 14, backgroundColor: '#0008', padding: 8, borderRadius: 7 },
  row: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 8 },
});
