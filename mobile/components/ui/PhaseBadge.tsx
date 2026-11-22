import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import type { CampaignPhase } from '@/types/api';
import { usePalette } from '@/theme/usePalette';
const labels: Record<CampaignPhase,string> = { orders:'ПРИЁМ ПРИКАЗОВ', waiting:'ОЖИДАНИЕ', resolving:'РАЗРЕШЕНИЕ', results:'РЕЗУЛЬТАТЫ' };
export function PhaseBadge({ phase }: { phase: CampaignPhase }) {
 const p=usePalette(); const color=phase==='orders'?p.success:phase==='waiting'?p.accent:phase==='resolving'?p.purple:p.gold;
 return <View style={[styles.badge,{backgroundColor:color+'18',borderColor:color+'66'}]}><View style={[styles.dot,{backgroundColor:color}]} /><Text style={{color,fontSize:10,fontWeight:'800',letterSpacing:1}}>{labels[phase]}</Text></View>;
}
const styles=StyleSheet.create({badge:{alignSelf:'flex-start',flexDirection:'row',alignItems:'center',gap:7,borderWidth:1,borderRadius:30,paddingHorizontal:10,paddingVertical:7},dot:{width:6,height:6,borderRadius:4}});
