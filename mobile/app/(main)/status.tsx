import React from 'react';
import { View, Pressable, StyleSheet, Alert } from 'react-native';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { usePalette } from '@/theme/usePalette';
import { useCurrentGame } from '@/features/game/useCurrentGame';
import { allocate } from '@/features/game/api';
import { useGame } from '@/features/game/store';

const labels: Record<string, string> = {
  str: 'Сила', agi: 'Ловкость', vit: 'Выносливость', end: 'Стойкость', mag: 'Магия',
  head: 'Голова', torso: 'Торс', arm_l: 'Левая рука', arm_r: 'Правая рука',
  leg_l: 'Левая нога', leg_r: 'Правая нога',
};
const bodyLabels: Record<string, string> = {
  intact: 'цело', bruised: 'ушиб', wounded: 'рана', broken: 'перелом',
  severed: 'отделено', destroyed: 'уничтожено',
};

export default function Status() {
  const p = usePalette();
  const { id, data } = useCurrentGame();
  const queryClient = useQueryClient();
  const character = data?.character;
  const attributes = character?.attributes || {};
  const mutation = useMutation({
    mutationFn: (allocations: Array<{ id: string; delta: number }>) => allocate(id!, allocations),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['campaign', id] });
    },
    onError: (error: any) => Alert.alert('Не получилось', error?.message || 'Попробуй ещё раз.'),
  });
  const unspent = Number(character?.unspent_points || 0);

  return (
    <Screen>
      <TopBar title="Статус персонажа" />
      {!character ? <Panel><AppText variant="muted">Открой кампанию, чтобы увидеть персонажа.</AppText></Panel> : (
        <>
          <Panel>
            <AppText variant="label" style={{ color: p.accent }}>
              УРОВЕНЬ {character.level || 1} · {character.class_name || 'КЛАСС НЕ ВЫБРАН'}
            </AppText>
            <AppText variant="title">{character.name}</AppText>
            <AppText variant="muted">Опыт: {character.xp || 0} / {character.xp_to_next || 5}</AppText>
            <AppText variant="muted">Свободные очки: {unspent}</AppText>
          </Panel>
          <Panel>
            <AppText variant="heading">Характеристики</AppText>
            {Object.entries(attributes).map(([key, value]) => (
              <View key={key} style={[s.stat, { borderBottomColor: p.border }]}>
                <AppText>{labels[key] || 'Характеристика'}</AppText>
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 10 }}>
                  {unspent > 0 && (
                    <Pressable onPress={() => mutation.mutate([{ id: key, delta: 1 }])} hitSlop={8}>
                      <AppText style={{ color: p.accent, fontSize: 20 }}>＋</AppText>
                    </Pressable>
                  )}
                  <AppText style={{ fontWeight: '800' }}>{String(value)}</AppText>
                </View>
              </View>
            ))}
            {unspent > 0 && <AppText variant="muted">Нажми «＋», чтобы вложить очко.</AppText>}
          </Panel>
          <Panel>
            <AppText variant="heading">Состояние тела</AppText>
            {Object.entries(character.body || {}).map(([part, info]: [string, any]) => (
              <View key={part} style={[s.stat, { borderBottomColor: p.border }]}>
                <AppText>{labels[part] || 'Часть тела'}</AppText>
                <AppText variant="muted">{bodyLabels[info?.status] || 'цело'}</AppText>
              </View>
            ))}
          </Panel>
          <Panel>
            <AppText variant="heading">Состояния</AppText>
            {(character.conditions || []).length ? (character.conditions || []).map((c: any, i: number) => (
              <AppText key={i} variant="muted">{c.name || 'Состояние'}</AppText>
            )) : <AppText variant="muted">Нет активных состояний.</AppText>}
            <AppText variant="muted">Класс: {character.class_name || 'не выбран'}</AppText>
            <AppText variant="muted">Навыки: {(character.skills || []).length}</AppText>
          </Panel>
        </>
      )}
    </Screen>
  );
}

const s = StyleSheet.create({
  stat: { paddingVertical: 11, borderBottomWidth: 1, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
});
