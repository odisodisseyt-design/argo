import React from 'react';
import { Text as RNText, TextProps, StyleSheet } from 'react-native';
import { usePalette } from '@/theme/usePalette';
import { usePreferences } from '@/store/preferences';
export function AppText({ children, variant = 'body', style, ...props }: TextProps & { variant?: 'title'|'heading'|'body'|'muted'|'label' }) {
  const p = usePalette(); const scale = usePreferences(s => s.fontScale);
  const sizes = { title: 28, heading: 19, body: 15, muted: 13, label: 11 };
  return <RNText {...props} style={[{ color: variant === 'muted' ? p.muted : p.text, fontSize: sizes[variant] * scale, fontWeight: variant === 'title' || variant === 'heading' ? '700' : variant === 'label' ? '700' : '400' }, style]}>{children}</RNText>;
}
