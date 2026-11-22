import React from 'react';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { useCurrentGame } from '@/features/game/useCurrentGame';

export default function Chat() {
  const { history } = useCurrentGame();
  const records = history.data?.steps || [];
  return (
    <Screen>
      <TopBar title="История шагов" />
      {records.map((record) => (
        <Panel key={record.step_no}>
          <AppText variant="label" style={{ letterSpacing: 1.5 }}>ШАГ {record.step_no}</AppText>
          <AppText style={{ lineHeight: 22 }}>{record.prose}</AppText>
          {record.outcome_text ? <AppText variant="muted">{record.outcome_text}</AppText> : null}
        </Panel>
      ))}
      {!records.length && <AppText variant="muted">История пока пуста. Отправь первый приказ.</AppText>}
    </Screen>
  );
}
