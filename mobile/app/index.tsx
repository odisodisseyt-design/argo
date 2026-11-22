import React, { useEffect } from 'react';
import { Redirect } from 'expo-router';
import { credentials } from '@/services/storage/credentials';
import { restoreActiveCampaign } from '@/features/game/store';

export default function Index() {
  const [ready, setReady] = React.useState(false);
  const [authed, setAuthed] = React.useState(false);

  useEffect(() => {
    (async () => {
      const token = await credentials.get();
      setAuthed(Boolean(token));
      if (token) await restoreActiveCampaign();
      setReady(true);
    })();
  }, []);

  if (!ready) return null;
  return <Redirect href={authed ? '/(main)' : '/(auth)/login'} />;
}
