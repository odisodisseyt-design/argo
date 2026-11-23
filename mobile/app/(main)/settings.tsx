import React, { useState } from 'react';
import { View, Pressable, StyleSheet, Alert } from 'react-native';
import { router } from 'expo-router';
import * as Updates from 'expo-updates';
import Constants from 'expo-constants';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { ActionButton } from '@/components/ui/ActionButton';
import { usePalette } from '@/theme/usePalette';
import { usePreferences } from '@/store/preferences';
import { logout } from '@/features/game/api';
import type { ThemeName } from '@/types/api';

const themes: { id: ThemeName; name: string; desc: string }[] = [
  { id: 'system', name: 'Как в системе', desc: 'Светлая или тёмная — как настроено в телефоне' },
  { id: 'fantasy', name: 'Старый мир', desc: 'Пергамент, металл и руны' },
  { id: 'scifi', name: 'Будущее', desc: 'Холодные панели и неон' },
  { id: 'dark', name: 'Тёмная', desc: 'Минимальный свет, максимум текста' },
];

export default function Settings() {
  const p = usePalette();
  const prefs = usePreferences();
  const [checking, setChecking] = useState(false);
  const [section, setSection] = useState<'main' | 'game'>('main');
  const version = Constants.expoConfig?.version || '0.1.3';

  const checkUpdate = async () => {
    setChecking(true);
    try {
      const result = await Updates.checkForUpdateAsync();
      if (!result.isAvailable) {
        Alert.alert('Обновления', 'У тебя актуальная версия.');
        return;
      }
      const fetched = await Updates.fetchUpdateAsync();
      if (!fetched.isNew) {
        Alert.alert('Обновление', 'Новая версия уже загружена.');
        return;
      }
      Alert.alert('Обновление загружено', 'Перезапустить приложение?', [
        { text: 'Позже' },
        { text: 'Перезапустить', onPress: () => void Updates.reloadAsync() },
      ]);
    } catch {
      Alert.alert('Не получилось', 'Проверь соединение и попробуй ещё раз.');
    } finally {
      setChecking(false);
    }
  };

  const Switch = ({ label, hint, value, onPress }: {
    label: string; hint?: string; value: boolean; onPress: () => void;
  }) => (
    <Pressable onPress={onPress} style={s.row}>
      <View style={{ flex: 1, gap: 3 }}>
        <AppText>{label}</AppText>
        {hint ? <AppText variant="muted">{hint}</AppText> : null}
      </View>
      <AppText style={{ color: value ? p.success : p.muted, fontWeight: '800' }}>
        {value ? 'ВКЛ' : 'ВЫКЛ'}
      </AppText>
    </Pressable>
  );

  return (
    <Screen>
      <TopBar title="Настройки" eyebrow="ARGO" />
      <View style={s.tabs}>
        <Pressable onPress={() => setSection('main')} style={[s.tab, section === 'main' ? { borderColor: p.accent } : null]}>
          <AppText variant="muted" style={{ color: section === 'main' ? p.accent : p.muted }}>Основные</AppText>
        </Pressable>
        <Pressable onPress={() => setSection('game')} style={[s.tab, section === 'game' ? { borderColor: p.accent } : null]}>
          <AppText variant="muted" style={{ color: section === 'game' ? p.accent : p.muted }}>Игра и эффекты</AppText>
        </Pressable>
      </View>

      {section === 'main' ? (
        <>
          <AppText variant="heading">Внешний вид</AppText>
          {themes.map((t) => (
            <Pressable key={t.id} onPress={() => prefs.setTheme(t.id)}>
              <Panel style={{ ...s.theme, ...(prefs.theme === t.id ? { borderColor: p.accent } : {}) }}>
                <View style={{ flex: 1, gap: 4 }}>
                  <AppText variant="heading">{t.name}</AppText>
                  <AppText variant="muted">{t.desc}</AppText>
                </View>
                <AppText style={{ color: prefs.theme === t.id ? p.accent : p.muted, fontWeight: '800' }}>
                  {prefs.theme === t.id ? 'ВЫБРАНО' : 'ВЫБРАТЬ'}
                </AppText>
              </Panel>
            </Pressable>
          ))}
          <Panel>
            <AppText variant="heading">Размер текста</AppText>
            <View style={s.row}>
              <Pressable onPress={() => prefs.setFontScale(Math.max(0.85, prefs.fontScale - 0.05))}>
                <AppText style={{ color: p.accent, fontSize: 22 }}>−</AppText>
              </Pressable>
              <AppText>{Math.round(prefs.fontScale * 100)}%</AppText>
              <Pressable onPress={() => prefs.setFontScale(Math.min(1.25, prefs.fontScale + 0.05))}>
                <AppText style={{ color: p.accent, fontSize: 22 }}>＋</AppText>
              </Pressable>
            </View>
          </Panel>
        </>
      ) : (
        <>
          <AppText variant="heading">Эффекты</AppText>
          <Panel>
            <Switch label="Тактильная отдача" hint="Лёгкий отклик при нажатиях"
              value={prefs.haptics} onPress={() => prefs.toggle('haptics')} />
            <Switch label="Звук" hint="Звуки шагов и событий"
              value={prefs.sound} onPress={() => prefs.toggle('sound')} />
            <Switch label="Иллюстрации" hint="Картинки к сценам"
              value={prefs.illustrations} onPress={() => prefs.toggle('illustrations')} />
          </Panel>
        </>
      )}

      <Panel>
        <AppText variant="heading">Обновление</AppText>
        <AppText variant="muted">Докачиваются только изменившиеся части — полный APK не нужен.</AppText>
        <ActionButton title={checking ? 'Проверяем…' : 'Проверить обновление'} onPress={checkUpdate} />
      </Panel>
      <ActionButton title="Выйти из аккаунта" secondary
        onPress={async () => { await logout(); router.replace('/(auth)/login'); }} />
      <AppText variant="muted">ARGO · открытый исходный код · версия {version}</AppText>
    </Screen>
  );
}

const s = StyleSheet.create({
  tabs: { flexDirection: 'row', gap: 8 },
  tab: { flex: 1, borderWidth: 1, borderRadius: 12, paddingVertical: 10, alignItems: 'center' },
  theme: { flexDirection: 'row', alignItems: 'center' },
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 10 },
});
