# B5 — service/date plate ownership and implementation audit

Status: `AUDIT COMPLETE / ORDINARY-vs-RICH MAPPED / STRATEGY B RECOMMENDED (VISUAL-DENSITY GATE OPEN) / NO PRODUCTION CHANGE`

Type: evidence + implementation-design audit (`docs/passes/B5_SERVICE_DATE_AUDIT.md`).

Base: `dev` at `2db3b48e7f3425518278a909ff65594a5410962a`.

Audit branch: `audit/cybergram-service-date`.

Production rendering changes: **NONE**. The only non-documentation change is a DEBUG-source-set
fixture (see §8).

Design authority: `docs/CYBERGRAM_UI_SPEC.md` (line 63: "Date separators and service messages use
compact dark plates with amber/cyan accents rather than Telegram's translucent default service
bubbles"; §Message geometry, lines 67-80).

---

## 1. Repository preflight

- `git status --porcelain` was **empty** before branching; worktree clean.
- `HEAD = dev = origin/dev = 2db3b48e7f3425518278a909ff65594a5410962a`.
- `git fetch origin --prune` **failed** with `schannel: AcquireCredentialsHandle failed:
  SEC_E_NO_CREDENTIALS` — no usable Git credential in this environment. The local `origin/dev`
  tracking ref already resolved to the expected base `2db3b48e...`, so the audit proceeded against
  that local ref and recorded the fetch failure rather than claiming a fresh remote verification.
- Branch created from `dev`: `audit/cybergram-service-date` (no merge, no push).

---

## 2. Mandatory source reconnaissance

Commands run (from repository root):

```text
git grep -n "setCustomDate" -- 'TMessagesProj/src/main/java/**/*.java'
git grep -n "backgroundPath"  -- TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatActionCell.java
git grep -n "backgroundPath2" -- TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatActionCell.java
git grep -n "drawBackground"  -- TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatActionCell.java
git grep -n "arcTo|addRoundRect|drawRoundRect|corner" -- .../ChatActionCell.java
git grep -n "isButtonLayout|isNewStyleButtonLayout|isMessageActionSuggestedPostApproval" -- .../ChatActionCell.java
git grep -n "TYPE_ACTION_PHOTO|TYPE_GIFT_*|TYPE_ACTION_WALLPAPER|TYPE_STORY_MENTION|..." -- .../MessageObject.java
```

### 2.1 `setCustomDate` callers (`ChatActionCell` construction sites)

`setCustomDate` is defined at `ChatActionCell.java:550-571`. Production callers:

| caller | provider | surface |
|---|---|---|
| `ChatActivity.java:7055` (`floatingDateView`), `:16319` | `themeDelegate` | chat floating date |
| `ChatActivity.java:7108` | `themeDelegate` | chat "scroll to bottom" live time |
| `ChannelAdminLogActivity.java:2670` | `null` (ctor `new ChatActionCell(context)`) | admin log floating date |
| `SharedMediaLayout.java:3683/4881/4885` | `null` | shared-media floating date |
| `FilteredSearchView.java:415/441` | `null` | search floating date |
| `Components/Paint/Views/MessageEntityView.java:124` | commented out | — |

`setCustomDate` formats `LocaleController.formatDateChat(date)` (or the scheduled variant) and routes
it through `updateTextInternal()` → `createLayout()` → the **same** text/background pipeline as
ordinary service text. There is no separate date geometry.

### 2.2 Ordinary background pipeline (`backgroundPath`)

`drawBackground(Canvas, boolean)` — `ChatActionCell.java:3302-3588`.

- Provider/paints: `getThemedPaint(...)` at `3311-3313`; `getThemedColor(...)` at `3315/3322`;
  `themeDelegate` field `386`, set from the constructor at `505-509`; `overrideBackgroundPaint` path
  `3314-3326`; `getThemedPaint` `3980-3983`; `hasGradientService()` `3889-3891`.
- Path (re)build guarded by `invalidatePath`, `3327-3475`:
  - `backgroundLeft = getWidth(); backgroundRight = 0; lineWidths.clear();` `3329-3331`.
  - `corner = dp(11)`, `cornerIn = dp(8)` `3333-3334`.
  - per-line widths from `textLayout.getLineWidth(a)` with a forward and backward smoothing pass
    (merge a narrower line into the previous when `diff <= 1.5f * corner + cornerIn`) `3337-3356`.
  - `y = dp(4)`, `x = getMeasuredWidth() / 2`, `cornerOffset = dp(3)`,
    `cornerInSmall = dp(6)`, `cornerRest = corner - cornerOffset` `3358-3364`.
  - forward pass builds the right half with `arcTo(...)` at `3397, 3401, 3418, 3422`, using
    `textLayout.getLineBottom(a)` for heights `3370-3426`.
  - backward pass builds the left half with `arcTo(...)` at `3444, 3448, 3456, 3460` using
    `lineHeights` `3427-3462`.
  - `backgroundPath.close()` `3463`.
  - **special override inside the ordinary block**: `if (isMessageActionSuggestedPostApproval() &&
    !isNewStyleButtonLayout())` replaces the whole path with
    `addRoundRect(rect, dp(15), dp(15))` `3465-3474`.
  - horizontal envelope is tracked by `checkLeftRightBounds()` `3297-3300` (min/max of the arc
    rects), consumed by `getBoundsLeft()/getBoundsRight()` `3852-3887` and by touch hit-testing
    `1236, 1309`.
- Ownership of the draw: `drawBackground(canvas, false)` is called unconditionally from `onDraw`
  at `2798`.
- Paint pipeline (must be preserved by B6): `backgroundPaint` `3499-3500`; gradient darken only when
  `hasGradientService() && alpha > 0` `3501-3503`; dim only when `dimAmount > 0` `3504-3511`;
  `applyServiceShaderMatrix` `3480-3484`; `drawPath` uses `backgroundPath` for all three layers.

**Key fact:** the ordinary `backgroundPath` is drawn for *every* non-repost state, including rich
states. Rich states then add their own plates **on top**; they do not replace `backgroundPath`
(except the suggested-post-approval override above).

### 2.3 Rich/special paths that supplement or replace the ordinary plate

| geometry | location | notes |
|---|---|---|
| `starGiftLayout` plate | `3515-3528` (`drawRoundRect` `dp(16)`) | star gift / unique gift |
| `birthdayLayout` plate | `3529-3541` (`drawRoundRect` `dp(16)`) | `TYPE_SUGGEST_BIRTHDAY` |
| new-style card plate | `3542-3582` | `isButtonLayout`; `backgroundPath2.addRoundRect` with `dp(16)/dp(6)` radii for `TYPE_GIFT_OFFER`/`TYPE_SHARING_OFFER` + inline buttons `3561-3572`; otherwise `drawRoundRect(dp(20))` `3576-3580` |
| gift button | `3143-3151` (`drawRoundRect(dp(15))`) | `key_paint_chatActionBackgroundSelected` |
| stars clip | `3162-3171` (`addRoundRect(dp(16))`) | star-particle clip |
| ribbon | `3234-3275` (`giftRibbonPath` + `CornerPathEffect(dp(5))` `2697-2704`) | corner effect, not a rounded card |
| released badge | `2983-2984` (`drawRoundRect(dp(8))`) | |
| bot buttons | `3596-3660` (`botButtonPath.addRoundRect(...)` `3634`) | inline keyboard |
| reactions | `3278` → `drawReactions` / `ReactionsLayoutInBubble` `3798-3849` | pill geometry is B4 `DESIGN-OPEN` |
| topic separator | `4002-4013` | |

### 2.4 Ordinary-vs-rich predicates

- `isButtonLayout(MessageObject)` `4016-4018` = `TYPE_GIFT_STARS`(30) ∥ `TYPE_GIFT_PREMIUM`(18) ∥
  `TYPE_GIFT_PREMIUM_CHANNEL`(25) ∥ `isNewStyleButtonLayout()`.
- `isNewStyleButtonLayout()` `1993-2005` = `starGiftLayout.has()` ∥ `birthdayLayout != null` ∥
  `TYPE_GIFT_THEME_UPDATE`(31) ∥ `TYPE_COMMUNITY_CHANGED`(37) ∥ `TYPE_GIFT_OFFER`(33) ∥
  `TYPE_SHARING_OFFER`(35) ∥ `TYPE_GIFT_OFFER_REJECTED`(34) ∥ `TYPE_SUGGEST_PHOTO`(21) ∥
  `TYPE_ACTION_WALLPAPER`(22) ∥ `isStoryMention()`(24) ∥ suggested-post-approval with
  `balance_too_low`/`rejected`.
- `isMessageActionSuggestedPostApproval()` `1693-1697`.
- Text alignment: `createLayout` uses `Layout.Alignment.ALIGN_CENTER` for ordinary text and
  `ALIGN_NORMAL` only for suggested post approval `1738-1740`; `textY = dp(7)`,
  `textX = (width - textWidth) / 2` `1788-1789`; measured height is
  `topicSeparatorTopPadding + textHeight + additionalHeight + dp(14)` `1988`.
- `hasReplyMessage` `359/630` is only a cache-invalidation flag — `ChatActionCell` does **not**
  draw `ReplyMessageLine`; reply plates belong to `ChatMessageCell`. A service action therefore has
  no reply-plate geometry inside this cell.
- `setOverrideColor` (`SharedMediaLayout.java:3686`, media-time colours) switches only paints
  (`3314-3326`); the ordinary path is unchanged.

### 2.5 Sharing boundary (important for B6)

`ChatActionCell` is constructed by: `ChatActivity` (`7055`, `11295`, `32736`, `37035`, `37398`),
`ChannelAdminLogActivity` (`1492`, `3293`), `SharedMediaLayout` (`3683`, `9210`),
`Components/Paint/Views/MessageEntityView` (`763`), `Gifts/SendGiftSheet` (`193`),
`bots/BotShareSheet` (`218`), `Components/ChatAttachAlertPhotoLayoutPreview` (`930`),
`ThemePreviewActivity` (`5370`), `EditWidgetActivity` (`205`). Most pass a `themeDelegate`; the
floating-date/`new ChatActionCell(context)` sites pass `null` and therefore rely on
`Theme.getCurrentTheme()` in the central gate.

---

## 3. Case inventory

`ordinary` = visible main plate is the ordinary `backgroundPath`; `rich` = one or more separate
special plates replace/supplement it; `overlay` = ordinary plate plus a non-plate draw.

| # | required case | branch taken | classification | evidence | tier |
|---|---|---|---|---|---|
| 1 | plain date separator via `setCustomDate(...)` | `currentMessageObject == null` → `backgroundPath` only | **ordinary** | source `550-571`, `1699-1803`, `3327-3475`; fixture row 1 | E-blocked / A pending |
| 2 | ordinary one-line service action | `messageObject != null`, not button/new-style → `backgroundPath` only | **ordinary** | source `4016-4018`, `1993-2005`; fixture rows 2-3 | E-blocked / A pending |
| 3 | ordinary multi-line service action | same, `textLayout` line count > 1 | **ordinary** | source `3370-3462`; fixture rows 4-6 | E-blocked / A pending |
| 4 | pinned-message / comparable ordinary action | `TL_messageActionPinMessage` is not a button type → `backgroundPath` | **ordinary** | type routing `isButtonLayout`/`isNewStyleButtonLayout` exclude it | A pending |
| 5 | action with reply / navigation interaction | `ChatActionCell` has no reply plate; `hasReplyMessage` is cache-only | **N/A for this owner** (reply plates are `ChatMessageCell`/`ReplyMessageLine`, B2/B4) | `359/609/630` | source-proven |
| 6 | premium / star gift action | `isButtonLayout` → text plate (`backgroundPath`) **plus** card/button (`backgroundRect`, `backgroundPath2`, `giftButtonRect`) | **rich (ordinary plate + special plates)** | `3542-3582`, `3143-3151` | A pending |
| 7 | wallpaper action (`TYPE_ACTION_WALLPAPER` 22) | `isNewStyleButtonLayout` → card + preview circle | **rich** | `1993-2005`, `2823-2835`, `2870-2882` | A pending |
| 8 | offer / community / special-card state (`GIFT_OFFER` 33, `SHARING_OFFER` 35, `GIFT_OFFER_REJECTED` 34, `COMMUNITY_CHANGED` 37, `GIFT_THEME_UPDATE` 31) | `isNewStyleButtonLayout` / `isButtonLayout` → card plate | **rich** | `3542-3582`, `2813-2821` | A pending |
| 9 | action with dedicated button/card background (gift button, bot inline buttons, ribbon) | special paths | **rich / overlay** | `3143-3151`, `3596-3660`, `3234-3275` | A pending |
| 10 | service action with reactions | ordinary `backgroundPath` + `ReactionsLayoutInBubble` overlay | **ordinary + overlay** | `3278`, `3798-3849` | A pending (needs auth reaction data) |
| 11 | suggested post approval, not rejected / not `balance_too_low` | ordinary block, but path replaced by `addRoundRect(dp(15))` | **special inside ordinary pipeline** | `3465-3474` | A pending |
| 12 | `TYPE_ACTION_PHOTO` (11) round-photo action | ordinary `backgroundPath` + round photo overlay | **ordinary + overlay** | `2812-2883`, `2764-2765` | A pending |
| 13 | `TYPE_SUGGEST_BIRTHDAY` (32) | `birthdayLayout != null` → own plate | **rich** | `3529-3541` | A pending |
| 14 | story mention (`TYPE_STORY_MENTION` 24) | `isNewStyleButtonLayout` → round avatar/story overlay | **rich** | `1993-2005`, `2836-2840` | A pending |

Untested rows are account/cell-dependent (real message data) and are explicitly `A pending`, per the
pass's instruction not to fake rich states with a parallel renderer.

---

## 4. Geometry decision: Strategy A vs Strategy B

### 4.1 What each strategy means here (source-grounded)

- **A — line-following chamfered outline**: keep `lineWidths`/`lineHeights` and replace the eight
  `arcTo(...)` transitions (`3397-3460`) with angular convex/concave segments, preserving the
  asymmetric `cornerOffset = dp(3)` / `cornerRest = dp(8)` envelope.
- **B — one compact enclosing chamfer**: replace the two-pass line-following build with a single
  `CybergramBubbleDrawable.buildPath(...)` around the widest line and the total text height.

### 4.2 Layout analysis of ordinary service/date text

Ordinary service/date text is **centre-aligned** and wrapped to `width - dp(30)`
(`createLayout` `1700`, `1738-1740`). Consequence for the silhouette:

- one-line and the common two-line action: the widest line already defines the plate; any shorter
  final line is the only place Strategy B adds side fill;
- Telegram-generated action strings wrap to `maxWidth`, so large width steps are uncommon;
- the upstream smoothing pass already merges small steps (`diff <= 1.5f * corner + cornerIn`,
  `3339-3356`), i.e. upstream itself refuses to hug tiny line-width differences;
- Strategy B therefore adds at most a bounded, symmetric side area below/above the widest line and
  never changes text bounds, vertical placement or the plate's horizontal envelope.

### 4.3 Decision

**Strategy B is recommended** as the B6 working geometry, subject to one explicit gate:

- it reuses the project-owned primitive required by the operating contract
  (`docs/EXECUTION_BACKLOG.md`: "Use `CybergramBubbleDrawable.buildPath(...)` ... instead of
  introducing another chamfer implementation"; `REMAINING_UI_ARCHITECTURE` rule 3);
- it keeps the change inside one method of one shared upstream file, which matters because
  `ChatActionCell` is 4,214 lines and is hosted by nine distinct surfaces;
- it satisfies the UI spec's "compact dark plates" target; the spec's anti-target is *large*
  rounded/glass capsules, not a compact enclosing plate.

Strategy A is **not** selected. It is the correct fallback only if visual evidence shows B is
wasteful; it would require a new, separately-justified concave/convex path helper rather than ad-hoc
math inside `ChatActionCell`, and it carries more regression risk in a high-risk shared file.

### 4.4 Evidence limitation (recorded honestly)

The pass requires multi-line **visual** evidence to accept/reject B. That evidence could not be
captured in this environment: the API 36 x86_64 emulator is blocked by the execution sandbox (see
§7). B5 therefore records Strategy B as the **recommended working decision with an open
visual-density gate**, not as visually confirmed. B6 must close that gate before it can be called
validated (see §5, `E-density` row). If the gate rejects B, B6 stops and Strategy A is designed
separately.

---

## 5. Bounded B6 proposal (NOT implemented in B5)

### 5.1 Production file scope

- **Primary (required):** `TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatActionCell.java`.
- **Existing helper (import only, no change):** `org.telegram.ui.ActionBar.CybergramBubbleDrawable`
  (`buildPath`) and `org.telegram.ui.ActionBar.CybergramTheme` (`useAngularMessageGeometry` /
  `BUBBLE_CORNER_CUT_DP`). No new geometry class.
- **Conditional extra (only if the ThemePreview leak in §6 is confirmed):**
  `ThemePreviewActivity.java` — a single debug-free opt-out call. Not part of the default scope.

### 5.2 Exact central gate

At the draw site the provider actually available is the `themeDelegate` field
(`ChatActionCell.java:386`, set at `505-509`). Use the **existing** central gate:

```java
CybergramTheme.useAngularMessageGeometry(themeDelegate)
```

No theme-name string, no colour heuristic, no new gate. (This is the same gate `MessageDrawable`
uses at `MessageDrawable.java:810`.)

### 5.3 Exact ordinary-vs-rich condition

```java
private boolean useCybergramOrdinaryServicePlate() {
    return CybergramTheme.useAngularMessageGeometry(themeDelegate)
        && !isButtonLayout(currentMessageObject)              // gifts/stars/new-style cards
        && !isMessageActionSuggestedPostApproval();           // path replaced by addRoundRect(dp(15))
}
```

`isButtonLayout(null)` returns false and `isMessageActionSuggestedPostApproval()` is null-safe, so
the `currentMessageObject == null` date/floating/info case is included. This condition deliberately
leaves rich/button/birthday/star-gift/offer/community/wallpaper/story/`TYPE_ACTION_PHOTO`-photo and
the suggested-post-approval override geometry on their upstream path.

### 5.4 Exact geometry and bounds (Strategy B)

Inside `if (invalidatePath)` (`3327`), branch **after** `invalidatePath = false;` and the
`backgroundLeft/backgroundRight/lineWidths` reset, and **instead of** the two-pass build. Compute:

```text
maxLineWidth = textLayout == null ? 0 : textLayout.getWidth()
pad          = dp(8)                       // matches upstream side inset cornerRest = corner - cornerOffset
top          = dp(4)                       // unchanged upstream start
bottom       = top + textHeight + dp(6)    // equals upstream path bottom for the ordinary case
left         = x - maxLineWidth / 2f - pad
right        = x + maxLineWidth / 2f + pad
cut          = dp(CybergramTheme.BUBBLE_CORNER_CUT_DP)   // 6dp, shared angular idiom
CybergramBubbleDrawable.buildPath(backgroundPath, left, top, right, bottom, cut);
backgroundLeft  = (int) Math.floor(left);
backgroundRight = (int) Math.ceil(right);
```

This preserves the footprint contract (`getBoundsLeft()/getBoundsRight()`, touch hit-tests at
`1236/1309`, `MessageEntityView` positioning). The suggested-post-approval override (`3465-3474`)
stays in the non-Cybergram branch and is untouched.

### 5.5 Explicitly unchanged

- `backgroundPaint` / `darkenBackgroundPaint` / `dimPaint` draw sequence `3499-3512`;
- `applyServiceShaderMatrix` `3480-3484`; `hasGradientService()` `3889-3891`;
- text measurement (`createLayout`), `textX/textY/textWidth/textHeight`, `onMeasure`;
- all rich branches `3515-3582` and their `drawRoundRect`/`backgroundPath2` geometry;
- gift button, ribbon, stars clip, bot buttons, reactions, topic separator;
- click/touch logic, message/action data types, `overrideBackground` paint path;
- the entire non-Cybergram path (the `else` branch is byte-for-byte upstream);
- non-goal compliance: no whole-cell clip, no combined reaction/normal-bubble work, no release
  manifests.

### 5.6 B6 validation matrix

| tier | check |
|---|---|
| `E-build` | `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64` → BUILD SUCCESSFUL; record APK size/SHA-256 |
| `E-geometry` | debug fixture rows show a 45° chamfer (slope-1 ramp) on ordinary one-line/multi-line/date plates; pixel-measure corner slope and plate envelope |
| `E-density` | **the open B5 gate**: confirm the enclosing plate is not visibly wasteful on uneven multi-line rows; if rejected, stop B6 and design Strategy A separately |
| `E-control` | the same fixture rendered through a non-Cybergram provider still shows the upstream rounded ordinary path |
| `E-rich` | rich fixture rows (gift/offer/wallpaper/birthday/community) remain upstream rounded |
| `A` | authenticated chat: date separators, pin/unpin, join/leave, title/photo/TTL changes, group-call and screenshot actions ordinary; gifts/offers/wallpapers/birthday/community unchanged; service-message reactions still render (pill geometry owned by B4) |
| `P` | Samsung/OEM spot check |

---

## 6. Unresolved issues / open items

1. **Visual-density gate (blocking B6 validation).** Emulator E blocked here (§7); must be captured
   before B6 is called validated.
2. **`ThemePreviewActivity` leak risk.** `ThemePreviewActivity.java:5370` constructs a real
   `ChatActionCell` with the *preview* provider. `isCybergramPresentation(provider)` is false for a
   plain preview provider, but falls back to `Theme.getCurrentTheme()`; if the active app theme is
   Cybergram, the service plate could render angular inside a preview of a **different** theme.
   `MessageDrawable` avoids the analogous problem by excluding `TYPE_PREVIEW`. Verify during B6 `A`;
   if confirmed, add the minimal local opt-out in `ThemePreviewActivity` (the only conditional extra
   file).
3. **`SharedMediaLayout` floating date** uses `new ChatActionCell(context)` (null provider) plus
   `setOverrideColor(...)`; the gate falls back to `Theme.getCurrentTheme()`. This is ordinary
   geometry and is expected to angularize under Cybergram — confirm it is acceptable in B6 `A`.
4. **Rich-case E evidence** (gifts/offers/wallpapers) is not reproducible pre-auth and stays
   `A pending`; B5 did not fake it.
5. **`TYPE_ACTION_PHOTO`**: its text plate angularizes under the B6 gate while the round photo
   overlay is untouched. Confirm visually in `A`.

---

## 7. Environment blocker (E tier)

The API 36 x86_64 emulator could not be launched under the execution sandbox. Five configurations
were attempted (original AVD `Cybergram_API36` and a workspace-local AVD `Cybergram_B5`, host and
swiftshader GPU, windowed and `-no-window`, with `Wifi`/`WiFiPacketStream`/`Uwb`/
`NetsimWebUi`/`NetsimCliUi`/`ModemSimulator`/`VirtioWifi` disabled). Every run aborts at the same
point:

- the emulator reaches `netsimd I ... rust_main.rs:93` and then exits with code `-36863`
  (`0xFFFF7001`) before the guest boots;
- `adb devices` never lists the emulator (only the unrelated physical device
  `4H8L598LAME6CEX4 unauthorized` is present);
- the sandbox denies writes to `~/.android`; relocating `ANDROID_EMULATOR_HOME`/`ANDROID_AVD_HOME`/
  `ANDROID_USER_HOME` into the workspace removed the `~/.android` lock errors but not the abort.

The failure point (immediately after the emulator spawns the `netsimd` child) is consistent with the
documented sandbox restriction on pipe/named-pipe stdio for child processes. Sandbox escalation to
`danger-full-access` was requested for the build and rejected because **no approval channel is
available**, so no wider-mode retry is possible.

The Gradle build itself was made to work inside the sandbox by pointing `GRADLE_USER_HOME` at a
workspace-local copy of the Gradle cache (see §8), producing a successful build and a verifiable APK.

To reproduce the (currently blocked) E run once a sandbox with process/pipe access is available:

```text
emulator -avd <API36_x86_64_AVD> -no-snapshot
adb install -r TMessagesProj_App/build/outputs/apk/afat/debug/app.apk
adb shell am start -n org.telegram.messenger.beta/org.telegram.ui.CybergramShowcaseActivity --ez cybergram_b5 true
adb exec-out screencap -p > b5_showcase.png
```

---

## 8. E-tier build evidence and debug fixture

### 8.1 Debug-only fixture (outside release source)

New file (debug source set only):

`TMessagesProj_App/src/debug/java/org/telegram/ui/CybergramB5ServiceDateFixture.java`

It renders the **real production** `ChatActionCell` ordinary path for deterministic cases (real date
via `setCustomDate`, one-line short/long, two-line uneven, three-line, long wrapped) using only
production public entry points (`setCustomText`/`setCustomDate`, `setVisiblePart`, `measure`,
`layout`, `draw`), with `currentMessageObject == null`, so exactly `backgroundPath` is exercised. A
magenta 1dp Strategy-B candidate outline derived from the cell's own measured envelope is overlaid as
a clearly-labelled debug annotation — it is not a production render. It also computes each row's
plate width from `getBoundsLeft()/getBoundsRight()` for measurement.

Wiring: `CybergramShowcaseActivity` gained a debug-only `--ez cybergram_b5 true` mode and a
`B5OnlyView` host. No release source or manifest was touched; no preference is written.

### 8.2 Build

```text
java -cp gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain \
     :TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64 --console=plain
```

with `JAVA_HOME=<JDK 17>` and `GRADLE_USER_HOME=<workspace-local cache copy>`.

- Result: **BUILD SUCCESSFUL** (1m 13s, 82 tasks).
- APK: `TMessagesProj_App/build/outputs/apk/afat/debug/app.apk`.
- Size: `74,277,072` bytes.
- SHA-256: `d3a861d4b929f3c7f9e06b61a2f202e31224c3194b4e80da7eef802081760c35`.
- Artifact proof: `CybergramB5ServiceDateFixture` and the `cybergram_b5` extra are present in
  `classes5.dex` of the packaged APK.

### 8.3 Runtime

Not executed — emulator blocked (§7). The fixture is committed so the run above (`§7`) produces the
B5 visual evidence and closes the §4.4 / §5.6 `E-density` gate as soon as an E environment is
available.

---

## 9. Production-diff proof

After the B5 commits, the production diff from the base must be empty:

```text
git diff 2db3b48e7f3425518278a909ff65594a5410962a..HEAD -- TMessagesProj/src/main
```

B5 made **no production rendering change**. Only `TMessagesProj_App/src/debug/...` (debug source
set) and `docs/` were modified.
