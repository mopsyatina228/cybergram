# Cybergram work state

Last updated: 2026-09-09

## Repository authority

Repository: `mopsyatina228/cybergram`

Upstream: `DrKLO/Telegram`

Upstream baseline: Telegram Android 12.10.1 (7038), commit `62b56a07ca7e30e39f7fd00a6728d6bbd716ca1c`

Branch policy:

- `master` remains the upstream-aligned baseline.
- `dev` is the Cybergram integration branch.
- product changes should not be committed directly to `master`.

## Current implementation state

The Cybergram fork has been bootstrapped on `dev` without intentional protocol, networking, storage, authentication or encryption changes.

Added project documentation and repository guardrails:

- `README.md`
- `AGENTS.md`
- `docs/CYBERGRAM_UI_SPEC.md`

Added the first executable visual palette:

- `TMessagesProj/src/main/assets/cybergram.attheme`

Added Cybergram-owned UI primitives/constants:

- `TMessagesProj/src/main/java/org/telegram/ui/ActionBar/CybergramTheme.java`
- `TMessagesProj/src/main/java/org/telegram/ui/ActionBar/CybergramBubbleDrawable.java`

`CybergramBubbleDrawable` is deliberately not wired into Telegram's `MessageDrawable` yet. It provides the first angular clipped-corner panel primitive while keeping the existing message renderer untouched until grouped messages, media clipping and selection behaviour can be tested on a real Android build.

## Theme-system findings

Telegram already separates most colours needed for the Cybergram chat design through theme keys, including incoming/outgoing bubbles, message text, links, timestamps, views, sent checks, reply blocks, composer colours and action-bar/list colours.

`MessageDrawable` reads the incoming/outgoing bubble colour keys directly, so colour prototyping can be done without replacing message rendering.

### Built-in Cybergram theme

The `Cybergram` theme is now registered as a built-in theme in the static initialization section of `Theme.java`:

- name/key: `Cybergram`
- asset: `cybergram.attheme`
- preview background `#080A0F`, incoming `#E8D93A`, outgoing `#0A1A21`
- `sortIndex = 6` (existing built-in order unchanged)
- no accent tables (Cybergram uses its own fixed palette)
- added to both `themes` and `themesDict`; `ThemeInfo.isDark()` resolves it as a dark theme

`isDark()` detection was made generic for asset-backed themes: when `assetName` is non-empty the theme file values are read via `getThemeFileValues(null, assetName, ...)`; otherwise the existing file-backed path is used; the existing `checkIsDark(...)` fast path for Blue / Dark Blue / Arctic Blue / Day / Night is unchanged.

### Fresh-install default

On a genuinely fresh Cybergram install (no `theme`/`nighttheme` preference and no `lastDayTheme`/`lastDarkTheme` bookkeeping ever written), `Theme.java` seeds `theme=Cybergram` and `nighttheme=Cybergram` into the existing `mainconfig` preferences, so Cybergram becomes the active day and night theme and the initially applied theme. An existing user's saved choice is never overwritten: the seed only runs when none of those keys are present, and on every subsequent launch the standard Telegram preference priority applies.

### .attheme CRLF handling

With `core.autocrlf=true` on Windows, `.attheme` assets are checked out CRLF; the upstream `getThemeFileValues` parser keeps the trailing `\r` in the value string, which makes `Utilities.parseInt` return `0` for every colour. `getThemeFileValues` now strips a single trailing CR (`charAt(last) == 13`) before parsing, so CRLF `.attheme` files (imported or Windows-checked-out) parse correctly. `.gitattributes` pins `*.attheme text eol=lf`.

## Verification status

- Repository-side changes written successfully to GitHub.
- Local single-ABI debug build established: `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a` (Gradle 8.11.1, AGP 8.10.1, JDK 17, SDK 36, NDK 27.2.12479018) — `BUILD SUCCESSFUL`.
- Runtime smoke test on Samsung SM-A256E / Android 16 / arm64-v8a: debug build `org.telegram.messenger.beta` cold-launches, `LaunchActivity` alive, no FATAL/ANR.
- Fresh-install default proven on-device: after `pm clear`, `mainconfig` contains `theme=Cybergram` and `nighttheme=Cybergram`; `themeconfig` contains `lastDayTheme=Cybergram` and `lastDarkTheme=Cybergram`.
- Persistence proven on-device: simulating an existing choice (`theme=Day`, `nighttheme=Day`) then relaunching leaves `theme=Day`/`nighttheme=Day` — Cybergram does not overwrite it.
- The `IntroActivity` screen is rendered with the client default surface and does not visually reflect the Cybergram palette in this build; this is a known display behaviour of the pre-auth intro and is intentionally out of scope for the theme-default change (per `docs/CYBERGRAM_UI_SPEC.md` Stage B note). The theme itself is active (day/night/preferences all resolve to Cybergram).

## Next implementation sequence

1. Make `Cybergram` selectable/automatically applied using the existing Telegram theme pipeline (done — built-in registration + fresh-install default).
2. Tune the `.attheme` palette from device screenshots.
3. Integrate angular geometry into `MessageDrawable` behind a narrow Cybergram-specific seam.
4. Validate plain text, grouped messages, replies, reactions, forwards, media, selection and pressed states before extending the design to the composer and dialog list.

## Explicitly deferred

- package/application ID rename;
- production `api_id` / `api_hash` configuration;
- signing and Firebase credentials;
- app icon and final branding;
- proprietary game fonts/assets;
- broad package refactors;
- protocol/business-logic changes.

These remain deferred until a reproducible local debug build exists.
