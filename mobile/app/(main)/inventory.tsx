import React, { useMemo, useState } from 'react';
import { View, StyleSheet, Pressable, Modal, Alert } from 'react-native';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { ActionButton } from '@/components/ui/ActionButton';
import { usePalette } from '@/theme/usePalette';
import { useCurrentGame } from '@/features/game/useCurrentGame';
import { equipItem, unequipItem, useItem } from '@/features/game/api';

const SLOT_NAMES: Record<string, string> = {
  weapon: 'Оружие', armor: 'Броня', head: 'Голова', body: 'Тело',
  hands: 'Руки', legs: 'Ноги', feet: 'Обувь', ring: 'Кольцо',
  amulet: 'Амулет', cloak: 'Плащ', tool: 'Инструмент',
};

const KIND_EQUIPPABLE: Record<string, string> = {
  weapon: 'Оружие', melee: 'Оружие', ranged: 'Оружие', blade: 'Оружие', blunt: 'Оружие',
  armor: 'Броня', clothing: 'Броня', gear: 'Броня',
};

function usageLabel(item: any): string {
  return item?.usable ? 'Использовать' : '';
}

export default function Inventory() {
  const p = usePalette();
  const queryClient = useQueryClient();
  const { data, id } = useCurrentGame();
  const [expanded, setExpanded] = useState(false);
  const [selected, setSelected] = useState<any>(null);
  const [busy, setBusy] = useState(false);
  const items = useMemo(
    () => [...(data?.character?.inventory || [])].sort((a: any, b: any) =>
      String(a.name || '').localeCompare(String(b.name || ''), 'ru')),
    [data?.character?.inventory],
  );
  const equipment = data?.character?.equipment || {};
  const visible = expanded ? items : items.slice(0, 8);

  const refresh = () => queryClient.invalidateQueries({ queryKey: ['campaign', id] });

  const act = async (kind: 'use' | 'equip' | 'unequip', arg: string) => {
    if (!id) return;
    setBusy(true);
    try {
      const result = kind === 'use' ? await useItem(id, arg)
        : kind === 'equip' ? await equipItem(id, arg)
          : await unequipItem(id, arg);
      if (result.error) {
        Alert.alert('Не вышло', result.message || 'Попробуй ещё раз.');
      } else {
        if (result.message) Alert.alert('Готово', result.message);
        setSelected(null);
        await refresh();
      }
    } catch (error: any) {
      Alert.alert('Не вышло', error?.message || 'Попробуй ещё раз.');
    } finally {
      setBusy(false);
    }
  };

  const useLabel = selected ? usageLabel(selected) : '';
  const equipLabel = selected?.equippable
    ? SLOT_NAMES[selected.equippable] || KIND_EQUIPPABLE[selected.equippable] || 'Надеть'
    : '';

  return (
    <Screen>
      <TopBar title="Инвентарь" eyebrow={`${items.length} предметов`} />
      <Panel>
        <AppText variant="label" style={{ color: p.accent }}>СНАРЯЖЕНИЕ</AppText>
        {Object.entries(equipment).length
          ? Object.entries(equipment).map(([slot, itemId]) => {
            const item = items.find((x: any) => x.id === itemId);
            return (
              <Pressable key={slot} onPress={() => act('unequip', slot)} style={s.row}>
                <AppText variant="muted">{SLOT_NAMES[slot] || 'Предмет'}</AppText>
                <AppText style={{ color: p.accent }}>{item?.name || 'Предмет'} · убрать</AppText>
              </Pressable>
            );
          })
          : <AppText variant="muted">Пока ничего не надето.</AppText>}
      </Panel>

      <AppText variant="heading">Предметы · {items.length}</AppText>
      <View style={s.grid}>
        {visible.map((item: any) => (
          <Pressable key={item.id} onPress={() => setSelected(item)} style={{ width: '47%' }}>
            <Panel style={s.item}>
              <AppText variant="heading">{item.name}</AppText>
              <AppText variant="muted">{item.quantity || 1} шт · {item.weight ?? item.weight_kg ?? 0} кг</AppText>
              {item.description ? (
                <AppText variant="muted" numberOfLines={2}>{item.description}</AppText>
              ) : null}
            </Panel>
          </Pressable>
        ))}
        {!items.length && <AppText variant="muted">В сумке пока пусто. Что-то можно найти или купить.</AppText>}
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

      <Modal visible={Boolean(selected)} transparent animationType="fade">
        <View style={s.backdrop}>
          <Panel style={s.modal}>
            <AppText variant="title">{selected?.name}</AppText>
            <AppText variant="muted">{selected?.description || 'Обычная вещь. Особые свойства неизвестны.'}</AppText>
            <AppText variant="muted">
              Количество: {selected?.quantity || 1} · Вес: {selected?.weight ?? selected?.weight_kg ?? 0} кг
            </AppText>
            {selected?.equipped ? <AppText variant="muted">Сейчас надето.</AppText> : null}
            {useLabel ? (
              <ActionButton title={busy ? '…' : useLabel} onPress={() => act('use', selected.id)} />
            ) : null}
            {equipLabel ? (
              <ActionButton title={busy ? '…' : `Надеть: ${equipLabel}`} secondary
                onPress={() => act('equip', selected.id)} />
            ) : null}
            <ActionButton title="Закрыть" secondary onPress={() => setSelected(null)} />
          </Panel>
        </View>
      </Modal>
    </Screen>
  );
}

const s = StyleSheet.create({
  row: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 9 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  item: { minHeight: 96, gap: 4 },
  backdrop: { flex: 1, justifyContent: 'center', backgroundColor: '#000b', padding: 18 },
  modal: { gap: 10 },
});
