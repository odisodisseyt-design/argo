import React from 'react';
import { View, StyleSheet, ViewStyle } from 'react-native';
import { usePalette } from '@/theme/usePalette';
export function Panel({ children, style }: { children: React.ReactNode; style?: ViewStyle }) {
  const p = usePalette();
  return <View style={[styles.panel, { backgroundColor: p.surface, borderColor: p.border }, style]}>{children}</View>;
}
const styles = StyleSheet.create({ panel: { borderWidth: 1, borderRadius: 18, padding: 16, gap: 10 } });
