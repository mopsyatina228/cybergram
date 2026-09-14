# B5 — service/date plate ownership and implementation audit

Status: `AUDIT COMPLETE / E VISUAL GATE PASS / STRATEGY B SELECTED / A RICH+AUTHENTICATED CASES PENDING / P NOT REQUIRED / NO PRODUCTION CHANGE`

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
| 1 | plain date separator via `setCustomDate(...)` | `currentMessageObject == null` → `backgroundPath` only | **ordinary** | source `550-571`, `1699-1803`, `3327-3475`; fixture row 1 | **E pass** (§8.3, `b5_corrected.png`) / A pending |
| 2 | ordinary one-line service action | `messageObject != null`, not button/new-style → `backgroundPath` only | **ordinary** | source `4016-4018`, `1993-2005`; fixture rows 2-3 | **E pass** (§8.3, `b5_corrected.png`) / A pending |
| 3 | ordinary multi-line service action | same, `textLayout` line count > 1 | **ordinary** | source `3370-3462`; fixture rows 4-6 | **E pass** (§8.3, `b5_corrected.png`) / A pending |
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
pass's instruction not to fake rich states with a parallel renderer. Rows 1-3 are additionally
covered by the corrected E-tier ordinary-path run (§8.3): the real `ChatActionCell` produced all
five ordinary cases on the API 36 emulator, so the ordinary ownership claim is now runtime-confirmed
as well as source-proven. Rich/special rows remain `A pending` and were not faked.

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

**Strategy B is selected** as the B6 geometry (the E visual-density gate passed — see §4.4 and §8.3):

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

### 4.4 Visual-density gate — PASS

The pass requires multi-line **visual** evidence to accept/reject B. That evidence was captured in
the corrected E-tier run (§8.3, screenshot `b5_corrected.png`) and reviewed by the supervisor.

Supervisor visual review of the corrected screenshot: the Strategy-B single enclosing chamfer is
compact on the date and one-line cases; on the two-line uneven and three-line cases it adds only
moderate symmetric side whitespace around the shorter centred lines and does not become a broad or
wasteful banner; it reads as visually cleaner and more consistent with Cybergram's angular plates
than preserving the upstream line-following rounded silhouette. The long case remains proportional
to its content.

**Outcome: the B5 visual-density gate PASSES and Strategy B is SELECTED** (no longer provisional).
Strategy A is **not** selected and does not need to be designed. B6 is cleared to implement
Strategy B as specified in §5.

---

## 5. Bounded B6 proposal (NOT implemented in B5)

### 5.1 Production file scope

- **Primary (required):** `TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatActionCell.java`.
- **Existing helper (import only, no change):** `org.telegram.ui.ActionBar.CybergramBubbleDrawable`
  (`buildPath`) and `org.telegram.ui.ActionBar.CybergramTheme` (`useAngularMessageGeometry` /
  `BUBBLE_CORNER_CUT_DP`). No new geometry class.
- **Conditional extra (only if a ThemePreview leak is confirmed):** `ThemePreviewActivity.java` — a single
  debug-free opt-out call. **Independent review of the implemented seam found no leak reachable in current
  code**, so B6 did *not* touch this file; it remains a conditional future item (§6.2).

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
| `E-density` | **PASSED in B5** (§8.3): the corrected fixture screenshot showed the enclosing plate is compact on date/one-line cases and only moderately wider on uneven multi-line rows; no broad/wasteful banner. B6 re-confirms on its own APK |
| `E-control` | the same fixture rendered through a non-Cybergram provider still shows the upstream rounded ordinary path |
| `E-rich` | rich fixture rows (gift/offer/wallpaper/birthday/community) remain upstream rounded |
| `A` | authenticated chat: date separators, pin/unpin, join/leave, title/photo/TTL changes, group-call and screenshot actions ordinary; gifts/offers/wallpapers/birthday/community unchanged; service-message reactions still render (pill geometry owned by B4) |
| `P` | not required for this audit; optional Samsung/OEM spot check only if an actual device/OEM defect appears |

---

## 6. Unresolved issues / open items

1. ~~**Visual-density gate (blocking B6 validation).**~~ **RESOLVED in B5**: the corrected E-tier run
   and supervisor visual review closed the gate; Strategy B is selected (§4.4, §8.3). No open
   density item remains for B6.
