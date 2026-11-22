import React from 'react';
import { View, StyleSheet } from 'react-native';
import { AppText } from '@/components/ui/Text';
export function SectionTitle({ title, action }: { title: string; action?: string }) {
 return <View style={styles.row}><AppText variant="heading">{title}</AppText>{action ? <AppText variant="muted">{action}</AppText> : null}</View>;
}
const styles=StyleSheet.create({row:{flexDirection:'row',justifyContent:'space-between',alignItems:'center'}});
