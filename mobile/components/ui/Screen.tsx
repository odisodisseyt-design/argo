import React from 'react';
import { ScrollView, View, StyleSheet, ViewStyle } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { usePalette } from '@/theme/usePalette';

export function Screen({ children, scroll = true, style }: { children: React.ReactNode; scroll?: boolean; style?: ViewStyle }) {
  const p = usePalette();
  return <SafeAreaView style={[styles.safe, { backgroundColor: p.background }]}>
    {scroll ? <ScrollView contentContainerStyle={[styles.content, style]} showsVerticalScrollIndicator={false}>{children}</ScrollView>
      : <View style={[styles.content, style]}>{children}</View>}
  </SafeAreaView>;
}
const styles = StyleSheet.create({ safe: { flex: 1 }, content: { padding: 18, paddingBottom: 34, gap: 16 } });
