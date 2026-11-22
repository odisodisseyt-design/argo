import React, { useEffect, useRef, useState } from 'react';
import { View, TextInput, StyleSheet, Alert } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Send, Clock3, Backpack, MapPin, BookOpen, ScrollText, ShoppingBag, MessageCircle, UserRound } from 'lucide-react-native';
import * as Haptics from 'expo-haptics';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { ActionButton } from '@/components/ui/ActionButton';
import { PhaseBadge } from '@/components/ui/PhaseBadge';
import { usePalette } from '@/theme/usePalette';
import { campaign, events, stepStatus, steps, submitTurn } from '@/features/game/api';
import { useGame } from '@/features/game/store';
import { createCampaignSocket } from '@/services/ws/campaignSocket';
import type { CampaignPhase } from '@/types/api';

export default function CampaignScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const queryClient = useQueryClient();
  const setActive = useGame((s) => s.setActiveCampaign);
  const p = usePalette();
  const [command, setCommand] = useState('');
  const [resolving, setResolving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const keyRef = useRef<string>('');
  const lastSeqRef = useRef<number>(0);

  useEffect(() => {
    if (!id) return;
    void setActive(id);
    void campaign(id).then((data) => useGame.getState().setFeatures(data.ui_manifest?.features || {})).catch(() => undefined);
  }, [id, setActive]);

  const game = useQuery({ queryKey: ['campaign', id], queryFn: () => campaign(id), enabled: Boolean(id) });
  const status = useQuery({ queryKey: ['step', id], queryFn: () => stepStatus(id), enabled: Boolean(id) });
  const history = useQuery({ queryKey: ['steps', id], queryFn: () => steps(id), enabled: Boolean(id) });

  useEffect(() => {
    if (!id) return;
    const refresh = () => {
      void queryClient.invalidateQueries({ queryKey: ['campaign', id] });
      void queryClient.invalidateQueries({ queryKey: ['step', id] });
      void queryClient.invalidateQueries({ queryKey: ['steps', id] });
    };
    const applyEvent = (event: string, payload: any, seq?: number) => {
      if (seq && seq <= lastSeqRef.current) return;
      if (seq) lastSeqRef.current = seq;
      if (event === 'step_resolved') setResolving(true);
      if (event === 'turn_final') setResolving(false);
      if (event === 'order_timeout' && payload?.prose) setError(payload.prose);
      if (event === 'rollback') refresh();
      refresh();
    };

    void events(id, 0).then((result) => {
      for (const item of result.events) applyEvent(item.event, item.payload, item.seq);
    }).catch(() => undefined);

    const socket = createCampaignSocket(id, (message) => {
      applyEvent(message.event, message.payload, message.payload?.event_id);
    });
    return () => socket.close();
  }, [id, queryClient]);

  const turn = useMutation({
    mutationFn: () => {
      if (!keyRef.current) keyRef.current = Math.random().toString(36).slice(2) + Date.now().toString(36);
      return submitTurn(id, command.trim(), keyRef.current);
    },
    onSuccess: (result: any) => {
      if (result.error) {
        setError(result.message || 'Приказ не принят.');
        return;
      }
      if (result.clarifying_question) {
        setError(result.clarifying_question);
        return;
      }
      keyRef.current = '';
      setCommand('');
      setError(null);
      void Haptics.selectionAsync();
      void queryClient.invalidateQueries({ queryKey: ['step', id] });
      void queryClient.invalidateQueries({ queryKey: ['campaign', id] });
    },
    onError: (error: any) => Alert.alert('Приказ не принят', error?.message || 'Повтори попытку.'),
  });

  const phase: CampaignPhase = resolving
    ? 'resolving'
    : status.data?.my_turn === false ? 'waiting' : 'orders';
  const latest = history.data?.steps?.[0];
  const roster = status.data?.participants || [];
  const nav = [
    ['Инвентарь', Backpack, '/inventory'], ['Статус', UserRound, '/status'],
    ['Рядом', MapPin, '/nearby'], ['Карта', MapPin, '/map'],
    ['Дневник', BookOpen, '/diary'], ['Квесты', ScrollText, '/quests'],
    ['Магазин', ShoppingBag, '/shop'], ['Чат', MessageCircle, '/chat'],
  ] as const;

  return (
    <Screen>
      <TopBar title={game.data?.name || 'История'} eyebrow={`ШАГ ${status.data?.step || game.data?.step_no || 1}`} />
      <View style={styles.phaseLine}>
        <PhaseBadge phase={phase} />
        <View style={styles.timer}>
          <Clock3 size={14} color={p.muted} />
          <AppText variant="muted">{status.data?.deadline ? `До ${new Date(status.data.deadline.replace(' ', 'T') + 'Z').toLocaleTimeString()}` : 'без дедлайна'}</AppText>
        </View>
      </View>

      {error ? <Panel><AppText style={{ color: p.gold }}>{error}</AppText></Panel> : null}
      <Panel style={{ gap: 8 }}>
        <AppText variant="label" style={{ color: p.accent, letterSpacing: 1.5 }}>МАСТЕР · ИСТОРИЯ</AppText>
        <AppText style={{ lineHeight: 24 }}>{latest?.prose || game.data?.lore?.slice(0, 700) || 'Мир готовится ответить.'}</AppText>
        <AppText variant="muted">{latest ? `Шаг ${latest.step_no}` : 'Ожидаем первую сцену'}</AppText>
      </Panel>

      {phase === 'orders' && (
        <Panel>
          <AppText variant="heading">Твой приказ</AppText>
          <TextInput
            value={command}
            onChangeText={setCommand}
            placeholder="Что будешь делать?"
            placeholderTextColor={p.muted}
            multiline
            style={[styles.input, { color: p.text, borderColor: p.border, backgroundColor: p.input }]}
          />
          <View style={styles.quick}>
            <ActionButton title="Осмотреться" secondary onPress={() => setCommand('Осмотреться вокруг')} />
            <ActionButton title="Подождать" secondary onPress={() => setCommand('Подождать')} />
          </View>
          <ActionButton title={turn.isPending ? 'Отправляем…' : 'Отправить приказ'} onPress={() => turn.mutate()} />
        </Panel>
      )}

      {phase === 'waiting' && (
        <Panel>
          <AppText variant="heading">Ожидаем остальных</AppText>
          {roster.map((r) => (
            <View key={r.name} style={styles.roster}>
              <View style={[styles.statusDot, { backgroundColor: r.submitted ? p.success : p.gold }]} />
              <AppText>{r.name}{r.isYou ? ' (ты)' : ''}</AppText>
              <AppText variant="muted" style={{ marginLeft: 'auto' }}>{r.submitted ? 'сдал' : 'думает…'}</AppText>
            </View>
          ))}
        </Panel>
      )}

      {phase === 'resolving' && (
        <Panel>
          <AppText variant="heading">Мир описывает последствия</AppText>
          <AppText variant="muted">Приказы уже применены, дожидаемся сцены.</AppText>
        </Panel>
      )}

      <AppText variant="heading">Быстрый доступ</AppText>
      <View style={styles.grid}>
        {nav.map(([label, Icon, path]) => (
          <View key={label} style={[styles.tile, { backgroundColor: p.surface, borderColor: p.border }]}>
            <Icon size={18} color={p.accent} />
            <AppText variant="muted">{label}</AppText>
          </View>
        ))}
      </View>
      <ActionButton title="В главное меню" secondary onPress={() => router.replace('/(main)')} />
    </Screen>
  );
}

const styles = StyleSheet.create({
  phaseLine: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  timer: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  input: { minHeight: 90, borderWidth: 1, borderRadius: 13, padding: 13, textAlignVertical: 'top' },
  quick: { flexDirection: 'row', gap: 8, flexWrap: 'wrap' },
  roster: { flexDirection: 'row', alignItems: 'center', gap: 9, paddingVertical: 7 },
  statusDot: { width: 8, height: 8, borderRadius: 5 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 9 },
  tile: { width: '23%', minHeight: 76, borderRadius: 14, borderWidth: 1, alignItems: 'center', justifyContent: 'center', gap: 8, padding: 5 },
});
