import React, { useState } from 'react';
import { View, TextInput, StyleSheet, Alert } from 'react-native';
import { router } from 'expo-router';
import { Screen } from '@/components/ui/Screen';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { ActionButton } from '@/components/ui/ActionButton';
import { usePalette } from '@/theme/usePalette';
import { login, register } from '@/features/game/api';

export default function Login() {
  const p = usePalette();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [busy, setBusy] = useState(false);
  const [mode, setMode] = useState<'login' | 'register'>('login');

  const submit = async () => {
    if (!username.trim() || password.length < 6) {
      Alert.alert('Проверь данные', 'Имя пользователя и пароль от 6 символов.');
      return;
    }
    setBusy(true);
    try {
      await (mode === 'login' ? login : register)(username.trim(), password);
      router.replace('/(main)');
    } catch (error: any) {
      Alert.alert('Не получилось войти', error?.message || 'Проверь соединение с сервером.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Screen style={{ flex: 1, justifyContent: 'center' }}>
      <AppText variant="label" style={{ color: p.accent, letterSpacing: 3, textAlign: 'center' }}>A R G O</AppText>
      <AppText variant="title" style={{ textAlign: 'center' }}>
        {mode === 'login' ? 'Войди в свою историю' : 'Новый странник'}
      </AppText>
      <Panel>
        <TextInput
          placeholder="Имя пользователя"
          autoCapitalize="none"
          placeholderTextColor={p.muted}
          value={username}
          onChangeText={setUsername}
          style={[s.input, { color: p.text, borderColor: p.border }]}
        />
        <TextInput
          placeholder="Пароль"
          secureTextEntry
          placeholderTextColor={p.muted}
          value={password}
          onChangeText={setPassword}
          style={[s.input, { color: p.text, borderColor: p.border }]}
        />
        <ActionButton title={busy ? 'Подожди…' : mode === 'login' ? 'Войти' : 'Создать аккаунт'} onPress={submit} />
        <ActionButton
          title={mode === 'login' ? 'Создать аккаунт' : 'У меня уже есть аккаунт'}
          secondary
          onPress={() => setMode(mode === 'login' ? 'register' : 'login')}
        />
      </Panel>
      <AppText variant="muted" style={{ textAlign: 'center' }}>
        Сервер — единственный источник правды. Клиент только показывает историю.
      </AppText>
    </Screen>
  );
}

const s = StyleSheet.create({ input: { height: 48, borderWidth: 1, borderRadius: 12, paddingHorizontal: 13 } });
