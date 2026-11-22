# API integration handoff

This repository is a visual/client foundation, not a claim that the production API contract is known.
Before connecting the backend, import its OpenAPI schema and replace provisional types in `types/api.ts`.

## Required contract confirmation
- Auth endpoints, token response, refresh semantics and logout.
- Campaign list/detail and pagination.
- `GET /api/campaign/{id}/step/status` exact response DTO.
- `POST /api/turn` command DTO, accepted command types and Idempotency-Key behavior.
- WebSocket URL/auth method, event names, event IDs, heartbeat and replay/resume support.
- Narrator history endpoint and cursor pagination.
- Nearby objects, inventory, character status, map, quests, shop, NPC and chat endpoints.

## Turn phases (server authoritative)
Expected UI states: `orders -> waiting -> resolving -> results`.
The client must not infer phase transitions from elapsed time or local player actions.
- On campaign open: fetch step status, then subscribe to campaign WS.
- After successful command submission: show waiting only when confirmed by server response/status contract.
- `order_submitted`: update submitted roster.
- `step_resolved`: render resolving animation, then results from server payload and refetch authoritative state.
- `order_timeout`: append the server-provided timeout narration.
- WS reconnect: exponential backoff, then refetch step status and relevant queries.
- 409: retry only according to server `retry_after`, preserving the exact Idempotency-Key.
- 422: show field-level validation.
- 503: non-blocking toast.
- 401: clear credentials and return to auth.
- Offline: cache display; queue a pending command only if backend semantics permit safe replay with the same key.

## Non-negotiable
No client-side stat calculations, combat resolution, loot generation, quest completion, inventory mutations or campaign state authority.
Every action that changes game state is a command sent to the server.
