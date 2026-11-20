# Text World — Architecture (English)

## Principle

The device is a **renderer of server decisions**. It contains:

- no rules engine, no dice, no probabilities, no damage/death computation;
- no LLM call, no prompt, no API key;
- no world state, no local item creation, no shop stock;
- no analytics, ads, telemetry or proprietary SDK.

Everything the player sees is either (a) a value delivered by the server, or
(b) a purely visual treatment derived from `presentation` (colors, particles,
parallax, haptics, typography pace). If the server did not send a value, the UI
does not display it. Raw field names, internal ids, reason codes, warnings,
event types, request ids, logs and stack traces are never rendered.

## Layers

```
ui/                     Composable screens + MainViewModel
presentation/
  renderer/             PresentationRenderer (single parse point)
  theme/                BOOK skin (warm parchment, serif)
  components/           Canvas particles/parallax/vignette, SYSTEM WINDOW, prose,
                        micro-interactions (page turn, paper slip, death veil)
  gateways/             VoiceGateway, ImageGateway, AmbientAudioGateway,
                        HapticsGateway
data/
  repository/           GameRepository: UI state + server command dispatch
  network/              TextWorldApi (REST/SSE), RealtimeClient (WS + fallback),
                        UpdateRepository (GitHub OTA)
  model/                lenient DTOs; unknown keys ignored
  token/                EncryptedSharedPreferences + Android Keystore
  settings/             player toggles + active campaign id
di/                     manual ServiceLocator
```

## Data flow

1. `TextWorldApp` builds `ServiceLocator`.
2. `MainViewModel.bootstrap()` calls `GET /api/health`. It handles version gating,
   offline mode ("The book is closed") and token auto-login.
3. `POST /api/turn` returns SSE events `meta`, `delta`, `final`. `GameRepository`
   appends `delta` prose and, on `final`, swaps in the server-sent `anchor`,
   `character`, `presentation`, `outcome_text` and `pending_details`.
4. `WebSocket /ws/campaign/{id}` frames are `{"event": ..., "payload": {...}}`.
   `RealtimeClient` reconnects with exponential backoff and offers a long-poll
   fallback. Out-of-turn and frozen-step messages come from the server phrase.
5. Every presentation update is pushed into `PresentationRenderer`, which
   produces a `RenderState`; `BookTheme` interpolates colors over 500–900 ms.

## `ui_manifest` rendering

`UiManifest` drives the entire UI. `tabsFor(manifest)` builds the destination
list. A feature that is `false` never creates a tab, screen or icon — it is not
greyed out. When the pack changes, navigation is rebuilt from the new manifest.

Feature flags map to screens as follows:

| Flag | Effect |
|---|---|
| `stats`, `skills`, `effects`, `currency`, `level_up` | Book tabs, level-up badge |
| `inventory` | Inventory screen |
| `shop` | System Shop screen |
| `quests`, `map`, `characters`, `journal` | Corresponding screens |
| `voice`, `images`, `audio`, `haptics`, `parallax` | Settings toggles + renderers |
| `magic`, `crafting`, `reputation` | Reserved feature keys (no UI until a pack renders them) |

`stat_schema` bounds the level-up +/- buttons; `status_icons` maps condition
names to glyphs and falls back to a neutral rune for unknown strings;
`show_odds` gates honest probability display in the shop; `shop_tabs` /
`shop_services` come from the pack.

## Two visual languages

- **THE BOOK** — `BookTheme` + serif text + parchment/vignette. Used for scene,
  around, character, map, journal, characters, saves.
- **THE SYSTEM WINDOW** — `SystemWindow` composable: cold, geometric, thin lines,
  monospaced numerals, slow scan. Used for stats/skills, inventory, shop, quests.
  `presentation.system_theme` selects `hologram` / `runic` / `symbiote` colors
  only; geometry is identical. The contrast between the two is intentional.

Health is never numeric. Only `character.body`, `conditions` and `statuses`
are shown, as delivered. Attributes (e.g. Strength 18) are ordinary numbers
and are allowed.

## PresentationRenderer

`PresentationRenderer.submit(presentation, manifest, settings)` is the only
place that interprets `presentation`. It resolves:

- palette (with optional environment overrides) -> animated Compose colors;
- mood table -> vignette alpha, desaturation, pulse, accent tint;
- weather table -> particle density/speed multipliers;
- environment table (e.g. `forest/day`, `forge/night`, `snow/dusk`, `library`)
  -> palette, particles, audio cues, illustration hint;
- `system_theme`, `typography.pace_ms`, `dropcap`, `haptic`, `emotion`;
- deterministic `sceneKey` from `illustration.seed` + subject + mood + weather
  + time of day. The procedural Canvas uses this seed, so the same seed always
  produces the same picture. If `illustration.url` / a portrait URL arrives, the
  image replaces procedural art without changing any screen.

Adding a mood, weather or environment is **one table row**. Unknown values fall
back to neutral defaults; `schema_version` is carried but unknown fields are
ignored by kotlinx.serialization. Theme changes interpolate over 500–900 ms;
`reduce_motion` and `animations=false` disable ambient motion.

## Gateways

All four ship with `isAvailable = false` and render no UI:

| Gateway | Responsibility | Future implementation |
|---|---|---|
| `VoiceGateway` | `transcribe(audio)`, `speak(text)` | push-to-talk -> `/api/voice/...`; microphone permission requested only on explicit enable |
| `ImageGateway` | resolve `scene_bg` / `portrait:<id>` slots by deterministic seed | generated images keyed by seed; screens unchanged |
| `AmbientAudioGateway` | layered loops from `audio_cues` with crossfade | silence today, architecture ready |
| `HapticsGateway` | profiles per `presentation.haptic` | `AndroidHapticsGateway` uses Vibrator when pack + player allow it |

`PresentationRenderer` is intentionally a fifth, always-on renderer gateway.

## Networking & auth

- Ktor `HttpClient` with OkHttp engine, WebSockets, no body logging.
- Token attached as `Authorization: Bearer <token>`, stored encrypted; password
  never persisted.
- HTTPS enforced by `network_security_config.xml`.
- All calls return `Outcome<T>`; transport errors are values, never crashes.
- Reconnect resumes from the last `step_no`; one order per participant per step
  is enforced by the server.

## Self-update

`UpdateRepository` reads `version.json` (`versionCode`, `versionName`,
`changelog`, `apk_url`, `sha256`, `min_client_version`), streams the APK with
progress, verifies SHA-256 before install, and uses `FileProvider` +
`ACTION_VIEW`. It handles `REQUEST_INSTALL_PACKAGES` and the "Install unknown
apps" flow with in-app instructions. Package name and signing key are constant
so OTA never clears data. GitHub down -> quiet "Could not check for updates".

## Accessibility & motion

Edge-to-edge, portrait primary, tablet navigation rail at >= 600 dp, TalkBack
content descriptions, WCAG AA contrast from the palette, font scale 0.9–1.6
without clipping. `reduce_motion` disables particles animation, drifting
parallax, scanlines, sheen, page turns and death desaturation.
