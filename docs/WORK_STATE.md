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

### Palette key audit

`cybergram.attheme` was audited against the actual `ThemeColors.createColorKeysMap()` dictionary (the same set `stringKeyToInt()` resolves). Two keys were not recognised by current upstream and were renamed while keeping their values:

- `graySectionText` → `key_graySectionText` (registered upstream name carries the `key_` prefix)
- `listSelector` → `listSelectorSDK21` (current upstream string name for `key_listSelector`)

A deterministic repository-side regression check was added: `tools/validate_cybergram_theme.py` (pure Python, no Android SDK) reads `ThemeColors.java` and `cybergram.attheme`, and fails non-zero on unknown / duplicate / malformed keys. After the fix: `unknown=0 duplicates=0 malformed=0`.

Control palette verified as intended: background `#080A0F`, header/panel `#0B0D12`/`#111820`, primary cyan `#00E5FF`, incoming bubble amber `#E8D93A`, outgoing bubble `#0A1A21`, danger `#FF2E46`, main text `#E6F2F2`, muted text `#7C8A91`. The only `alpha=00` values are deliberate disabled shadows (`chat_inBubbleShadow`, `chat_outBubbleShadow`, `chat_messagePanelShadow`).

## Verification status

- Repository-side changes written successfully to GitHub.
- Local single-ABI debug build established: `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a` (Gradle 8.11.1, AGP 8.10.1, JDK 17, SDK 36, NDK 27.2.12479018) — `BUILD SUCCESSFUL`.
- Runtime smoke test on Samsung SM-A256E / Android 16 / arm64-v8a: debug build `org.telegram.messenger.beta` cold-launches, `LaunchActivity` alive, no FATAL/ANR.
- Fresh-install default proven on-device: after `pm clear`, `mainconfig` contains `theme=Cybergram` and `nighttheme=Cybergram`; `themeconfig` contains `lastDayTheme=Cybergram` and `lastDarkTheme=Cybergram`.
- Persistence proven on-device: simulating an existing choice (`theme=Day`, `nighttheme=Day`) then relaunching leaves `theme=Day`/`nighttheme=Day` — Cybergram does not overwrite it.
- The `IntroActivity` screen is rendered with the client default surface and does not visually reflect the Cybergram palette in this build; this is a known display behaviour of the pre-auth intro and is intentionally out of scope for the theme-default change (per `docs/CYBERGRAM_UI_SPEC.md` Stage B note). The theme itself is active (day/night/preferences all resolve to Cybergram).

## Debug showcase Activity (2026-09-09)

A DEBUG-ONLY visual showcase was added for offline UI iteration, so palette/geometry changes can be reviewed without a Telegram account or login.

Files (uncommitted recovery snapshot, all in `debug` source set / debug manifests so they are absent from release builds):

- `org.telegram.ui.CybergramShowcaseActivity` in `TMessagesProj_App/src/debug/java/org/telegram/ui/` — self-contained `Activity` that draws a header bar, incoming/outgoing message bubbles and a media bubble via the real production `MessageDrawable`, a composer bar, and a runtime debug strip. v1 read colours through `Theme.getColor(...)`; later rewritten (v2, below) to a deterministic Cybergram palette via a DEBUG `Theme.ResourcesProvider`.
- `org.telegram.ui.CybergramShowcaseActivity` declared in `TMessagesProj/config/debug/AndroidManifest.xml` and `AndroidManifest_SDK23.xml` with `android:exported="true"` and NO launcher `intent-filter` (started only via an explicit `adb` intent). Never merged into release manifests.

Verified this session (device Samsung SM-A256E / Android 16 / arm64-v8a):

