import 'react-native-gesture-handler';
import React, { useEffect, useState } from 'react';
import { Stack, useRouter, useSegments } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { usePalette } from '@/theme/usePalette';
import { credentials } from '@/services/storage/credentials';
import { restoreActiveCampaign } from '@/features/game/store';

const queryClient = new QueryClient({ defaultOptions: { queries: { staleTime: 15_000, retry: 1 } } });

function RootStack() {
  const p = usePalette();
  const router = useRouter();
  const segments = useSegments();
  const [checked, setChecked] = useState(false);

  useEffect(() => {
    (async () => {
      const token = await credentials.get();
      if (token) await restoreActiveCampaign();
      const inAuth = segments[0] === '(auth)';
      if (!token && !inAuth) router.replace('/(auth)/login');
      if (token && inAuth) router.replace('/(main)');
      setChecked(true);
    })();
  }, [router, segments]);

  if (!checked) return null;
  return (
    <>
      <StatusBar style="light" />
      <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: p.background }, animation: 'fade' }} />
    </>
  );
}

export default function RootLayout() {
  return (
    <SafeAreaProvider>
      <QueryClientProvider client={queryClient}>
        <RootStack />
      </QueryClientProvider>
    </SafeAreaProvider>
  );
}
