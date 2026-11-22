# ARGO Mobile

Open-source mobile client shell for server-hosted, turn-based multiplayer AI-RPG campaigns.

## Scope
- React Native + Expo Router; Android and iOS only.
- Campaigns are created and curated on the server. Players join existing campaigns.
- The server is the sole source of truth for character stats, inventory, quests, events, turn resolution and outcomes.
- The client renders server data and submits player commands. The demo screens currently use clearly-labelled mock data.

## Run locally
1. Install Node.js LTS and the Expo tooling.
2. Copy `.env.example` to `.env` and set `EXPO_PUBLIC_API_URL` / `EXPO_PUBLIC_WS_URL`.
3. Run `npm install`.
4. Run `npx expo start`.

## Project map
- `app/` — Expo Router routes.
- `features/` — feature-level UI.
- `components/` — reusable visual components.
- `theme/` — theme tokens and theme state.
- `services/` — API, WebSocket and secure storage adapters.
- `store/` — local UI state only.
- `types/` — client contracts pending generation from server OpenAPI.

## Backend integration checklist
See `docs/API_INTEGRATION.md`. Do not implement game calculations in the client.
