# Cybergram work state

Last updated: 2026-09-10

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

## Angular message geometry — border + grouped corners (Stage C, pass 2, 2026-09-09)

Second pass on the Cybergram message silhouette: add a thin outline and make `topNear`/`bottomNear` visually meaningful for the angular bubbles. Geometry only — layout, colours, text, sizes and Telegram logic unchanged. Old Telegram rendering is untouched when Cybergram geometry is off.

### Per-corner builder API

- `CybergramBubbleDrawable.buildPath(Path, left, top, right, bottom, cut)` (uniform) now delegates to a new overload `buildPath(Path, left, top, right, bottom, topLeftCut, topRightCut, bottomRightCut, bottomLeftCut)`. All clamp logic stays centralised (each cut clamped to at most half the smaller side). `CybergramBubbleDrawable` (standalone) continues to run through the same builder.

### Near-corner mapping (near-aware directional cuts)

- `generateCybergramPath` now emits four different cuts:
  - outgoing: left corners always `BUBBLE_CORNER_CUT_DP` (6dp); `topRight = topNear ? BUBBLE_NEAR_CORNER_CUT_DP (2dp) : 6dp`, `bottomRight = bottomNear ? 2dp : 6dp`.
  - incoming: right corners always 6dp; `topLeft = topNear ? 2dp : 6dp`, `bottomLeft = bottomNear ? 2dp : 6dp`.
  - TYPE_MEDIA uses the same incoming/outgoing directional semantics.
  - With no near flag set, all four corners are 6dp — the standalone v3 silhouette is unchanged. The cut is never 0, so a 2dp chamfer keeps the angular language inside a group.

### Border render seam

- New `MessageDrawable`-owned separate overlay stroke (`borderPaint`: Style.STROKE, Join.MITER, Cap.SQUARE, width = `dp(BUBBLE_BORDER_WIDTH_DP)` = 1dp), drawn ALWAYS on the exact `generateCybergramPath` path (no second geometry implementation, no separate RectF approximation).
- Hooked in both paths of `draw(Canvas, Paint)`:
  - fast solid path (`getBackgroundDrawable()` -> `background.draw(canvas)`): border drawn after the background, before `return`;
  - direct/gradient path: fill -> selected overlay -> border last.
- Only when `useAngularGeometry()` and `TYPE_TEXT`/`TYPE_MEDIA`. Only on the FINAL user canvas, i.e. `paintToUse == null` — it is never baked into the cached nine-patch/shadow bitmap. `borderPaint` alpha scales with the drawable alpha; colorFilter/crossfade untouched.

### Border theme keys (via the current ResourcesProvider; no hardcoded colours)

- incoming (normal/selected): `Theme.key_chat_inReplyLine` (cybergram `#10141A`, thin dark outline on the amber panel).
- outgoing (normal): `Theme.key_chat_outReplyLine` (cybergram `#00E5FF` cyan).
- outgoing (selected): `Theme.key_chat_outReplyLine2` (cybergram `#33D6FF` secondary cyan), falling back to `chat_outReplyLine`.
- `getColor(key)` routes through the same provider (showcase Palette / production provider), so the border works in both. `cybergram.attheme` defines all three keys.

### Verified (SM-A256E / Android 16 / arm64-v8a)

