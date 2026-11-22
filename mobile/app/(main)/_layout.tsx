import React from 'react';
import { Tabs } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Home, Backpack, UserRound, Map, Settings, ScrollText, ShoppingBag, BookOpen } from 'lucide-react-native';
import { usePalette } from '@/theme/usePalette';
import { useGame } from '@/features/game/store';

export default function MainLayout() {
  const p = usePalette();
  const insets = useSafeAreaInsets();
  const features = useGame((s) => s.features);
  const enabled = (key: string) => features[key] !== false;

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarStyle: {
          backgroundColor: p.surface,
          borderTopColor: p.border,
          height: 54 + insets.bottom,
          paddingTop: 6,
          paddingBottom: insets.bottom,
        },
        tabBarActiveTintColor: p.accent,
        tabBarInactiveTintColor: p.muted,
        tabBarLabelStyle: { fontSize: 10, fontWeight: '700', marginBottom: -2 },
      }}
    >
      <Tabs.Screen name="index" options={{ title: 'Главная', tabBarIcon: ({ color, size }) => <Home color={color} size={size} /> }} />
      <Tabs.Screen name="inventory" options={{ title: 'Инвентарь', href: enabled('inventory') ? undefined : null, tabBarIcon: ({ color, size }) => <Backpack color={color} size={size} /> }} />
      <Tabs.Screen name="status" options={{ title: 'Статус', href: enabled('stats') ? undefined : null, tabBarIcon: ({ color, size }) => <UserRound color={color} size={size} /> }} />
      <Tabs.Screen name="map" options={{ title: 'Карта', href: enabled('map') ? undefined : null, tabBarIcon: ({ color, size }) => <Map color={color} size={size} /> }} />
      <Tabs.Screen name="settings" options={{ title: 'Настройки', tabBarIcon: ({ color, size }) => <Settings color={color} size={size} /> }} />
      <Tabs.Screen name="campaigns/[id]" options={{ href: null }} />
      <Tabs.Screen name="nearby" options={{ href: null }} />
      <Tabs.Screen name="quests" options={{ title: 'Квесты', href: enabled('quests') ? undefined : null, tabBarIcon: ({ color, size }) => <ScrollText color={color} size={size} /> }} />
      <Tabs.Screen name="shop" options={{ title: 'Магазин', href: enabled('shop') ? undefined : null, tabBarIcon: ({ color, size }) => <ShoppingBag color={color} size={size} /> }} />
      <Tabs.Screen name="diary" options={{ title: 'Дневник', href: enabled('journal') ? undefined : null, tabBarIcon: ({ color, size }) => <BookOpen color={color} size={size} /> }} />
      <Tabs.Screen name="chat" options={{ href: null }} />
    </Tabs>
  );
}
