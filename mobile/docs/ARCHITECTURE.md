# Architecture

The app is divided into routes, feature UI, services and local presentation state.

- TanStack Query owns cached server state.
- Zustand owns presentation preferences (theme, font scale, haptics) and ephemeral UI state.
- SecureStore owns credentials; never persist tokens in AsyncStorage.
- WebSocket events invalidate/update query cache; they do not become a second source of truth.
- Theme packs change presentation tokens only, never game data or rules.

Campaign creation is intentionally absent from the player client. The server publishes campaigns for players to discover and join.
