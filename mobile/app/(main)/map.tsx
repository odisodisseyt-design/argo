import React from 'react';
import { View, StyleSheet, ScrollView } from 'react-native';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { usePalette } from '@/theme/usePalette';
import { useCurrentGame } from '@/features/game/useCurrentGame';

const WIDTH = 980;
const HEIGHT = 420;

export default function MapScreen() {
  const p = usePalette();
  const { data } = useCurrentGame();
  const nodes = data?.map_nodes || [];
  const currentId = data?.anchor?.location?.id;
  const here = data?.anchor?.location?.name || 'Неизвестное место';

  const xs = nodes.map((n: any) => Number(n.x) || 0);
  const ys = nodes.map((n: any) => Number(n.y) || 0);
  const minX = Math.min(...xs, 0);
  const maxX = Math.max(...xs, 100);
  const minY = Math.min(...ys, 0);
  const maxY = Math.max(...ys, 100);
  const px = (x: number) => 40 + ((x - minX) / Math.max(1, maxX - minX)) * (WIDTH - 80);
  const py = (y: number) => 40 + ((y - minY) / Math.max(1, maxY - minY)) * (HEIGHT - 80);

  return (
    <Screen>
      <TopBar title="Карта" eyebrow={here} />
      <ScrollView horizontal showsHorizontalScrollIndicator={false}>
        <View style={[s.canvas, { backgroundColor: p.surface, borderColor: p.border, width: WIDTH, height: HEIGHT }]}>
          {nodes.filter((node: any) => node.visited || node.id === currentId).map((node: any) => {
            const isHere = node.id === currentId;
            return (
              <View key={node.id} style={[s.node, { left: px(node.x) - 8, top: py(node.y) - 8, backgroundColor: isHere ? p.accent : p.surface2 }]}>
                {isHere ? <AppText style={{ color: '#07131B', fontWeight: '900', fontSize: 10 }}>Я</AppText> : null}
              </View>
            );
          })}
          {nodes.filter((n: any) => n.visited || n.id === currentId).map((node: any) => (
            <View key={`label-${node.id}`} style={[s.label, { left: px(node.x) - 60, top: py(node.y) + 14 }]}>
              <AppText variant="muted">{node.name}</AppText>
            </View>
          ))}
        </View>
      </ScrollView>
      <Panel>
        <AppText variant="heading">Текущее место</AppText>
        <AppText variant="muted">{here}</AppText>
        <AppText variant="muted">Известно мест: {nodes.filter((n: any) => n.visited || n.id === currentId).length} из {nodes.length}</AppText>
      </Panel>
      <AppText variant="muted">Неизвестные места скрыты до посещения. Карта строится по координатам сервера.</AppText>
    </Screen>
  );
}

const s = StyleSheet.create({
  canvas: { borderWidth: 1, borderRadius: 20, overflow: 'hidden', marginBottom: 12 },
  node: { position: 'absolute', width: 16, height: 16, borderRadius: 8, alignItems: 'center', justifyContent: 'center', borderWidth: 2, borderColor: '#ffffff88' },
  label: { position: 'absolute', width: 120, alignItems: 'center' },
});
