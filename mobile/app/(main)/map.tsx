import React, { useMemo, useState } from 'react';
import { View, StyleSheet, ScrollView, Pressable, Alert } from 'react-native';
import { router } from 'expo-router';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { ActionButton } from '@/components/ui/ActionButton';
import { usePalette } from '@/theme/usePalette';
import { useCurrentGame } from '@/features/game/useCurrentGame';
import { submitTurn } from '@/features/game/api';

const WIDTH = 980;
const HEIGHT = 420;

const DANGER: Record<string, string> = {
  safe: 'Спокойно', calm: 'Спокойно', low: 'Опасно чуть-чуть',
  medium: 'Опасно', high: 'Очень опасно', deadly: 'Смертельно',
};
const BIOME: Record<string, string> = {
  street: 'Улица', indoors: 'Помещение', metro: 'Метро', sewer: 'Канализация',
  forest: 'Лес', wasteland: 'Пустошь', ocean: 'Вода', techno: 'Металл',
};

export default function MapScreen() {
  const p = usePalette();
  const queryClient = useQueryClient();
  const { data, id } = useCurrentGame();
  const [selected, setSelected] = useState<any>(null);
  const nodes = data?.map_nodes || [];
  const currentId = data?.anchor?.location?.id;
  const here = data?.anchor?.location?.name || 'Неизвестное место';
  const known = nodes.filter((node: any) => node.visited || node.id === currentId);
  const current = nodes.find((node: any) => node.id === currentId);

  const xs = known.map((n: any) => Number(n.x) || 0);
  const ys = known.map((n: any) => Number(n.y) || 0);
  const minX = Math.min(...xs, 0);
  const maxX = Math.max(...xs, 100);
  const minY = Math.min(...ys, 0);
  const maxY = Math.max(...ys, 100);
  const singleX = maxX - minX < 0.001;
  const singleY = maxY - minY < 0.001;
  const px = (x: number) => singleX ? WIDTH / 2 : 40 + ((x - minX) / (maxX - minX)) * (WIDTH - 80);
  const py = (y: number) => singleY ? HEIGHT / 2 : 40 + ((y - minY) / (maxY - minY)) * (HEIGHT - 80);

  const walk = useMutation({
    mutationFn: (name: string) => {
      const key = Math.random().toString(36).slice(2) + Date.now().toString(36);
      return submitTurn(id!, `Идти: ${name}`, key);
    },
    onSuccess: (result: any) => {
      if (result.error) {
        Alert.alert('Не вышло', result.message || 'Попробуй ещё раз.');
        return;
      }
      if (result.clarifying_question) {
        Alert.alert('Уточни', result.clarifying_question);
        return;
      }
      void queryClient.invalidateQueries({ queryKey: ['campaign', id] });
      void queryClient.invalidateQueries({ queryKey: ['step', id] });
      void queryClient.invalidateQueries({ queryKey: ['steps', id] });
      setSelected(null);
      router.back();
    },
    onError: (error: any) => Alert.alert('Не вышло', error?.message || 'Попробуй ещё раз.'),
  });

  const reachable = useMemo(
    () => known.filter((node: any) => current?.connections?.includes(node.id)),
    [known, current],
  );

  return (
    <Screen>
      <TopBar title="Карта" eyebrow={here} />
      <ScrollView horizontal showsHorizontalScrollIndicator={false}>
        <View style={[s.canvas, { backgroundColor: p.surface, borderColor: p.border, width: WIDTH, height: HEIGHT }]}>
          {known.map((node: any) => {
            const isHere = node.id === currentId;
            return (
              <Pressable key={node.id} onPress={() => setSelected(node)}
                style={[s.node, {
                  left: px(node.x) - 9, top: py(node.y) - 9,
                  backgroundColor: selected?.id === node.id ? p.gold : isHere ? p.accent : p.surface2,
                  borderColor: isHere ? p.text : p.border,
                }]}>
                {isHere ? <AppText style={{ color: '#07131B', fontWeight: '900', fontSize: 10 }}>Я</AppText> : null}
              </Pressable>
            );
          })}
          {known.map((node: any) => (
            <Pressable key={`label-${node.id}`} onPress={() => setSelected(node)}
              style={[s.label, { left: px(node.x) - 60, top: py(node.y) + 14 }]}>
              <AppText variant="muted">{node.name}</AppText>
            </Pressable>
          ))}
        </View>
      </ScrollView>

      <Panel>
        <AppText variant="heading">{selected ? selected.name : here}</AppText>
        {selected ? (
          <>
            <AppText variant="muted">
              {selected.id === currentId ? 'Ты здесь'
                : reachable.some((n: any) => n.id === selected.id) ? 'Дорога известна' : 'Дорога пока не найдена'}
            </AppText>
            {selected.danger ? <AppText variant="muted">Опасность: {DANGER[selected.danger] || 'Неизвестно'}</AppText> : null}
            {selected.biome ? <AppText variant="muted">Место: {BIOME[selected.biome] || 'Неизвестно'}</AppText> : null}
            {selected.id !== currentId && reachable.some((n: any) => n.id === selected.id) ? (
              <ActionButton title={walk.isPending ? 'Идём…' : 'Идти сюда'}
                onPress={() => walk.mutate(selected.name)} />
            ) : null}
          </>
        ) : (
          <AppText variant="muted">Коснись точки, чтобы узнать подробности.</AppText>
        )}
        <AppText variant="muted">Известно мест: {known.length} из {nodes.length}</AppText>
      </Panel>

      {reachable.length > 0 && (
        <Panel>
          <AppText variant="label" style={{ color: p.accent }}>ОТСЮДА МОЖНО ПОЙТИ</AppText>
          {reachable.map((node: any) => (
            <Pressable key={node.id} onPress={() => setSelected(node)} style={s.row}>
              <AppText>{node.name}</AppText>
              <AppText variant="muted">→</AppText>
            </Pressable>
          ))}
        </Panel>
      )}
    </Screen>
  );
}

const s = StyleSheet.create({
  canvas: { borderWidth: 1, borderRadius: 20, overflow: 'hidden', marginBottom: 12 },
  node: { position: 'absolute', width: 18, height: 18, borderRadius: 9, alignItems: 'center', justifyContent: 'center', borderWidth: 2 },
  label: { position: 'absolute', width: 120, alignItems: 'center' },
  row: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 8 },
});
