import React from 'react';
import { View, StyleSheet } from 'react-native';
import { AppText } from '@/components/ui/Text';
import { usePalette } from '@/theme/usePalette';
export function TopBar({ title, eyebrow = 'ARGO' }: { title: string; eyebrow?: string }) {
 const p = usePalette();
 return <View style={styles.wrap}><View><AppText variant="label" style={{color:p.accent,letterSpacing:2}}>{eyebrow}</AppText><AppText variant="title">{title}</AppText></View><View style={[styles.avatar,{borderColor:p.border,backgroundColor:p.surface2}]}><AppText style={{fontWeight:'800'}}>A</AppText></View></View>;
}
const styles=StyleSheet.create({wrap:{flexDirection:'row',alignItems:'center',justifyContent:'space-between'},avatar:{width:42,height:42,borderRadius:15,borderWidth:1,alignItems:'center',justifyContent:'center'}});
