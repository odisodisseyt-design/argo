import React, { useState } from 'react';
import { View, Pressable, StyleSheet, Alert } from 'react-native';
import { router } from 'expo-router';
import * as Updates from 'expo-updates';
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
  { id: 'system', name: 'SYSTEM', desc: 'Системный интерфейс LitRPG' },
  { id: 'fantasy', name: 'FANTASY', desc: 'Пергамент, металл и руны' },
  { id: 'scifi', name: 'SCI-FI', desc: 'Футуристичные панели' },
  { id: 'dark', name: 'DARK', desc: 'Мрачная атмосфера' },
];

export default function Settings() {
  const p = usePalette();
  const theme = usePreferences((s) => s.theme);
  const setTheme = usePreferences((s) => s.setTheme);
  const scale = usePreferences((s) => s.fontScale);
  const setScale = usePreferences((s) => s.setFontScale);
  const haptics = usePreferences((s) => s.haptics);
  const toggle = usePreferences((s) => s.toggle);
  const [checking, setChecking] = useState(false);

  const checkUpdate = async () => {
    setChecking(true);
    try {
      const result = await Updates.checkForUpdateAsync();
      if (!result.isAvailable) {
        Alert.alert('Обновления', 'У вас актуальная версия.');
        return;
      }
      const fetched = await Updates.fetchUpdateAsync();
      if (!fetched.isNew) {
        Alert.alert('Обновления', 'Новая версия уже загружена.');
        return;
      }
      Alert.alert('Обновление загружено', 'Перезапустить приложение?', [
        { text: 'Позже' },
        { text: 'Перезапустить', onPress: () => void Updates.reloadAsync() },
      ]);
    } catch (error: any) {
      Alert.alert('Не удалось проверить', 'Проверь соединение с сервером и попробуй ещё раз.');
    } finally {
      setChecking(false);
    }
  };

  return (
    <Screen>
      <TopBar title="Настройки" />
      <AppText variant="heading">Визуальный стиль</AppText>
      {themes.map((t) => (
        <Pressable key={t.id} onPress={() => setTheme(t.id)}>
          <Panel style={{ ...s.theme, ...(theme === t.id ? { borderColor: p.accent } : {}) }}>
            <View style={{ flex: 1, gap: 4 }}>
              <AppText variant="heading">{t.name}</AppText>
              <AppText variant="muted">{t.desc}</AppText>
            </View>
            <AppText style={{ color: theme === t.id ? p.accent : p.muted, fontWeight: '800' }}>
              {theme === t.id ? 'ВЫБРАНО' : 'ВЫБРАТЬ'}
            </AppText>
          </Panel>
        </Pressable>
      ))}
      <Panel>
        <AppText variant="heading">Размер текста</AppText>
        <View style={s.row}>
          <Pressable onPress={() => setScale(Math.max(0.85, scale - 0.05))}>
            <AppText style={{ color: p.accent, fontSize: 22 }}>−</AppText>
          </Pressable>
          <AppText>{Math.round(scale * 100)}%</AppText>
          <Pressable onPress={() => setScale(Math.min(1.25, scale + 0.05))}>
            <AppText style={{ color: p.accent, fontSize: 22 }}>＋</AppText>
          </Pressable>
        </View>
      </Panel>
      <Panel>
        <Pressable onPress={() => toggle('haptics')} style={s.row}>
          <AppText>Тактильная отдача</AppText>
          <AppText style={{ color: haptics ? p.success : p.muted }}>{haptics ? 'ВКЛ' : 'ВЫКЛ'}</AppText>
        </Pressable>
        <AppText variant="muted">Звук и иллюстрации появятся вместе с медиа-паком.</AppText>
      </Panel>
      <Panel>
        <AppText variant="heading">Обновление</AppText>
        <AppText variant="muted">Код и ассеты обновляются без скачивания полного APK.</AppText>
        <ActionButton title={checking ? 'Проверяем…' : 'Проверить обновление'} onPress={checkUpdate} />
      </Panel>
      <ActionButton title="Выйти из аккаунта" secondary onPress={async () => { await logout(); router.replace('/(auth)/login'); }} />
      <AppText variant="muted">ARGO · Open Source · версия 0.1.1 · OTA активен</AppText>
    </Screen>
  );
}

const s = StyleSheet.create({
  theme: { flexDirection: 'row', alignItems: 'center' },
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 8 },
});