- Build: `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a` -> APK `TMessagesProj_App/build/outputs/apk/afat/debug/app.apk`, single ABI `arm64-v8a`, package `org.telegram.messenger.beta`, versionCode `70389`, versionName `12.10.1`, size 72,376,594 B, SHA-256 `5f3860788ab6e50d2252f89219e00cd38147aeedabefe2dd490529c3bf4b9433`.
- Install: same APK installed on-device (`lastUpdateTime` 2026-09-09 16:27); installed `base.apk` SHA-256 and size match the host APK byte-for-byte, so the showcase Activity is present in the installed build.
- Launch: `adb shell am start -n org.telegram.messenger.beta/org.telegram.ui.CybergramShowcaseActivity` -> `topResumedActivity` confirmed, PID 8986, no FATAL/ANR.
- Screenshot: `.local-artifacts/cybergram_showcase_v1.png` (1080x2340, git-ignored).
- Runtime debug values read from the on-screen debug strip:
  - theme name = `Day`, dark = `false`
  - `windowBackgroundWhite` = `#FFFFFFFF`
  - `chat_inBubble` = `#FFF0F0F0`
  - `chat_outBubble` = clipped at the right screen edge (the debug strip text overflows the canvas width, so the final hex is not rendered); the rendered outgoing bubble and media placeholder are dark/near-black.

