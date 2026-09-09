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

Built-in themes are registered in the large static initialization section of `Theme.java`. The existing built-ins (`Blue`, `Dark Blue`, `Arctic Blue`, `Day`, `Night`) are instantiated there with asset names and accent tables. `cybergram.attheme` is currently shipped as an asset but is not yet registered as a built-in theme. This is intentionally deferred rather than performing a broad, difficult-to-review rewrite of `Theme.java` without a local build loop.

## Verification status

Repository-side changes have been written successfully to GitHub.

No Android compilation or runtime test has been executed from this ChatGPT environment. Therefore there is no build-pass claim yet.

The next machine-side checkpoint must be a clean build of the untouched/upstream-compatible branch and then a `dev` build with the new additive Cybergram files. Any compile failure should be fixed before integrating the custom bubble drawable into Telegram's message renderer.

## Next implementation sequence

1. Clone `mopsyatina228/cybergram` with submodules on the development machine and add `DrKLO/Telegram` as `upstream`.
2. Build the existing project using Android Studio 2025.1.4, Android SDK 36 and NDK 27.2.12479018.
3. Build `dev` and confirm the additive Cybergram classes/assets compile cleanly.
4. Make `Cybergram` selectable/automatically applied using the existing Telegram theme pipeline.
5. Tune the `.attheme` palette from device screenshots.
6. Integrate angular geometry into `MessageDrawable` behind a narrow Cybergram-specific seam.
7. Validate plain text, grouped messages, replies, reactions, forwards, media, selection and pressed states before extending the design to the composer and dialog list.

## Explicitly deferred

- package/application ID rename;
- production `api_id` / `api_hash` configuration;
- signing and Firebase credentials;
- app icon and final branding;
- proprietary game fonts/assets;
- broad package refactors;
- protocol/business-logic changes.

These remain deferred until a reproducible local debug build exists.