- Build: `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a` BUILD SUCCESSFUL; APK SHA-256 `43ae8f4c6e3aee1cdcf0ebb4f678156c572fb97b7fb2a5da5d635041eaf57a81`.
- Install: over `org.telegram.messenger.beta` via `adb install -r` (streamed), Success; no `pm clear`.
- Pre-launch read-only: `mainconfig.xml` `theme=Day`, `nighttheme=Day`.
- Launch showcase: explicit adb intent -> `topResumedActivity=...CybergramShowcaseActivity`, PID 28283, no FATAL/ANR.
- Screenshot: `.local-artifacts/cybergram_showcase_v4_border_grouped.png` (1080x2340, git-ignored).
- Banner verbatim: `Showcase: Cybergram | dark=true` / `Active app theme: Day` / `BG #080A0F | IN #E8D93A | OUT #0A1A21`.
- Border: incoming amber has a thin dark outline; outgoing dark has a thin cyan outline; 1dp, follows the chamfers (machine: cyan ~2px vertical lines at an outgoing bubble's left and right edges).
- Grouped: standalone/multiline far corners measured ~15px (6dp); grouped near corners (top bubble bottom-left, bottom bubble top-left for incoming) measured ~4-5px (2dp). No tails. Selected bubble renders lighter teal with a cyan border; media bordered + angular; no clipping.
- v3 -> v4 comparison (read-only vision): only the outline + grouped near-cuts are new; standalone silhouette, colours, layout and banner unchanged. The cyan outline is thin and does not turn the bubble into a neon frame; the 2dp near chamfer is subtle and reads as a joined group.
- Regression: normal beta client at `theme=Day` launched (no FATAL/ANR) — no border/angular leak (activation gate false). Prefs still `theme=Day`, `nighttheme=Day`.

Note (pre-existing, out of scope) carries over: the debug composer placeholder "Сообщение" sits low; not a regression of these passes.

### Deferred from this pass

- `TYPE_PREVIEW` angular support.
- The border re-strokes each frame by design (never baked into the cached nine-patch); revisit only if profiling shows a cost.

## Chat header + composer structural decoration (Stage D, pass 1, 2026-09-09)

First decoration pass over the chat header and composer. Presentation only: no rewrite of `ChatActivityEnterView`, no input-mode replacement, no size/touch-target changes. Cybergram presentation is added on top of the existing Telegram mechanics through narrow, non-interactive, gated seams.

### Generic Cybergram presentation gate

- `CybergramTheme.isCybergramPresentation(Theme.ResourcesProvider provider)` — true iff `provider instanceof GeometryProvider` OR the active `Theme.getCurrentTheme()` is built-in Cybergram; false otherwise; never inferred from colours.
- `CybergramTheme.useAngularMessageGeometry(provider)` is now a thin delegation to it, so message geometry and HUD decoration share a single Cybergram detection path. The showcase `Palette` still enables via the marker.

### Reusable HUD primitive

- New `CybergramHudDrawable` (ActionBar): a pure `Drawable` that draws a rectangular panel with 45° chamfered corners using the SAME `CybergramBubbleDrawable.buildPath` (per-corner cuts supported; fill and stroke independently optional; stroke MITER/SQUARE; alpha). No touch/event handling, no assets. Used for the composer frame and (via `buildPath`) the header ticks — the polygon is never duplicated.

### Composer seam (`ChatActivityEnterView`)

- Background-owner audit: the composer panel background is a full `drawRect` (compose paint) at the EnterView level; `messageEditText` has `setBackgroundDrawable(null)`; `messageEditTextContainer` (the pill) has no background. So decorating the field means a chamfered cyan frame over `messageEditTextContainer` — no rounded-pill conflict.
- Integrated in the `messageEditTextContainer` anonymous FrameLayout: a `dispatchDraw` override that post-draws a `CybergramHudDrawable` frame (1dp cyan stroke via `key_chat_messagePanelSend`, 6dp cut) around the container's bounds, only when `isCybergramPresentation(resourcesProvider)`.
- It follows the container automatically (multiline expansion, edit message, reply panel, attach/emoji/bot keyboard changes) because it draws in the container's local space; when the container is hidden/GONE the decoration disappears with it. Not styled this pass (recording UI / voice lock / slow mode / bot menu / stickers) — the frame does not break or cover them.

### Header seam (`ChatActivity`)

- `ActionBar extends FrameLayout` but overrides `onLayout` to position only its known children, so a foreign child is not laid out — the decoration is instead a dedicated non-interactive overlay added as a sibling over the action bar.
- New `CybergramHeaderDecorationView` (ui): added to `contentView` right before `return fragmentView` (top z-order), gated by `isCybergramPresentation(getResourceProvider())`, so non-Cybergram chats add nothing. It draws at the action bar's window position (follows the header/hierarchy): a 1dp cyan bottom rule (colour from `key_actionBarDefaultIcon`), a short ~40dp amber accent segment near the identity/title zone (`CybergramTheme.AMBER`), and two very small chamfered corner ticks via the shared `buildPath`. No microtext, no fake SECURE/ID badges; non-clickable/non-focusable.

### Send button

- No change to `SendButton` geometry / touch / animation this pass. The cybergram palette already makes the send cyan (`key_chat_messagePanelSend`). A dedicated angular send control is deferred to Stage D pass 2.

### Showcase

- Header/composer now use the same HUD primitive (composer frame via `CybergramHudDrawable`; header rule/segment/ticks via `CybergramBubbleDrawable.buildPath`), not standalone geometry. v4 bubble samples preserved; banner unchanged.
- Screenshot: `.local-artifacts/cybergram_showcase_v5_header_composer.png` (1080x2340, git-ignored).

### Verified

- Build: `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a` BUILD SUCCESSFUL (final APK SHA-256 `81235d588f6216551bf452db6ceaff0ad56e9ffb398eb02e6d9de90b81400a25`). The production ChatActivity / ChatActivityEnterView seams compile (compile/static safety verified); full real-chat runtime validation pending an authenticated beta session.
- Install over `org.telegram.messenger.beta` (`adb install -r`, streamed), Success; no `pm clear`. Pre-launch prefs `theme=Day`, `nighttheme=Day`.
- Launch showcase: explicit intent -> `topResumedActivity=...CybergramShowcaseActivity`, no FATAL/ANR. Banner verbatim: `Showcase: Cybergram | dark=true` / `Active app theme: Day` / `BG #080A0F | IN #E8D93A | OUT #0A1A21`.
- Visual (showcase, runtime): header shows the thin cyan bottom rule + short amber segment + two subtle ticks (technical, not cluttered; pixel-confirmed cyan line y143-145/967px, amber x146-258); composer shows a 1px cyan chamfered frame over the field, no rounded pill (pixel-confirmed edges x~23..910 with chamfered top corners, clear of the send control); bubbles unchanged (angular, chamfered, no tails); no clipping; readable, not overloaded. NOTE: a colour-application bug in `CybergramHudDrawable` (its setters stored the colour but did not apply it to the Paint) was found and fixed during this pass.
- Regression: normal beta client at `theme=Day` launched (no FATAL/ANR); the Cybergram header/composer gate is false, so nothing is added/drawn at Day; prefs still `theme=Day`, `nighttheme=Day`.

### Deferred / honest limitation

- Production ChatActivity header + ChatActivityEnterView composer seams are compile/static-verified only; real-chat runtime validation requires an authenticated beta session (no auth this build; no login/session touched).
- Send button angular control: Stage D pass 2.

## Stabilization pass (Stage D pass 1 follow-up, 2026-09-09)

Two lifecycle/reusable-component fixes; no new artwork.

### CybergramHudDrawable alpha contract (order-independent)

- Root cause: `setAlpha()` applied the alpha to the Paints, but the subsequent `setFillColor`/`setStrokeColor`/`setFill`/`setStroke` called `Paint.setColor(color)`, restoring the alpha from the ARGB colour and clobbering the drawable alpha.
- Fix: the drawable stores the BASE colours (full ARGB) and a drawable `alpha` (clamped 0..255); a centralised `updatePaintColors()` recomputes each Paint colour as `effectiveAlpha = Color.alpha(baseColor) * drawableAlpha / 255` over the base RGB. Every colour setter and `setAlpha()` routes through it, so the outcome is identical regardless of call order: `setAlpha(96); setStrokeColor(0xff00e5ff)` == `setStrokeColor(0xff00e5ff); setAlpha(96)`.
- Regression evidence (debug showcase, temporary, removed afterwards): two side-by-side HUD samples with the alpha/stroke order swapped rendered pixel-identical (0/2601 differing pixels). Proof screenshot `.local-artifacts/cybergram_showcase_v5_1_proof.png`; the final screenshot has no sample.

### ChatActivity header lifecycle (live theme switch)

- Root cause: the overlay was added only inside `if (isCybergramPresentation(...))`, so on an existing `ChatActivity` an in-place Day -> Cybergram switch had no overlay (it was never instantiated).
- Fix: `CybergramHeaderDecorationView` is now ALWAYS added to `contentView` as a non-interactive overlay; its `onDraw()` already carries the runtime gate, so at Day it draws nothing and at Cybergram it draws. A Day <-> Cybergram switch reflects without recreating the activity. The composer frame was already dynamic via the `dispatchDraw` gate — left unchanged.
- Non-Cybergram impact (inert overlay): `clickable=false`, `focusable=false`, `importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO`, and `onTouchEvent` returns false (never consumes events), so touch/layout/accessibility are unaffected. The class comment was cleaned to "production non-interactive presentation overlay" (misleading DEBUG wording removed; an unused `CybergramTheme` import was dropped).

### Verified

- Build: `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a` BUILD SUCCESSFUL; final APK SHA-256 `b12d7df510817b6d7baf8e1873f3df96a7389f5358e7d33d6a48dcd88208586f`.
- Install over `org.telegram.messenger.beta` (streamed), Success; no `pm clear`. Pre-launch prefs `theme=Day`, `nighttheme=Day`.
- Showcase launch: explicit intent -> `topResumedActivity=...CybergramShowcaseActivity`, no FATAL/ANR.
- Screenshot: `.local-artifacts/cybergram_showcase_v5_1_stabilized.png` (1080x2340, git-ignored) — visually identical to v5 (banner/header/composer/bubbles unchanged; only 0.29% incidental AA jitter between two distinct captures).
- Alpha regression proof: `.local-artifacts/cybergram_showcase_v5_1_proof.png` — order-swapped HUD samples pixel-identical.
- Day regression: normal beta client launched (no FATAL/ANR); the always-present overlay is inert at Day (gate=false, draws nothing); prefs still `theme=Day`, `nighttheme=Day`.

### Honest limitation (unchanged)

- Production ChatActivity header + ChatActivityEnterView composer seams are compile/static-verified; full real-chat runtime validation still requires an authenticated beta session (no auth; no login/session touched).

## Telegram API credentials injection (local, gitignored, 2026-09-09)

Root cause of the auth blocker: the tracked `BuildVars.java` carried upstream sample credentials (`APP_ID = 4` + a sample `APP_HASH`). The Telegram server rejects them with `API_ID_PUBLISHED_FLOOD`, so login was impossible. The developer user has their own `api_id`/`api_hash` (kept secret / local-only).

### Scheme

- Credentials are read from the gitignored root `local.properties` via `getProps("CYBERGRAM_API_ID")` / `getProps("CYBERGRAM_API_HASH")` in `TMessagesProj/build.gradle` (the library that owns `BuildVars.java`).
- The library `defaultConfig` injects them into `BuildConfig` (`buildConfigField "int" APP_ID`, `buildConfigField "String" APP_HASH`).
- `BuildVars.APP_ID` / `APP_HASH` now come from `BuildConfig.APP_ID` / `BuildConfig.APP_HASH` - no literal user values in tracked source.
- Dev vs other variants:
  - **Cybergram developer build** (`CYBERGRAM_ABI` set, e.g. `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a`): credentials REQUIRED. If missing, configuration fails with `Cybergram Telegram API credentials are missing; configure CYBERGRAM_API_ID and CYBERGRAM_API_HASH in local.properties.` - no silent `APP_ID=4`.
  - **Other upstream/release variants** (no `CYBERGRAM_ABI`): keep the upstream sample fallback (`APP_ID=4`, sample hash) so they stay buildable.

### local.properties contract (gitignored, .gitignore line 6)

```
sdk.dir=...
CYBERGRAM_API_ID=<integer>
CYBERGRAM_API_HASH=<string>
```

The user adds `CYBERGRAM_API_ID` / `CYBERGRAM_API_HASH` themselves; no credentials are committed, staged, or printed (diagnostics report only configured=yes / api_id / hash length, never the hash value).

### Verification

- `:TMessagesProj:help` (no `CYBERGRAM_ABI`) -> BUILD SUCCESSFUL (sample fallback configures; BuildVars compiles against `BuildConfig.APP_ID` / `APP_HASH`).
- `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a` with no credentials -> fails at configuration with the exact message above (fail-fast instead of silent `APP_ID=4`).
- `local.properties` is gitignored and untracked; no real credentials appear in the diff; no API_HASH is ever printed.

### Credentials added / verified (2026-09-09)

- The user added `CYBERGRAM_API_ID` (8-digit numeric) and `CYBERGRAM_API_HASH` (32 chars) to `local.properties` (values not recorded here; gitignored).
- `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a` BUILD SUCCESSFUL. Generated `BuildConfig.APP_ID` = a real 8-digit numeric api_id (no longer the sample `4`); `APP_HASH` is a 32-char string present but its exact value is never recorded/tracked.
- Installed over `org.telegram.messenger.beta` (streamed), no `pm clear`; official Telegram untouched.
- Launched the app: launches to the Telegram intro/login screen (onboarding slide 1, «Начать общение»), no FATAL/ANR, no `API_ID_PUBLISHED_FLOOD` in logcat. The sample-based flood is resolved at source (the real api_id is compiled in).
- The actual `auth.sendCode` step (entering the phone number) is performed manually by the user; the number was NOT entered here and OTP was not read.

### Honest limitation (carries over)

- Production ChatActivity header + ChatActivityEnterView composer seams are now RUNTIME-validated in an authenticated real-chat session (see below). Stage D pass 2 (angular send control) and dialogs-list styling are still pending.

## Authenticated real-chat runtime validation (2026-09-09, safe surface = «Избранное»/Saved Messages)

The user completed login manually; validation ran against the real ChatActivity (beta, real api_id). No messages sent; no auth/session/OTP/prefs touched; no pm clear; official Telegram untouched.

- Day baseline (`real_chat_day_v1.png`): plain Telegram header + normal rounded composer + rounded outgoing bubble; ZERO Cybergram decor leak (no border/header/composer decor); chrome (back/title/menu/composer) working; 0 FATAL/ANR.
- Cybergram activation: theme switched via the theme UI; a fresh Saved Messages chat shows the full Cybergram presentation. Dark #080A0F background; header cyan bottom rule (pixel: single full-width line, no status-bar shift) + ~40dp amber segment (right of the back button, near the title) + 2 subtle HUD ticks at the bottom corners (small marks below the back/menu glyphs, non-intrusive); composer cyan chamfered frame around the field+emoji+attach (frame right edge left of the mic button), tap->focus->keyboard opens, emoji panel open/close works; outgoing bubbles dark w/ cyan border, 45° chamfered, no tail; timestamps + checks present; no clipping; 0 FATAL/ANR.
- Showcase-vs-production: outgoing bubble silhouette/border and the composer frame match the showcase; real ChatMessageCell padding/metadata correct. Incoming content in this chat is rendered as rectangular content CARDS (code/audio/file/link) — Telegram's standard content-card background overlays the bubble chamfer, so the incoming chamfered *text*-bubble silhouette is not visible for those; EXPECTED difference vs the showcase's plain amber text bubbles (no plain incoming text bubble present in this chat -> incoming text-bubble chamfer is DEFERRED to a chat that has one, without opening arbitrary personal chats).
- Theme-switch semantics: both states verified — Day active => no decor (Day baseline); Cybergram active => decor present. Telegram recreates the activity on theme change (standard behaviour), so the switch was observed across freshly rendered chats rather than forced in-place on a single instance.
- No BLOCKER. Minor observation only: the header HUD ticks land at the bottom corners, under the back/menu buttons (small marks below the glyphs, subtle, non-intrusive) — left as-is since the slice works and redesign is out of scope this pass.
- No production code changed this pass; docs/WORK_STATE.md only.

## Stage D pass 2: Cybergram Angular Send Control (2026-09-09)

### Send-state ownership / state machine (audit)
- The compose send control lives in `ChatActivityEnterView`: a `sendButtonContainer` FrameLayout hosts stacked controls — `audioVideoButtonContainer` (mic/audio/video), `cancelBotButton`, `sendButton` (a `ChatActivityEnterView.SendButton` custom view), `sendButtonBlockedByTypingView`, `slowModeButton`, `expandStickersButton`. They toggle by visibility/alpha/scale.
- `SendButton` (static inner class) draws a `backgroundRect` (38dp rounded-rect, RADIUS 19) with `getFillColor()` = `key_chat_messagePanelSend` (cyan) behind the send glyph; the glyph (new-design) is white. It exposes `isInScheduleMode()`, `isOpen()`, `isInactive()`, `shouldDrawBackground()`, `setAlpha()`.
- `resId` is set once (send_plane_24 / input_schedule); mic/audio/video are SEPARATE controls, and the send button animates in/out (alpha 0→1, scale 0.1→1) via the enter-view state machine.

### Integration seam
- Cybergram treatment is a decoration, NOT a new button: inside `SendButton.onDraw`, when `isCybergramSendPlateEnabled()`, draw a `CybergramHudDrawable` chamfered dark plate (fill = `key_chat_messagePanelBackground` #0B0D12, stroke = `key_chat_messagePanelSend` #00E5FF, cut = `BUBBLE_CORNER_CUT_DP` 6dp, bounds = `backgroundRect`) over the rounded-rect; the glyph draws on top. Otherwise (Day/non-Cybergram/non-send) the original `drawRoundRect` renders.
- Gate `isCybergramSendPlateEnabled()`: base returns false; only the composer's anonymous SendButton overrides to `CybergramTheme.isCybergramPresentation(resourcesProvider) && editingMessageObject == null && !recordingAudioVideo` — uses the real Telegram state, not text.length. Reuses the shared `CybergramBubbleDrawable.buildPath` via CybergramHudDrawable (no second polygon).

### Normal-send gate + animation
- Plate appears only under Cybergram and only when the normal text-send state is shown. Drawn inside the SendButton's own `onDraw`, it inherits the send button's alpha/scale/translation/visibility — no separate animation, no double scale, no "icon gone but frame left" artifact.
- Empty/mic, video/voice, recording, edit/done, slow-mode: gate false or send button not shown → plate does not hang under other controls.

### Runtime send-state result (real chat, authenticated Saved Messages)
- Built + installed over org.telegram.messenger.beta (no pm clear, no logout). No FATAL/ANR.
- Human checkpoint #1: user typed a single "." (unsent). Verified: 108x108 chamfered dark plate (45° top-left/bottom-left cuts), ~1dp cyan stroke, light paper-plane glyph centered on top, right edge 29px from screen edge, ~3dp gap to the composer frame, no rounded/circular leftover. Screenshot `.local-artifacts/real_chat_cybergram_send_v1.png`.
- Human checkpoint #2: user deleted the ".". Verified: send button + plate disappeared, mic/empty state returned, no ghost border/background, composer frame intact.
- Transitions (task 12): keyboard open (composer + send plate moved up together), emoji panel open/close (plate stays aligned above the panel) — the plate follows the send state.
- Deferred (intentionally not tested per task): voice/video recording, camera, slow-mode, edit/done, actual message send.

### Showcase v6
- `cybergram_showcase_v6_send.png`: v4 bubbles + v5 header + v5 composer frame + banner preserved; added send control (dark chamfered plate + 1dp cyan stroke + paper-plane glyph) via the same CybergramHudDrawable primitive.

### Day / non-Cybergram regression
- At Day `isCybergramPresentation()` is false → `isCybergramSendPlateEnabled()` false → the SendButton draws the original rounded cyan `drawRoundRect` (no plate). Verified by the static gate; a real Day send screenshot is optional (would need a theme switch).

### Showcase vs production (read-only)
- Consistent: dark chamfered plate, 1dp cyan stroke, paper-plane glyph, no round; composer frame + plate visually coherent. Only a minor scale difference (showcase ~34dp mock plate vs production ~38dp real button) — EXPECTED. No blockers, no visual defects.

## Stage E pass 1: Dialogs List / Chat Row Presentation (2026-09-09)

### Dialogs ownership map (audit)
- `DialogsActivity`: `viewPage.listView = new DialogsRecyclerView(...)` (RecyclerView); list background = `Theme.key_windowBackgroundGray` (dark in Cybergram at theme); `actionBar` bg = `key_windowBackgroundWhite` (#080A0F dark); bottom-nav + FAB = standard Telegram new UI (round/Material, left as-is this pass). Filter/folder tabs + search exposed to accessibility; chat rows are NOT (custom RecyclerView) — navigation via more-menu / vision for "Избранное".
- `DialogCell` (BaseCell): row `onDraw` draws content + the swipe/archive reveal + the `isSelected` rounded blob (`Theme.dialogs_tabletSeletedPaint`, `dp(8)` corners). Normal rows are transparent over the list bg (no per-row card). Unread badge = `drawCounter` (`key_topics_unreadCounter`, `key_chats_unreadCounter`); muted = `key_chats_unreadCounterMuted`; pinned overlay = `key_chats_pinnedOverlay`. Row pressed/selector handled by the RecyclerListView (list-level). `resourcesProvider` present; `dpf2`/`dp` static-imported; `Path` imported.

### Exact production seams
- `DialogCell.drawCybergramRowTreatment(Canvas)` — called at the top of `onDraw` (line ~3789) only when `CybergramTheme.isCybergramPresentation(resourcesProvider)`. Draws in the row's base bounds: (a) a thin (~1px) sub-1dp cyan-dark separator along the bottom edge (cyan at alpha 66 — subtle, not a neon line); (b) a short cyan technical accent tab (2px wide x 16px tall) at the LEFT edge (alpha 150) + a small 45° HUD tick (6px diagonal) at its top (alpha 110). The row itself stays transparent (no rounded card, no full-row cyan frame) so density is preserved.
- `DialogCell` `isSelected` branch (line ~4002): under Cybergram, the Telegram rounded blob is replaced by a `CybergramHudDrawable` restrained raised dark panel (fill = `key_chats_pinnedOverlay`, stroke = `key_chat_messagePanelSend` 1dp, cut 6dp). Day/non-Cybergram keeps `drawRoundRect` (upstream).

### Private-content handling
- The real Chats screen contains personal content. Hermes only analyzed geometry/colors/chrome (never transcribing chat names / message text); the runtime screenshots are local `.local-artifacts/real_dialogs_cybergram_v1.png` only, not committed. Tap navigation used only the safe «Избранное».

### Runtime findings (real dialogs)
- List bg near-black; rows show the thin subtle cyan-dark separator at the bottom + small restrained cyan accent at the left edge (toned down after the artistic pass — the first capture read as "neon" and was reduced). Dense list, no per-row cards. Bottom row clipped by the bottom nav (normal overlap). FATAL/ANR = 0 across scroll, tap «Избранное»+back, header more-menu, bottom-nav (Contacts/Chats), no touch regression. Unread + muted/pinned indicators present (existing cyan/gray palette).

### Showcase v7 vs real (read-only)
- `cybergram_showcase_v7_dialogs.png`: 4 mock rows (normal / unread / muted-pinned / selected) with the same primitives (separator + left-edge accent + 45° tick + CybergramHudDrawable selected panel) and the preserved v5 header / composer frame / banner. Real matches: same separator + accent treatment, dense rhythm, dark bg. Rows still read as a dense Telegram list with a clear Cybergram HUD rhythm (not cards / not "Excel from the future").

### Day / non-Cybergram regression
- Statically verified (gate at `DialogCell.java:3789` + `:4002`): at Day `isCybergramPresentation()` is false → no row treatment → upstream DialogCell appearance; selected state uses the original rounded blob. A real Day runtime screenshot is optional (needs a theme switch — same manual action as before; avoided re-doing the complex chain).

### Deferred (Stage E pass 2 / not in scope this pass)
- Dialogs action-bar structural header decor (cyan line + amber identity + HUD ticks) — the action bar is already Cybergram-dark via theme; adding the structural language is a focused pass-2 item (different hierarchy from ChatActivity's header). Bottom-nav geometry + FAB (still round/Material) — pass 2. Amber attention tint for the unread counter — the existing at theme uses cyan (readable + coherent); switching to amber is an optional pass-2 semantic palette change. Settings/Contacts/Profile/media viewer — untouched.

## Stage E pass 2: Dialogs Chrome (action-bar decor + opt-in angular FAB + bottom-nav recon) (2026-09-09)

### Row consistency cleanup
- Production DialogCell + showcase now share CybergramTheme constants: `DIALOGS_ROW_SEPARATOR_ALPHA` (=66), `DIALOGS_ROW_ACCENT_ALPHA` (=150), `DIALOGS_ROW_TICK_ALPHA` (=110). The showcase v7 accent/tick mismatch (206/150) is fixed; the selected-panel cut now uses `CybergramTheme.BUBBLE_CORNER_CUT_DP` (was hardcoded dpf2(6)). No visual change to the pass-1 production result.
### Action-bar ownership + seam
- The dialogs action bar is a local `ActionBar` in `DialogsActivity.createView` (contentView is full-screen; the fragment root overlays it). Reused the existing `CybergramHeaderDecorationView` (non-interactive: clickable=false, focusable=false, importantForAccessibility=NO, onTouchEvent=false) as the shared header primitive — no mechanical copy, no second polygon implementation.
- Added to `DialogsActivity` contentView (MATCH_PARENT overlay, top z-order, non-blocking) passing the local `actionBar`; it measures the real action-bar bounds via `getLocationInWindow` (no magic constants; follows stories/filter-tab heights).
- Gated on `CybergramTheme.isCybergramPresentation(resourcesProvider)`; at Day the overlay draws nothing.

### Header state visibility policy
- Added `DECOR_FULL / DECOR_RULE_ONLY / DECOR_NONE` + a `DecorStateProvider`. `DialogsActivity` provider: action-mode (multiselect) -> `DECOR_RULE_ONLY`; search active (`searchViewPager.getAlpha()>0.5`) -> `DECOR_NONE`; else `DECOR_FULL` (rule + amber segment + 2 restrained ticks). ChatActivity keeps default `DECOR_FULL`.
- Verified: normal dialogs show the cyan bottom rule + amber segment + corner ticks at the real action-bar bottom (no status-bar offset, no overlap with title/buttons/icons).

### FAB opt-in (clean)
- `FragmentFloatingButton` (shared) got `setCybergramPresentationEnabled(boolean)` (default false). Only the non-sub updateColors branch swaps the background: opt-in && Cybergram -> `CybergramHudDrawable` dark chamfered plate (fill `key_windowBackgroundWhite`, stroke `key_chat_messagePanelSend` 1dp, cut `BUBBLE_CORNER_CUT_DP`) at the same 48dp View/touch target; icon + visibility/scale/progress animations untouched. Else upstream circle selector. `DialogsActivity` enables it only on `floatingButton3`. Non-Cybergram ignores it (upstream circle). Pressed feedback preserved via the existing view-level scale animator (documented). `floatingButtonStories` NOT styled in pass 2.

### Bottom-nav actual owner (recon)
- `MainTabsActivityController` is only a `setTabsVisible(boolean)` visibility controller — NOT the owner. Actual owner: `MainTabsActivity` -> `MainTabsLayout` (tabsView) hosting `GlassTabView[]`(5) + a `tabsViewBackground` (BlurredBackgroundDrawable); selected item, icons/text and press/selection animations live in `GlassTabView`/`MainTabsLayout`. Complex shared nav component; a Cybergram restyle is a separate risky redesign. No production bottom-nav code changed. Documented -> Stage E pass 3.

### Runtime findings
- Real Chats screen (authenticated): header cyan rule + amber segment + ticks at the action-bar bottom; FAB renders as an angular chamfered dark plate with cyan stroke; rows keep pass-1 separator + left accent. Search open/close, more menu, «Избранное» open+back, scroll — all work, no touch regression, no clipping, FATAL/ANR=0. Local (gitignored) screenshots: `cybergram_showcase_v8_dialogs_chrome.png`, `real_dialogs_cybergram_v2_chrome.png` (private content not transcribed, not committed).

### Day regression
- Static: header onDraw gate, FAB opt-in branch, row gate are all false at Day -> upstream header/circular FAB/upstream rows. Real Day screenshot optional (theme switch = human).

### Defects / deferred
- No BLOCKER. One search-state nuance documented (decor stacked above the search field, no overlap). Deferred: bottom-nav restyle (pass 3), floatingButtonStories styling, amber attention tint for the unread counter.

## Chrome Normalization audit + Stage C incoming-bubble palette correction (2026-09-09)

### Screenshot-derived visual defects (real-device)
- Chat header: identity block + call/menu controls are large light-gray OPAQUE rounded Material capsules/pills (sample px ~195,195,197) sitting under the Cybergram cyan rule/ticks.
- Dialogs top: filter/folder tabs remain a large rounded pill.
- Bottom nav: large rounded/glass pill; selected tab rounded capsule.
- FAB = good (angular, keep).

### Chrome ownership + seams (audit; fixes deferred — complex shared seams)
- Chat header pills: driven by the deep `ActionBar.setGlassMode` glass/blur pipeline
  (`BlurredBackgroundDrawableViewFactory`, `glassBackgroundSourceRenderNode`, `ChatAvatarContainer.setGlassMode()`,
  capsule px 195,195,197 from the glass overlay, NOT an at-theme key). Narrow normalization would require gating/flattening
  the shared glass pipeline -> deferred (task allows documenting complex seams; "не ломать blur pipeline").
- Dialogs filter tabs: `FilterTabsView` rounded pill -> audit only; not changed this pass.
- Bottom nav: owner = `MainTabsActivity` -> `MainTabsLayout` (tabsView) + `GlassTabView[5]` + `tabsViewBackground`
  (BlurredBackgroundDrawable); shared complex nav component -> Stage E pass 3 (not changed here).
- CybergramHeaderDecorationView rule/amber/ticks kept (verified still coherent).

### Stage C incoming-bubble palette correction (APPLIED + validated)
- Rationale: real-device screenshot -> the opaque amber-yellow incoming fill (`chat_inBubble` #E8D93A) dominated the
  composition and did not fit the dark HUD language. Opaque amber incoming fill REJECTED after real-device review.
- Change (cybergram.attheme only, no MessageDrawable geometry change):
  - chat_inBubble -> 0xFF13140E (dark warm graphite body); chat_inBubbleSelected -> 0xFF1C1D17 (lighter warm-dark raised).
  - chat_messageTextIn -> 0xFFE9EAE5 (light text; was dark -> unreadable on dark body).
  - chat_inTimeText/Selected -> muted warm-light; chat_inForwardedNameText -> amber; chat_inReplyLine -> amber
    (drives the incoming 1dp border via `MessageDrawable.getCybergramBorderColor` + the reply quote line);
    chat_inReplyNameText -> amber; chat_inReplyMessageText/ReplyMedia -> light; chat_inMenu/Selected -> warm-dark;
    chat_inPreviewLine -> muted.
- Outgoing unchanged (dark cyan `chat_outBubble` + cyan `chat_outReplyLine` border).
- APK verified to embed the new at-theme values; installed over beta (no pm clear/logout).
- Real incoming plain-text bubble (safe chat, human-opened): left/incoming = dark warm body + 1dp amber outline + light
  readable text; right/outgoing = dark cyan + cyan stroke; no yellow bubbles; FATAL/ANR=0.
- Showcase v9 reflects the new palette (showcase reads at-theme via palette.color; incoming fill/border/text auto-update).
- Before/after: `real_header_before` (chrome), `cybergram_showcase_v9_incoming.png`, `real_incoming_v1.png` (gitignored).

### Day regression
- Incoming palette is a at-theme override (Cybergram at theme only); Day/stock at-theme unaffected.
- Chrome fixes NOT applied -> Day chrome unchanged (upstream rounded/glass preserved) by construction.

## Stage D pass 3: Cybergram flat chat header (glass capsule suppression) (2026-09-10)

Resolves defect 1 of the Chrome Normalization audit above ("chat header glass capsules not suppressed"; `e70c790` only darkened the tint). Presentation-only: one opt-in seam, no behaviour/state-machine change, no global `ActionBar` behaviour change for the rest of Telegram.

### Ownership audit (exact glass pipeline)

- The three header capsules are all owned by **`ActionBar`**: `glassDrawable` (identity -> call/menu capsule), `glassDrawableBack` (back capsule), `glassDrawableMenu` (menu capsule) -- `BlurredBackgroundDrawable`s built by `BlurredBackgroundDrawableViewFactory` and drawn in `ActionBar.dispatchDraw()` (bounds computed there, radius `dp(23)`, padding `dp(6)`). `ActionBarMenu`/`ActionMode` only carry a `glassMode` flag that tightens item margins (`-dp(5)`).
- `ChatAvatarContainer.setGlassMode()` owns **no** surface at all (verified: no blur/glass drawable in that file). It is pure text/layout metrics: title `17.5dp` / subtitle `13.5dp`, title left `dp(49.66)` vs `dp(55)`, subtitle top `dp(23.66)` vs `dp(24)`, title top `dp(1.66)` vs `dp(11)` without subtitle.
- Lifecycle: `ChatActivity.createView` calls `avatarContainer.setGlassMode()` (~L4170) and `actionBar.setupGlass(factory, topPanelChatActivity, isForum)` (~L4576). `setupGlass` is also called by `ChannelAdminLogActivity`, `CommunityCreateActivity`, `CommunityEditActivity`, `ChatAttachAlert`; `DialogsActivity` never calls it.
- Non-surface side effects of `setupGlass` (must be preserved): `setClipChildren(false)`; `menu.setTranslationX(-dp(10))` + `menu.setGlassMode(true)`; `actionMode.setTranslationX(-dp(10))` + `actionMode.setGlassMode(true)`; `backButtonImageView.setTranslationX(dp(2))`; `glassMode = true` (drives ActionBar title sizing, `textLeft`, the capsule hit-test in `dispatchTouchEvent`, and the status-bar-colour branch); and in `dispatchDraw` the capsule-derived `chatAvatarContainer` centering translation, which only applies to pinned / welcome-messages / comments modes (in a normal chat `ActionBar.chatAvatarContainer == null`, so identity position is independent of glass).
- Root cause of the translucency: `setupGlass` also calls `setBackground(null)`, so the chat header has **no opaque background** and chat content shows through it (`BaseFragment.createActionBar` had assigned `key_actionBarDefault`). Restoring that colour is `#0B0D12`, identical to `chat_topPanelBackground` (pinned bar) and to `CybergramTheme.PANEL`.

### Implementation seam

- `ActionBar.setCybergramFlatHeader(boolean)` -- new opt-in, **default false**. When enabled, `setupGlass` keeps every layout side effect above but skips creating the three capsules and restores `setBackgroundColor(getThemedColor(Theme.key_actionBarDefault))`. Upstream/Day callers execute the original branch unchanged (same `setBackground(null)` + same drawable creation).
- `ActionBar.dispatchTouchEvent` capsule hit-test is now additionally guarded by `glassDrawable != null` (a null-safety no-op upstream, where `glassDrawable` is non-null whenever `glassMode` is set); without it a capsule-less header would have rejected every touch in the pinned/welcome/comments modes.
- `ChatActivity`: one line before `setupGlass(...)`: `actionBar.setCybergramFlatHeader(CybergramTheme.isCybergramPresentation(getResourceProvider()))` (+ the import). No change to `ActionBar.setupGlass` callers outside ChatActivity.
- Deliberately **not** changed: `avatarContainer.setGlassMode()` is still called, so title/subtitle metrics and identity geometry are byte-identical to the previous build; `ActionBarMenu.setGlassMode` behaviour; the `CybergramHeaderDecorationView` rail/segment/ticks; the round avatar; all touch targets.

### Verified (build + real device, SM-A256E / Android 16 / arm64-v8a)

- Build: `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a` -> **BUILD SUCCESSFUL in 5m 2s** (JDK 17.0.20.1+1; Gradle 8.11.1). APK package `org.telegram.messenger.beta`, 83,583,818 bytes, SHA-256 `516839C711B37015CB39536EF96890186377E92D8D82E661D8F6745B499839D2`.
- Install: `adb install -r` (streamed) -> `Success`; `org.telegram.messenger.beta` lastUpdateTime `22:12:05`; **`org.telegram.messenger` untouched** (`2026-08-25 21:32:01`). No `pm clear`, no uninstall, no logout, no OTP/2FA/session action.
- Launch: `monkey` -> `topResumedActivity=org.telegram.messenger.beta/org.telegram.messenger.DefaultIcon`, same PID 27598 before/after capture; logcat main+crash scanned repeatedly: **no `FATAL EXCEPTION`, no `E AndroidRuntime`, no `ANR in`**.
- Header measurement (same real chat, human-opened; device pixels): before/after capsule-edge detection over `y=85..225` at columns x=40/300/700/980 -> **BEFORE: top edge at y~91-93 and bottom edge at y~204-221 at every column (delta up to 107); AFTER: zero transitions at all four columns**. Header background probes (old back capsule, capsule interior, bleed zone, status zone, screen-edge margin) are **all exactly `#0B0D12`** after the fix, versus varying `#090A0F..#1C1C14` (translucent content bleed) before. Distinct colours in the identity band (y=90..215, x=310..780): **1424 -> 254** (residual = glyph antialiasing).
- Decoration preserved: the red rail occupies exactly rows y=232-234 in both captures; cyan identity segment/ticks present (cyan pixel x-range 67..1012 in both); round avatar, title, status, call and overflow controls unchanged in place and colour; composer row profile pixel-identical (light-pixel counts 53/52, 28/28, 28/28, 29/29, 26/26 on the same rows).
- Regression: dialogs screen is on an untouched code path (`DialogsActivity` never calls `setupGlass`; the opt-in defaults false) and its Cybergram decoration rows are identical across pre/post-install captures. The dialogs captures themselves were **different UI states** (post-back header tone `#0F161E` vs launch `#080A0F`, ~50% difference in the filter-chip/search bands), so cross-capture dialogs pixel diffs are not build-attributable and were not used as evidence.
- Out of scope and untouched this pass: dialogs search pill, `FilterTabsView`, bottom navigation, FAB, message bubbles, composer, pinned bar, service/date cells, chat HUD/background, media renderer.

### Honest limitations

- Vision comparison of the before/after header crops was performed while the session was on the image-capable model; the numeric evidence above is the durable record.
- The header status line differs between the two captures ("в сети" -> "был(а) в 21:48") because presence is live content; it is not a layout change.
- In pinned / welcome-messages / comments modes the capsules also used to center `chatAvatarContainer`; with the capsules suppressed the identity block stays at its layout margin (left-aligned) there, which is consistent with the flat-header target but was not exercised on device this pass.

## Launcher icon integration (source logo -> simplified adaptive icon) (2026-09-09)

### Audit of icon scheme
- The active launcher icon = default `ic_launcher` (`mipmap-anydpi-v26/ic_launcher.xml`): background `@drawable/icon_background` (= layer-list of `icon_background_sa` gradient + `icon_background_clip`), foreground `@mipmap/icon_foreground` (shared, used by ic_launcher + icon_2/4), monochrome `@drawable/icon_plane`; round = `ic_launcher_round.xml` (bg `icon_background_round` + fg `icon_foreground_round`). Legacy `ic_launcher.png`/`ic_launcher_round.png` at mdpi..xxxhdpi (48..192px). Alternate launcher aliases (Vintage/Aqua/Nox, `activity-alias` `enabled="false"`) use `icon_*_launcher` with their own `icon_*_background` + the shared `icon_foreground`.

### Source evaluation + adaptation
- `design/branding/cybergram_logo_v1.png` is an intricate circular HUD emblem (glowing cyan ring, 4 amber compass triangles, dozens of segment dots + internal traces) — too detailed for a launcher icon; at 48px it would blur to noise.
- Made a SAFE simplified adaptation on its basis: bold angular Telegram-like paper-plane silhouette (cyan #00E5FF body + amber #E8D93A fold), preserving the cyberpunk palette; readable at 48/96/192px (verified via preview render + launcher screenshot). No copyright/proprietary assets (original simplified vector).

### Derived assets (tracked)
- Adaptive foregrounds (transparent, plane in safe zone): `icon_foreground.png`/`_round` (plane ~58% of canvas) + `icon_foreground_sa.png` (~50%) at mdpi..xxxhdpi.
- Legacy composites (dark #0B0D12 bg + plane): `ic_launcher.png` (square) + `ic_launcher_round.png` (circular) at mdpi..xxxhdpi.
- `drawable/icon_background.xml` + `icon_background_round.xml` → dark gradient only (removed the blue `icon_background_clip` cloud layer); `drawable/icon_background_sa.xml` → #12151C→#080A0F gradient; `drawable/icon_plane.xml` → new white monochrome plane (themed-icons).
- Source logo kept at `design/branding/cybergram_logo_v1.png` (tracked).

### Runtime
- BUILD SUCCESSFUL; `pm install -r` over beta. Launcher app-drawer shows "Telegram Beta" as dark near-black + cyan/amber angular plane (confirmed via screenshot `.local-artifacts/launcher_appdrawer.png` + `launcher_icon_zoom.png`); no old blue circle. No other UI touched. FATAL/ANR=0.

1. Make `Cybergram` selectable/automatically applied using the existing Telegram theme pipeline (done — built-in registration + fresh-install default).
2. Tune the `.attheme` palette from device screenshots.
3. Integrate angular geometry into `MessageDrawable` behind a narrow Cybergram-specific seam. (DONE — geometry-only pass; then border + grouped near-corners pass. Remaining follow-ups: `TYPE_PREVIEW` support, and validating replies/reactions/forwards/pressed states.)
4. Validate plain text, grouped messages, replies, reactions, forwards, media, selection and pressed states before extending the design to the composer and dialog list. (Stage D pass 1 started: header + composer structural decoration added via gated seams; composer/dialog-list full runtime validation pending an authenticated beta session; angular send control = Stage D pass 2.)

## Explicitly deferred

- package/application ID rename;
- production `api_id` / `api_hash` configuration;
- signing and Firebase credentials;
- app icon and final branding;
- proprietary game fonts/assets;
- broad package refactors;
- protocol/business-logic changes.

These remain deferred until a reproducible local debug build exists.