Observation (recorded, not acted on this session): the debug strip reports `day.attheme`-derived values (e.g. `chat_inBubble #FFF0F0F0`, which is day.attheme's `#7FF0F0F0` with alpha normalised to `FF`), yet the rendered outgoing bubble appears near-black rather than day.attheme's `chat_outBubble` (`#7F2D7ED5` -> `#FF2D7ED5`, a blue). Worth investigating whether the active palette / `MessageDrawable` colour path matches expectation. Per recovery constraints no code was changed based on this.

### Deterministic palette rewrite (v2, 2026-09-09)

v1 read colours through the global `Theme.getColor(...)`, so after the persistence test the active theme was `Day` and the polygon did not actually show the Cybergram palette; the debug strip also overflowed the 1080 px canvas (clipping `chat_outBubble`), and the bubble radius was hard-overridden with `setRoundRadius(dp(12))`. v2 fixes all of it (production `MessageDrawable` / `CybergramBubbleDrawable` unchanged):

- `Palette` — a DEBUG-only `Theme.ResourcesProvider` that loads `Theme.getThemeFileValues(null, "cybergram.attheme", null)` into a `SparseIntArray` and returns that Cybergram value for any key present; keys absent from the palette fall back to `ThemeColors.createDefaultColors()` (deterministic upstream defaults), never the user's Day/Night theme. `getColor` and `getCurrentColor` both route through the provider, so `Theme.getColor`/`currentColors` are never consulted and the active user theme is untouched (no `Theme.applyTheme`, no preference writes, no `pm clear`).
- All `MessageDrawable` instances are created as `new MessageDrawable(type, out, selected, palette)` and driven by the full production contract: `setBounds(...)` + `setTop(top, w, h, false, false)` (which selects `chat_inBubble`/`chat_outBubble` and initialises paint/gradient state) + `draw(...)`. `setDrawFullBubble(true)` renders the standalone production tail. The `setRoundRadius(dp(12))` override was REMOVED, so the radius is the genuine `SharedConfig.bubbleRadius` (default 17) from `MessageDrawable`'s own geometry path.
- Media example uses `MessageDrawable.TYPE_MEDIA`.
- Multiline text is laid out with `StaticLayout` (newline + wrapping work; bubble height derives from the real layout) instead of a single `Canvas.drawText`.
- Header / composer / service colours come from the same `Palette`/provider (e.g. `actionBarDefault`, `actionBarDefaultTitle`, `actionBarDefaultSubtitle`, `chat_messagePanelBackground`, `chat_messagePanelSend`, `chat_messagePanelHint`, `chat_serviceBackground`, `chat_serviceText`, `chat_messageTextIn`, `chat_messageTextOut`).
- Debug banner is now 3 compact lines:
  `Showcase: Cybergram | dark=true` / `Active app theme: Day` / `BG #080A0F | IN #E8D93A | OUT #0A1A21`
  It deliberately shows BOTH the showcase palette (Cybergram) and the active user theme (read-only, name from `Theme.getCurrentTheme()`), so it is visible the showcase does not change preferences.

Verified on-device (SM-A256E / Android 16 / arm64-v8a):

- Pre-launch read-only: `mainconfig.xml` has `theme=Day`, `nighttheme=Day` — the saved active theme stays `Day`.
- Build/install: `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a` (BUILD SUCCESSFUL, warm 7 s); APK single ABI `arm64-v8a`, SHA-256 `53d07f03c461ee3a13c8f15af89ec41c067212c28294ef319ec7bf3cb5b3a1a7`; installed over `org.telegram.messenger.beta` (`pm install -r`, Success, `lastUpdateTime` 17:09:27).
- Launch: explicit adb intent -> `topResumedActivity=...CybergramShowcaseActivity`, PID 14513, no FATAL/ANR.
- Screenshot: `.local-artifacts/cybergram_showcase_v2.png` (1080x2340, git-ignored). On-screen debug banner reads verbatim: `Showcase: Cybergram | dark=true`, `Active app theme: Day`, `BG #080A0F | IN #E8D93A | OUT #0A1A21`.
- Runtime palette (from the banner): BG `windowBackgroundWhite` `#080A0F`, IN `chat_inBubble` `#E8D93A`, OUT `chat_outBubble` `#0A1A21`. Rendered bubbles match these values (incoming amber `#E8D93A`, outgoing dark `#0A1A21`), with production rounded corners + tails and no clipping; multiline renders correctly; composer/header correct.
- Post-close read-only: `mainconfig.xml` still `theme=Day`, `nighttheme=Day`; `themeconfig.xml` still `lastDayTheme=Cybergram`, `lastDarkTheme=Cybergram` — the showcase did not modify the user's theme choice.

This resolves the v1 colour-path observation: the showcase now renders the deterministic Cybergram palette regardless of the active Day/Night theme.

## Angular message geometry (Stage C — geometry-only pass, 2026-09-09)

First production integration of Cybergram geometry: it replaces the Telegram rounded/tail bubble silhouette with the Cybergram clipped-corner silhouette, geometry only — colours, text, layout, sizes and logic are untouched. Production `MessageDrawable` / `CybergramBubbleDrawable` internals and behaviour are preserved.

### Activation seam (centralised in `CybergramTheme`)

- `CybergramTheme.THEME_NAME = "Cybergram"` and `CybergramTheme.BUBBLE_CORNER_CUT_DP = 6f`.
- `CybergramTheme.GeometryProvider extends Theme.ResourcesProvider` — a marker interface that opts a renderer into Cybergram geometry.
- `CybergramTheme.useAngularMessageGeometry(Theme.ResourcesProvider provider)` returns true iff `provider instanceof GeometryProvider` OR the active `Theme.getCurrentTheme()` is the built-in Cybergram theme; false otherwise. It never infers Cybergram from colours. It is called from a single place (`MessageDrawable.useAngularGeometry()`); no theme-name checks are scattered through the drawable. This covers both real clients (usually `provider == null`, global theme = Cybergram) and the debug showcase (global theme = Day, enabled via the marker `GeometryProvider`).

### Shared polygon builder

- `CybergramBubbleDrawable.buildPath(Path, float left, float top, float right, float bottom, float cut)` — new `public static` helper producing the four-straight-side / four-45°-chamfered-corner Cybergram polygon (no arcs, no round radius); the cut is clamped to at most half the smaller side. Used by BOTH the standalone `CybergramBubbleDrawable` (its `rebuildPath` now delegates here) and `MessageDrawable`, so there is exactly one implementation of the Cybergram polygon.

### `MessageDrawable` seam

- A narrow branch at the top of `generatePath(...)`: if `useAngularGeometry()` and `currentType` is `TYPE_TEXT` or `TYPE_MEDIA`, build the Cybergram silhouette and return; `TYPE_PREVIEW` stays on the Telegram path. When Cybergram geometry is not active, the exact upstream rounded-path generation is preserved (the branch is a no-op).
- `generateCybergramPath(...)` mirrors the upstream body area so text/layout does not shift:
  - outgoing text: left = `bounds.left + padding`, right = `bounds.right - dp(8)`;
  - incoming text: left = `bounds.left + dp(8)`, right = `bounds.right - padding`;
  - media: left = `bounds.left + padding`, right = `bounds.right - padding`;
  - vertical: `bounds.top + padding` .. `bounds.bottom - padding`, where `padding = dp(2)`.
- The 8dp tail region is NOT reclaimed; the silhouette keeps the existing content geometry.
- The existing selected fill/overlay and the cached/nine-patch rendering both flow through the same Cybergram Path (verified: the selected outgoing bubble renders as `chat_outBubbleSelected` (`#10313C`-ish) on the same silhouette).

### Scope / deferred

- No border/stroke layer yet (CybergramBubbleDrawable has stroke support, but MessageDrawable still runs through its own Paint/Path — a border will be a following layer after visual validation). No cyan stroke is hard-coded in MessageDrawable.
- Grouped `topNear` / `bottomNear` / `botButtonsBottom` API is accepted and preserved; the first Cybergram polygon uses the SAME corner cut on all four corners regardless of near flags. Grouped-near-specific corner treatment is DEFERRED (see Next sequence).
- `cybergram.attheme`, Theme registration/default logic, `ChatMessageCell` layout, composer production UI, dialogs, IntroActivity, network/auth/storage, package/signing/API credentials — all untouched.

### Verified (SM-A256E / Android 16 / arm64-v8a)

- Build: `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a` BUILD SUCCESSFUL; APK single ABI `arm64-v8a`, SHA-256 `76c85d54a6641be7e1cf6895f9c8869c3f1ee824d01e622e51a6b5e40e5e6a88`.
- Install: over `org.telegram.messenger.beta` via `adb install -r` (streamed). NOTE: the previously-proven `push + pm install` file:// path was rejected by on-device verification this time (`INSTALL_FAILED_VERIFICATION_FAILURE`), so the direct streamed install was used. No `pm clear`; data kept.
- Pre-launch read-only: `mainconfig.xml` `theme=Day`, `nighttheme=Day` (unchanged).
- Launch showcase: explicit adb intent -> `topResumedActivity=...CybergramShowcaseActivity`, no FATAL/ANR.
- Screenshot: `.local-artifacts/cybergram_showcase_v3_angular.png` (1080x2340, git-ignored).
- Banner still reads verbatim: `Showcase: Cybergram | dark=true` / `Active app theme: Day` / `BG #080A0F | IN #E8D93A | OUT #0A1A21`.
- Geometry acceptance: Telegram tails gone (vision + pixel); all four corners are 45° chamfers (pixel-verified straight diagonal on the incoming bubble: top-left 74->57 and top-right 817->834 over ~14 rows then straight edges, bottom-left 57->74); body alignment vs text unchanged (incoming body left = `bounds.left + 8dp`); selected outgoing bubble renders (lighter teal, `#10313C`-ish); TYPE_MEDIA uses the same silhouette; multiline not clipped; FATAL/ANR = 0.
- v2 -> v3 comparison (read-only vision): geometry only — v2 had rounded corners + pointed tails; v3 has chamfered corners + no tails; colours (`IN #E8D93A`, `OUT #0A1A21`), layout and banner unchanged. The 6dp cut reads as a modest chamfer on 1080x2340.
- Regression: normal beta client launched at `theme=Day` (launcher activity top, no FATAL/ANR) — the Cybergram code present in the APK does NOT flip Day-theme bubbles to angular (the activation gate returns false). Prefs still `theme=Day`, `nighttheme=Day`.

Note (pre-existing, out of scope): the debug composer placeholder "Сообщение" sits low and reads as partially clipped; this predates the geometry pass and is not a regression of this task.

## Next implementation sequence

1. Make `Cybergram` selectable/automatically applied using the existing Telegram theme pipeline (done — built-in registration + fresh-install default).
2. Tune the `.attheme` palette from device screenshots.
3. Integrate angular geometry into `MessageDrawable` behind a narrow Cybergram-specific seam. (DONE — geometry-only pass. Follow-ups: `TYPE_PREVIEW` support, grouped-near-specific corner treatment, and the border/stroke layer.)
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
