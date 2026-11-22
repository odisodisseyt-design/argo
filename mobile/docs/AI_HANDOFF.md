# Instructions for the integration agent

You are integrating the ARGO mobile client with an existing backend.

1. Preserve the visual system and screen hierarchy unless a backend constraint requires a change.
2. Obtain the actual OpenAPI document and WebSocket event contract before coding integration.
3. Generate TypeScript DTOs from OpenAPI; replace provisional `types/api.ts`.
4. Replace mock data in `features/campaigns/mockData.ts` and demo-only UI with query hooks.
5. Implement secure auth using `services/storage/credentials.ts`.
6. Implement authenticated WS subscriptions and reconnection; on reconnect refetch authoritative step status.
7. The four turn phases are server-authoritative. Never advance a phase based on a local timer.
8. Preserve an Idempotency-Key across retries of the same command.
9. Do not calculate stats, combat, loot, progression or outcomes on-device.
10. Add tests for phase transitions, idempotent submission, 401/409/422/503 handling and offline recovery.
11. Add a campaign details/catalog endpoint only if it exists in the server contract. Do not invent endpoints.
12. Keep campaign creation out of the player app.
