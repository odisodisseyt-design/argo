import React, { useState } from 'react';
import { View, Pressable, StyleSheet, Alert, TextInput } from 'react-native';
import { router } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowRight, Sparkles } from 'lucide-react-native';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { ActionButton } from '@/components/ui/ActionButton';
import { PhaseBadge } from '@/components/ui/PhaseBadge';
import { usePalette } from '@/theme/usePalette';
import { campaign, deleteCampaign, myCampaigns, models, newCampaign } from '@/features/game/api';
import { useGame } from '@/features/game/store';
import { logout } from '@/features/game/api';
import type { CampaignPhase } from '@/types/api';

const PHASE_NAMES: Record<string, string> = {
  orders: 'приём приказов', waiting: 'ожидание', resolving: 'разрешение', results: 'итоги',
};

export default function HomeScreen() {
  const p = usePalette();
  const queryClient = useQueryClient();
  const setActive = useGame((s) => s.setActiveCampaign);
  const setFeatures = useGame((s) => s.setFeatures);
  const [hero, setHero] = useState('');

  const games = useQuery({ queryKey: ['campaigns'], queryFn: myCampaigns });
  const availableModels = useQuery({ queryKey: ['models'], queryFn: models });
  const remove = useMutation({
    mutationFn: (id: string) => deleteCampaign(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['campaigns'] });
    },
    onError: (error: any) => Alert.alert('Не удалось удалить', error?.message || 'Попробуй ещё раз.'),
  });
  const create = useMutation({
    mutationFn: () => newCampaign('Первый час', availableModels.data?.models[0]?.id || 'cheap', hero.trim()),
    onSuccess: async (result) => {
      await setActive(result.campaign_id);
      const fresh = await campaign(result.campaign_id);
      setFeatures(fresh.ui_manifest?.features || {});
      await queryClient.invalidateQueries({ queryKey: ['campaigns'] });
      router.push(`/campaigns/${result.campaign_id}`);
    },
    onError: (error: any) => Alert.alert('Не получилось', error?.message || 'Не удалось начать историю.'),
  });

  const list = games.data?.campaigns || [];
  const open = async (id: string) => {
    await setActive(id);
    const fresh = await campaign(id);
    setFeatures(fresh.ui_manifest?.features || {});
    router.push(`/campaigns/${id}`);
  };

  return (
    <Screen>
      <TopBar title="Арго" eyebrow="ГОРОД ПОСЛЕ ПРИХОДА СИСТЕМЫ" />
      <View style={styles.welcome}>
        <AppText variant="muted">ТВОЯ ИСТОРИЯ</AppText>
        <AppText variant="heading">С возвращением, странник.</AppText>
        <AppText variant="muted">Продолжай с того места, где остановился.</AppText>
      </View>

      {list.length > 0 ? (
        <Pressable onPress={() => open(list[0].id)}>
          <Panel style={{ padding: 0, overflow: 'hidden', borderColor: p.accent + '77' }}>
            <View style={[styles.heroArt, { backgroundColor: p.surface2 }]}>
              <Sparkles size={18} color={p.accent} />
              <AppText variant="title">{list[0].name}</AppText>
              <AppText variant="muted">{list[0].description || 'Город после прихода Системы.'}</AppText>
              <AppText variant="muted">{list[0].anchor?.location?.name || 'Неизвестное место'}</AppText>
            </View>
            <View style={styles.heroBottom}>
              <View style={{ gap: 6, flex: 1 }}>
                <PhaseBadge phase={list[0].phase as CampaignPhase} />
                <AppText variant="muted">Шаг {list[0].step_no} · {PHASE_NAMES[list[0].phase] || 'в процессе'}</AppText>
              </View>
              <View style={[styles.continue, { backgroundColor: p.accent }]}>
                <AppText style={{ color: '#07131B', fontWeight: '900' }}>Продолжить</AppText>
                <ArrowRight color="#07131B" size={17} />
              </View>
            </View>
          </Panel>
        </Pressable>
      ) : (
        <Panel>
          <AppText variant="heading">Пока нет историй</AppText>
          <AppText variant="muted">Начни первую — мир встретит тебя в первый час после прихода Системы.</AppText>
        </Panel>
      )}

      <AppText variant="heading">Мои истории</AppText>
      {list.map((c) => (
        <Panel key={c.id} style={styles.campaignRow}>
          <Pressable onPress={() => open(c.id)} style={{ flex: 1, gap: 5 }}>
            <AppText variant="heading">{c.name}</AppText>
            <AppText variant="muted">Шаг {c.step_no} · {PHASE_NAMES[c.phase] || 'в процессе'}</AppText>
            <PhaseBadge phase={c.phase as CampaignPhase} />
          </Pressable>
          <Pressable
            onPress={() => Alert.alert('Удалить историю?', `«${c.name}» пропадёт из списка.`, [
              { text: 'Отмена' },
              { text: 'Удалить', style: 'destructive', onPress: () => remove.mutate(c.id) },
            ])}
            hitSlop={10}
          >
            <AppText style={{ color: p.muted, fontSize: 20 }}>✕</AppText>
          </Pressable>
          <ArrowRight color={p.muted} />
        </Panel>
      ))}

      <Panel style={{ gap: 9 }}>
        <AppText variant="label" style={{ color: p.accent }}>НОВАЯ ИСТОРИЯ</AppText>
        <AppText variant="muted">Город после прихода Системы: заражённые, классы, убежища и люди, которые ещё держатся.</AppText>
        <TextInput
          value={hero}
          onChangeText={setHero}
          maxLength={24}
          placeholder="Как тебя зовут? (необязательно)"
          placeholderTextColor={p.muted}
          style={[styles.input, { color: p.text, borderColor: p.border, backgroundColor: p.input }]}
        />
        <AppText variant="muted">Рассказ: {availableModels.data?.models[0]?.title || 'загрузка…'}</AppText>
        <ActionButton title={create.isPending ? 'Начинаем…' : 'Начать историю'} onPress={() => create.mutate()} />
      </Panel>
      <ActionButton title="Выйти" secondary onPress={async () => { await logout(); router.replace('/(auth)/login'); }} />
      {games.isError && <AppText variant="muted">Не удалось загрузить истории. Проверь соединение.</AppText>}
    </Screen>
  );
}

const styles = StyleSheet.create({
  welcome: { gap: 5, marginTop: 3 },
  heroArt: { padding: 20, gap: 8, borderRadius: 14 },
  heroBottom: { padding: 16, flexDirection: 'row', alignItems: 'center', gap: 10 },
  continue: { flexDirection: 'row', alignItems: 'center', gap: 8, borderRadius: 13, paddingHorizontal: 14, paddingVertical: 13 },
  campaignRow: { flexDirection: 'row', alignItems: 'center', gap: 13 },
  input: { borderWidth: 1, borderRadius: 12, paddingHorizontal: 12, paddingVertical: 11 },
});
