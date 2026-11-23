import React, { useMemo, useState } from 'react';
import { View, Pressable, StyleSheet, Alert } from 'react-native';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { usePalette } from '@/theme/usePalette';
import { useCurrentGame } from '@/features/game/useCurrentGame';
import { buyItem } from '@/features/game/api';

const SECTION_NAMES: Record<string, string> = {
  consumables: 'Расходные', equipment: 'Снаряжение',
  materials: 'Материалы', structures: 'Для убежища',
};
const CURRENCY_NAMES: Record<string, string> = { coins: 'монеты' };

function coinsText(value: number): string {
  const n = Math.max(0, Math.floor(value));
  const mod100 = n % 100;
  if (mod100 >= 11 && mod100 <= 14) return `${n} монет`;
  const mod10 = n % 10;
  if (mod10 === 1) return `${n} монета`;
  if (mod10 >= 2 && mod10 <= 4) return `${n} монеты`;
  return `${n} монет`;
}

export default function Shop() {
  const p = usePalette();
  const queryClient = useQueryClient();
  const { data, id } = useCurrentGame();
  const [filter, setFilter] = useState('all');
  const [busyId, setBusyId] = useState('');
  const items = data?.shop_items || [];
  const wallet = data?.character?.currency || {};

  const sections = useMemo(() => {
    const names = new Set(items.map((item: any) => String(item.type || item.slot || '')));
    return [...names].filter(Boolean);
  }, [items]);
  const shown = filter === 'all' ? items : items.filter((item: any) =>
    String(item.type || item.slot || '') === filter);

  const buy = useMutation({
    mutationFn: (itemId: string) => buyItem(id!, itemId),
    onSuccess: async (result) => {
      if (result.error) {
        Alert.alert('Не вышло', result.message || 'Попробуй ещё раз.');
      } else {
        Alert.alert('Покупка', 'Вещь у тебя в сумке.');
        await queryClient.invalidateQueries({ queryKey: ['campaign', id] });
      }
      setBusyId('');
    },
    onError: (error: any) => {
      Alert.alert('Не вышло', error?.message || 'Попробуй ещё раз.');
      setBusyId('');
    },
  });

  const walletText = Object.entries(wallet).length
    ? Object.entries(wallet).map(([kind, amount]) =>
      kind === 'coins' ? coinsText(Number(amount)) : `${CURRENCY_NAMES[kind] || 'ресурс'}: ${amount}`).join(' · ')
    : 'Монет пока нет';

  return (
    <Screen>
      <TopBar title="Магазин" eyebrow="У торговца" />
      <Panel><AppText>{walletText}</AppText></Panel>

      <View style={s.filters}>
        <Pressable onPress={() => setFilter('all')}>
          <Panel style={{ ...s.chip, ...(filter === 'all' ? { borderColor: p.accent } : {}) }}>
            <AppText variant="muted" style={{ color: filter === 'all' ? p.accent : p.muted }}>Всё</AppText>
          </Panel>
        </Pressable>
        {sections.map((key) => (
          <Pressable key={key} onPress={() => setFilter(key)}>
            <Panel style={{ ...s.chip, ...(filter === key ? { borderColor: p.accent } : {}) }}>
              <AppText variant="muted" style={{ color: filter === key ? p.accent : p.muted }}>
                {SECTION_NAMES[key] || key}
              </AppText>
            </Panel>
          </Pressable>
        ))}
      </View>

      {shown.map((item: any) => {
        const price = item.currency === 'coins'
          ? coinsText(Number(item.price))
          : `${item.price} ${CURRENCY_NAMES[item.currency] || 'ресурс'}`;
        const affordable = Number(wallet[item.currency] || 0) >= Number(item.price || 0);
        return (
          <Panel key={item.id} style={s.card}>
            <AppText variant="heading">{item.name}</AppText>
            <AppText variant="muted">{item.description || 'Обычный товар.'}</AppText>
            <View style={s.bottom}>
              <AppText style={{ color: affordable ? p.gold : p.muted }}>
                {price} · осталось {item.stock}
              </AppText>
              <Pressable
                disabled={!item.stock || !affordable || Boolean(busyId) || !id}
                onPress={() => {
                  setBusyId(item.id);
                  Alert.alert('Купить?', `«${item.name}» за ${price}.`, [
                    { text: 'Не надо', onPress: () => setBusyId('') },
                    { text: 'Купить', onPress: () => buy.mutate(item.id) },
                  ]);
                }}
                style={[s.buy, { backgroundColor: item.stock && affordable ? p.accent : p.surface2 }]}
              >
                <AppText style={{ color: item.stock && affordable ? '#07131B' : p.muted, fontWeight: '800' }}>
                  {busyId === item.id ? '…' : 'Купить'}
                </AppText>
              </Pressable>
            </View>
          </Panel>
        );
      })}
      {!shown.length && <AppText variant="muted">Здесь пока ничего нет.</AppText>}
    </Screen>
  );
}

const s = StyleSheet.create({
  filters: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  chip: { paddingHorizontal: 12, paddingVertical: 8 },
  card: { gap: 5 },
  bottom: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: 6 },
  buy: { borderRadius: 12, paddingHorizontal: 16, paddingVertical: 10 },
});