2. **`ThemePreviewActivity` leak risk — RESOLVED for current code (2026-09-14).** `ThemePreviewActivity:5370`
   does construct a real `ChatActionCell`, and `isCybergramPresentation(provider)` falls back to
   `Theme.getCurrentTheme()` for a plain preview provider. **Independent review of the implemented B6 seam,
   re-confirmed against source in the 2026-09-14 reconciliation, found the leak unreachable today:** in the
   foreign app-theme `SCREEN_TYPE_PREVIEW` path the adapter instantiates no `ChatActionCell` at all — its only
   two `contentType` assignments (`:4940` → 5, `:4962` → 1; view type 1 maps to `ChatActionCell`) both live
   inside `screenType == SCREEN_TYPE_CHANGE_BACKGROUND` with `dialogId != 0 && serverWallpaper == null`, i.e.
   the wallpaper/chat-theme preview, which is *current app presentation* and must follow the active theme.
   **No extra `ThemePreviewActivity` production opt-out is required now**, and none was added. **Latent
   caveat:** if a future foreign-theme preview ever instantiates a `ChatActionCell`/`contentType == 1` row, it
   will need an explicit opt-out, because the global `Theme.getCurrentTheme()` fallback is not preview-scoped.
3. **`SharedMediaLayout` floating date** uses `new ChatActionCell(context)` (null provider) plus
   `setOverrideColor(...)`; the gate falls back to `Theme.getCurrentTheme()`. This is ordinary
   geometry and is expected to angularize under Cybergram — confirm it is acceptable in B6 `A`.
4. **Rich-case E evidence** (gifts/offers/wallpapers) is not reproducible pre-auth and stays
   `A pending`; B5 did not fake it.
5. **`TYPE_ACTION_PHOTO`**: its text plate angularizes under the B6 gate while the round photo
   overlay is untouched. Confirm visually in `A`.

---

## 7. Executor-sandbox emulator limitation (troubleshooting history — NOT current status)

> **Current B5 runtime status:** E was completed successfully on the real host, outside the DSH
> sandbox, via Remote Desktop Commander (§8.3). The DSH-local failure below is retained only as
> troubleshooting history and is **not** an open B5 blocker or a product/AVD defect.

An earlier attempt to launch the API 36 x86_64 emulator **inside the DSH execution sandbox** failed.
Five configurations were attempted (original AVD `Cybergram_API36` and a workspace-local AVD
`Cybergram_B5`, host and swiftshader GPU, windowed and `-no-window`, with
`Wifi`/`WiFiPacketStream`/`Uwb`/`NetsimWebUi`/`NetsimCliUi`/`ModemSimulator`/`VirtioWifi` disabled).
Every run aborted at the same point:

- the emulator reaches `netsimd I ... rust_main.rs:93` and then exits with code `-36863`
  (`0xFFFF7001`) before the guest boots;
- `adb devices` never lists the emulator (only the unrelated physical device
  `4H8L598LAME6CEX4 unauthorized` is present);
- the sandbox denied writes to `~/.android`; relocating
  `ANDROID_EMULATOR_HOME`/`ANDROID_AVD_HOME`/`ANDROID_USER_HOME` into the workspace removed the
  `~/.android` lock errors but not the abort.

The failure point (immediately after the emulator spawns the `netsimd` child) is consistent with the
documented sandbox restriction on child-process pipe/named-pipe stdio. This was an **executor
sandbox/environment limitation, not an AVD or product failure**: the same existing AVD
`Cybergram_API36` later booted normally on the real host without any AVD change (§8.3).

Historical DSH-local reproduction attempt (this failed; do not treat as B5 status):

```text
emulator -avd <API36_x86_64_AVD> -no-snapshot          # DSH-local: aborts at netsimd, exit -36863
adb install -r TMessagesProj_App/build/outputs/apk/afat/debug/app.apk
adb shell am start -n org.telegram.messenger.beta/org.telegram.ui.CybergramShowcaseActivity --ez cybergram_b5 true
adb exec-out screencap -p > b5_corrected.png
```

The Gradle build was likewise made to work inside the sandbox by pointing `GRADLE_USER_HOME` at a
workspace-local copy of the Gradle cache (see §8.2), but the authoritative build/install/run for
B5 was performed directly on the real host (§8.2-§8.3).

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
a clearly-labelled debug annotation — it is not a production render.

