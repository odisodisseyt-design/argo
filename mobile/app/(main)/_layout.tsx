import React from 'react';
import { Tabs } from 'expo-router';
import { Home, Backpack, UserRound, Map, Settings } from 'lucide-react-native';
import { usePalette } from '@/theme/usePalette';
import { useGame } from '@/features/game/store';

export default function MainLayout() {
  const p = usePalette();
  const features = useGame((s) => s.features);
  const enabled = (key: string) => features[key] !== false;

  return (
    <Tabs screenOptions={{ headerShown: false, tabBarStyle: { backgroundColor: p.surface, borderTopColor: p.border, height: 62, paddingTop: 7, paddingBottom: 7 }, tabBarActiveTintColor: p.accent, tabBarInactiveTintColor: p.muted, tabBarLabelStyle: { fontSize: 10, fontWeight: '700' } }}>
      <Tabs.Screen name="index" options={{ title: 'Главная', tabBarIcon: ({ color, size }) => <Home color={color} size={size} /> }} />
      <Tabs.Screen name="inventory" options={{ title: 'Инвентарь', href: enabled('inventory') ? undefined : null, tabBarIcon: ({ color, size }) => <Backpack color={color} size={size} /> }} />
      <Tabs.Screen name="status" options={{ title: 'Статус', href: enabled('stats') ? undefined : null, tabBarIcon: ({ color, size }) => <UserRound color={color} size={size} /> }} />
      <Tabs.Screen name="map" options={{ title: 'Карта', href: enabled('map') ? undefined : null, tabBarIcon: ({ color, size }) => <Map color={color} size={size} /> }} />
      <Tabs.Screen name="settings" options={{ title: 'Настройки', tabBarIcon: ({ color, size }) => <Settings color={color} size={size} /> }} />
      <Tabs.Screen name="campaigns/[id]" options={{ href: null }} />
      <Tabs.Screen name="nearby" options={{ href: null }} />
      <Tabs.Screen name="quests" options={{ href: enabled('quests') ? undefined : null }} />
      <Tabs.Screen name="shop" options={{ href: enabled('shop') ? undefined : null }} />
      <Tabs.Screen name="diary" options={{ href: enabled('journal') ? undefined : null }} />
      <Tabs.Screen name="chat" options={{ href: null }} />
    </Tabs>
  );
}
