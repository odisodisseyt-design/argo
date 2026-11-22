import React, { useState } from 'react';
import { View, Pressable, StyleSheet, Alert } from 'react-native';
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

export default function HomeScreen() {
  const p = usePalette();
  const queryClient = useQueryClient();
  const setActive = useGame((s) => s.setActiveCampaign);
  const setFeatures = useGame((s) => s.setFeatures);
  const [name, setName] = useState('Моё пространственное убежище');

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
    mutationFn: () => newCampaign(name, availableModels.data?.models[0]?.id || 'fast'),
    onSuccess: async (result) => {
      await setActive(result.campaign_id);
      const fresh = await campaign(result.campaign_id);
      setFeatures(fresh.ui_manifest?.features || {});
      await queryClient.invalidateQueries({ queryKey: ['campaigns'] });
      router.push(`/campaigns/${result.campaign_id}`);
    },
    onError: (error: any) => Alert.alert('Не получилось', error?.message || 'Ошибка создания игры.'),
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
      <TopBar title="Твой мир начинается здесь" />
      <View style={styles.welcome}>
        <AppText variant="muted">ВОЗВРАЩЕНИЕ В ИГРУ</AppText>
        <AppText variant="heading">С возвращением, странник.</AppText>
        <AppText variant="muted">Твои истории продолжаются на сервере.</AppText>
      </View>

      {list.length > 0 ? (
        <Pressable onPress={() => open(list[0].id)}>
          <Panel style={{ padding: 0, overflow: 'hidden', borderColor: p.accent + '77' }}>
            <View style={[styles.heroArt, { backgroundColor: p.surface2 }]}>
              <Sparkles size={18} color={p.accent} />
              <AppText variant="title">{list[0].name}</AppText>
              <AppText variant="muted">{list[0].anchor?.location?.name || 'Неизвестное место'}</AppText>
            </View>
            <View style={styles.heroBottom}>
              <View style={{ gap: 6, flex: 1 }}>
                <PhaseBadge phase={list[0].phase as CampaignPhase} />
                <AppText variant="muted">Шаг {list[0].step_no} · ожидают {list[0].waiting_for.length}</AppText>
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
          <AppText variant="heading">Пока нет игр</AppText>
          <AppText variant="muted">Создай первую историю — сервер подготовит мир.</AppText>
        </Panel>
      )}

      <AppText variant="heading">Мои кампании</AppText>
      {list.map((c) => (
        <Panel key={c.id} style={styles.campaignRow}>
          <Pressable onPress={() => open(c.id)} style={{ flex: 1, gap: 5 }}>
            <AppText variant="heading">{c.name}</AppText>
            <AppText variant="muted">Шаг {c.step_no} · фаза {c.phase}</AppText>
            <PhaseBadge phase={c.phase as CampaignPhase} />
          </Pressable>
          <Pressable
            onPress={() => Alert.alert('Удалить игру?', `«${c.name}» будет убрана из твоего списка.`, [
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

      <Panel>
        <AppText variant="label" style={{ color: p.accent }}>НОВАЯ ИСТОРИЯ</AppText>
        <AppText variant="muted">Модель: {availableModels.data?.models[0]?.title || 'загрузка…'}</AppText>
        <ActionButton title={create.isPending ? 'Создаём…' : 'Создать игру'} onPress={() => create.mutate()} />
      </Panel>
      <ActionButton title="Выйти" secondary onPress={async () => { await logout(); router.replace('/(auth)/login'); }} />
      {games.isError && <AppText variant="muted">Не удалось загрузить игры. Проверь соединение.</AppText>}
    </Screen>
  );
}

const styles = StyleSheet.create({
  welcome: { gap: 5, marginTop: 3 },
  heroArt: { padding: 20, gap: 8, borderRadius: 14 },
  heroBottom: { padding: 16, flexDirection: 'row', alignItems: 'center', gap: 10 },
  continue: { flexDirection: 'row', alignItems: 'center', gap: 8, borderRadius: 13, paddingHorizontal: 14, paddingVertical: 13 },
  campaignRow: { flexDirection: 'row', alignItems: 'center', gap: 13 },
});
