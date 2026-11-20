# Third-party assets

No third-party images, fonts, audio or icon packs are bundled with this
application. All visuals are drawn procedurally with Compose Canvas from the
deterministic `illustration.seed` delivered by the server.

- Typefaces: platform serif/monospace families (`FontFamily.Serif`,
  `FontFamily.Monospace`). No font files are shipped.
- Icons: small vector drawables and Unicode glyphs created for this project
  (`ic_launcher_foreground.xml`, `ic_send.xml`), covered by the MIT license.
- Server-provided `illustration.url` / portrait URLs, when present, are loaded
  with Coil (Apache-2.0) and remain the property of their respective pack
  authors. A pack MUST ship its own asset licenses.

Dependencies are open-source: Kotlin/Coroutines/Flow (Apache-2.0),
Jetpack Compose & Material 3 (Apache-2.0), Ktor (Apache-2.0), Coil (Apache-2.0),
Room (Apache-2.0),kotlinx.serialization (Apache-2.0),
Security-Crypto (Apache-2.0).