Plate-width measurement was **corrected** in debug commit
`4ae5a973ae953700cc209d520136fde461eef6a0` ("debug: use raw ChatActionCell background bounds in B5
fixture"): the fixture now reads the cell's private `backgroundLeft`/`backgroundRight` fields
directly, after the real draw, instead of deriving bounds from the public accessors. The earlier
public-bounds derivation was buggy and produced **invalid** plate-width evidence; the previous
full-row candidate screenshot rendered from it is invalid evidence and is **superseded by
`b5_corrected.png`**. No production file was touched by this correction (debug source set only).

Wiring: `CybergramShowcaseActivity` gained a debug-only `--ez cybergram_b5 true` mode and a
`B5OnlyView` host. No release source or manifest was touched; no preference is written.

### 8.2 Build

**Authoritative build (real host, after the corrected debug fixture commit
`4ae5a973ae953700cc209d520136fde461eef6a0`):**

```text
:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64
```

- Result: **BUILD SUCCESSFUL in 57s** — 82 tasks, 7 executed / 75 up-to-date.
- APK: `TMessagesProj_App/build/outputs/apk/afat/debug/app.apk`.
- Final APK SHA-256:
  `e4411dd071f8600a58bfa6b9ff08044d0d3b0dc24fad8db99bb10f906dd556ad`.
- Install to `org.telegram.messenger.beta` on `emulator-5554`: **succeeded**.
- Artifact proof: `CybergramB5ServiceDateFixture` and the `cybergram_b5` extra are present in the
  packaged APK.

**Historical DSH-local build** (pre-correction fixture, retained for provenance):

```text
java -cp gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain \
     :TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64 --console=plain
```

with `JAVA_HOME=<JDK 17>` and `GRADLE_USER_HOME=<workspace-local cache copy>`.

- Result: **BUILD SUCCESSFUL** (1m 13s, 82 tasks).
- APK size: `74,277,072` bytes.
- SHA-256 (superseded by the authoritative build above):
  `d3a861d4b929f3c7f9e06b61a2f202e31224c3194b4e80da7eef802081760c35`.

### 8.3 Runtime (E) — completed on the real host

Executed outside the DSH sandbox on the real host via Remote Desktop Commander, using the
**existing** AVD `Cybergram_API36` (no AVD recreation):

- AVD booted successfully and appeared as `emulator-5554`; `sys.boot_completed=1`.
- The corrected debug fixture commit `4ae5a973ae953700cc209d520136fde461eef6a0` was present; it reads
  `ChatActionCell`'s private `backgroundLeft`/`backgroundRight` after the real draw. No production
  files changed.
- The final APK from §8.2 (`e4411dd0...56ad`) was installed to `org.telegram.messenger.beta`.
- Validation sequence: **normal app launch first**, then `CybergramShowcaseActivity` with
  `--ez cybergram_b5 true`. Session stable; FATAL/ANR/process-crash scan = **0**.
- Final screenshot: `.local-artifacts/b5/b5_corrected.png` (git-excluded).

Corrected real ordinary-path plate widths printed by the fixture:

| case | plate width |
|---|---|
| date | 98 px |
| one-line short | 158 px |
| one-line long | 288 px |
| two-line uneven | 230 px |
| three-line | 310 px |
| long (wrapped) | 640 px |

The long case remains proportional to its content (it is the widest wrapped line), and the
two-line/three-line cases are only moderately wider than their longest line, confirming the §4.4
density conclusion. The earlier full-row candidate screenshot produced from the buggy public-bounds
measurement is **invalid evidence and is superseded by `b5_corrected.png`**.

**E visual-density gate: PASS — Strategy B SELECTED.** B6 is cleared to implement §5.

A rich/authenticated cases (gifts, offers, wallpapers, birthday, community, story, reactions,
`TYPE_ACTION_PHOTO`) remain `A pending` as already noted in §3 and §6, because they require real
authenticated message data and were deliberately not faked. P was not required for this audit.

---

## 9. Production-diff proof

After the B5 commits, the production diff from the base must be empty:

```text
git diff 2db3b48e7f3425518278a909ff65594a5410962a..HEAD -- TMessagesProj/src/main
```

B5 made **no production rendering change**. Only `TMessagesProj_App/src/debug/...` (debug source
set) and `docs/` were modified. Verified at the final B5 evidence-correction commit: the command
above is empty and `git diff --check` is clean.

**SUPERSEDED 2026-09-14 — B6 is now IMPLEMENTED and INTEGRATED on `dev`.** This audit closed with “B6 remains
NOT IMPLEMENTED”; it now exists as a bounded seam, fast-forwarded onto `dev` from branch
`feature/cybergram-service-date-angular` (production
commit `f86ef812bb585db7c753335c7f573406e5129323`, `ChatActionCell.java` only, **+22/−1**; independent review
verdict `SAFE_MINIMAL_SEAM`), with `E-build`/`E-install+launch`/`E-geometry`/`E-control` PASS, `E-rich`
unavailable pre-auth and `A` pending; it is **integrated on `dev`** by ff-only fast-forward (no squash, no merge
commit; integration final SHA `0c4172346764c5622a5cdbfa06a4ce624d3cd56a`). The §5 proposal is otherwise unchanged:
Strategy B — one compact enclosing `CybergramBubbleDrawable.buildPath(...)` plate for ordinary service/date
geometry only, with the already-documented rich/special exclusions (`isButtonLayout`, new-style cards, birthday,
star gift, offer/community/wallpaper/story, bot buttons/ribbon and the suggested-post-approval override) left on
their upstream paths. Implementation/evidence record: `docs/B6_SERVICE_DATE_IMPLEMENTATION_2026-09-14.md`.
`docs/CYBERGRAM_UI_SPEC.md` was not modified; `CURRENT_STATE.md` / `EXECUTION_BACKLOG.md` / the pass index are
not touched by this evidence corrections note — those files were updated only by the separate 2026-09-14 B6
reconciliation.
