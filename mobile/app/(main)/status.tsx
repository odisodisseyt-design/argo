import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { usePalette } from '@/theme/usePalette';
import { useCurrentGame } from '@/features/game/useCurrentGame';

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
  const { data } = useCurrentGame();
  const character = data?.character;
  const attributes = character?.attributes || {};
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
            <AppText variant="muted">Свободные очки: {character.unspent_points || 0}</AppText>
          </Panel>
          <Panel>
            <AppText variant="heading">Характеристики</AppText>
            {Object.entries(attributes).map(([key, value]) => (
              <View key={key} style={[s.stat, { borderBottomColor: p.border }]}>
                <AppText>{labels[key] || 'Характеристика'}</AppText>
                <AppText style={{ fontWeight: '800' }}>{String(value)}</AppText>
              </View>
            ))}
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
  stat: { paddingVertical: 11, borderBottomWidth: 1, flexDirection: 'row', justifyContent: 'space-between' },
});
