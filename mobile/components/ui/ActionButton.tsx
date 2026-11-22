import React from 'react';
import { Pressable, Text, StyleSheet, ViewStyle } from 'react-native';
import { usePalette } from '@/theme/usePalette';
export function ActionButton({ title, onPress, secondary = false, style }: { title: string; onPress?: () => void; secondary?: boolean; style?: ViewStyle }) {
  const p = usePalette();
  return <Pressable onPress={onPress} style={[styles.button, { backgroundColor: secondary ? p.surface2 : p.accent, borderColor: p.border }, style]}>
    <Text style={{ color: secondary ? p.text : '#06121A', fontWeight: '800', fontSize: 14 }}>{title}</Text>
  </Pressable>;
}
const styles = StyleSheet.create({ button: { minHeight: 46, paddingHorizontal: 16, borderRadius: 13, justifyContent: 'center', alignItems: 'center', borderWidth: 1 } });
