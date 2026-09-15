# B7 — chat-canvas HUD decoration layer

Status: `SPEC PREPARED / NOT AUTHORIZED`

Type: bounded implementation contract (prepared, **not** authorized for execution)

Production fixes: **NOT AUTHORIZED IN THIS PASS**

Authorization state: B7 is an **optional** pass. This document prepares the contract so that a
future decision can be executed without new reconnaissance. It does **not** authorize production code,
and writing it grants nothing. `docs/EXECUTION_BACKLOG.md` records that a pass does not authorize the next
pass and that the user chooses execution priority.

Design authority: `docs/CYBERGRAM_UI_SPEC.md` (lines 19, 21, 23, 25, 55, 113, 115, 117)

Planning authority: `docs/EXECUTION_BACKLOG.md`

Static ownership evidence below was re-established 2026-09-14 against `dev`
`23882dbf00e23b4a521f1185aeea2556d1c5afe7` (equal to `origin/dev`) by local `git grep` and direct source
reading. Re-run it at execution time; do not trust this copy.

## Mission

Give the chat canvas an optional, non-interactive Cybergram structural/HUD decoration layer, drawn over the
wallpaper and behind the message list, without touching Telegram's message renderers, wallpaper behaviour,
scroll/gesture plumbing, blur capture, or any non-Cybergram presentation.

`docs/CYBERGRAM_UI_SPEC.md` line 113 requires exactly this shape of work: decorative lines, corner marks, IDs
and micro-labels *"must be drawn by a dedicated Cybergram layer or reusable component rather than hard-coded
independently into screens."* B7 is that dedicated layer for the chat canvas. It is the canvas counterpart of
the already-landed header decoration layer.

## Why this is prepared but not started

`docs/EXECUTION_BACKLOG.md` states B7 must not start before B1 and the primary message/service presentation are
coherent, and that file scope must **not** be inferred from the backlog entry alone. As of 2026-09-14:

- B1 (main bottom navigation) is integrated at E tier;
- message bodies are angular (B2: `TYPE_TEXT`/`TYPE_MEDIA` PASS);
- ordinary service/date plates are angular (B6 integrated);
- replies/reactions remain `DESIGN-OPEN` (B4), which is an open design question, not a defect.

The backlog's stated precondition is therefore met at E tier. Starting B7 remains a **priority decision for
the user**, not an inference: B7 is decorative and optional, while the largest outstanding debt in this
repository is authenticated (A) verification of already-integrated work. See "Relationship to other open
work" below.

## Verified static ownership (2026-09-14)

### Chat canvas container

- `TMessagesProj/src/main/java/org/telegram/ui/ChatActivity.java:4559` —
  `fragmentView = contentView = new ChatActivityFragmentView(context, parentLayout);`
- `ChatActivity.java:17085` — `public class ChatActivityFragmentView extends SizeNotifierFrameLayout`
  (constructor at 17091).
- The container overrides, but does **not** currently draw the canvas itself:
  - `addView(View child, int index, ViewGroup.LayoutParams params)` — `ChatActivity.java:17342-17357`. This is
    the existing z-order precedent: for `botCommandsMenuContainer` it computes
    `indexToAdd = chatActivityFadeView != null ? indexOfChild(chatActivityFadeView) : -1` and inserts
    relative to an already-placed sibling (17344-17345).
  - `onUpdateBackgroundDrawable(Drawable)` — `ChatActivity.java:17222` (a background-change hook already
    exists).
  - `drawList(Canvas blurCanvas, RectF position)` — `ChatActivity.java:17312` (blur-capture path, drives
    `Blur3Utils.captureRelativeParent`).
  - `getScrollOffset()` 17360, `getBottomOffset()` 17365, `getListTranslationY()` 17370.
  - `onAttachedToWindow()` 17375, `onDetachedFromWindow()` 17395.
- The container **already overrides the draw pipeline** — this is the single most important constraint:
  - `onDraw(Canvas)` — `ChatActivity.java:17539-17547`. Guarded by `BlurBehindDrawable.TAG_DRAWING_AS_BACKGROUND`
    checks (17540-17545), then `super.onDraw(canvas)` (17546). Content drawn here lands **below all children**.
  - `drawChild(Canvas, View, long)` — `ChatActivity.java:17550-…`. Substantial z-order and blur logic: it
    suppresses named overlay children while a scrim/enter transition runs (17551-17553), and while
    `setTag(BlurBehindDrawable.TAG_DRAWING_AS_BACKGROUND)` is set it draws only `actionBar` (static) or
    `chatListView`/`chatInputViewsContainer`/`bottomChannelButtonsLayout` (otherwise), returning `false` for
    every other child (17566-17581). **Any new child View added to `contentView` is drawn through this
    method**, so the HUD's blur/overlay interaction must be proven, not assumed.
  - `dispatchDraw(Canvas)` — `ChatActivity.java:17710-…`. Updates layout/visible-part state, applies a
    `saveLayerAlpha`+scale for topic switching (17726-17730), calls `super.dispatchDraw(canvas)` at 17731,
    then draws send-animations (17748-17773) and scrim overlays (17774-…) **on top of** children.
- There is therefore **no unused canvas-level draw seam**: both view-level hooks are already occupied and,
  before `super`, draw below every child — including the wallpaper child at index 0. A HUD drawn in `onDraw`
  or in `dispatchDraw` before `super` would be hidden behind the wallpaper; drawn after `super` in
  `dispatchDraw` it would sit on top of messages. This is why the recommended seam is a dedicated child View
  (Option A) rather than a canvas-draw hook.
- `git grep Cybergram` in `SizeNotifierFrameLayout.java` → no matches, so no Cybergram canvas seam exists yet in
  the shared base class either.

### Wallpaper layer (must not be disturbed)

- `TMessagesProj/src/main/java/org/telegram/ui/Components/SizeNotifierFrameLayout.java:166` —
  `private class BackgroundView extends View`, `onDraw` at 172 guarded by
  `backgroundDrawable == null || skipBackgroundDrawing` (173).
- `SizeNotifierFrameLayout.java:363-368` — `setBackgroundImage(...)` lazily creates `backgroundView` and adds
  it at **child index 0** via `addView(backgroundView, 0, MATCH_PARENT/MATCH_PARENT)`.
- `SizeNotifierFrameLayout.java:92` — `public View backgroundView;` (public field, so a sibling index can be
  computed deterministically with `indexOfChild(backgroundView)`).
- `SizeNotifierFrameLayout.java:431` — `public Drawable getBackgroundImage()`;
  `:586` — `public void setSkipBackgroundDrawing(boolean)`.
- `SizeNotifierFrameLayout.java:834-841` — `dispatchDraw` performs blur-node invalidation/`startBlur()` before
  `super.dispatchDraw(canvas)`.
- `ChatActivity.java:44179-44190` — `updateBackground()`; it returns early when
  `contentView == null || parentThemeDelegate != null`, then calls
  `contentView.setBackgroundImage(drawable, Theme.isWallpaperMotion())` at 44188.
  `parentThemeDelegate` is assigned for embedded/preview hosts (e.g. 7729, 13569, 13587).

### Message list and canvas z-order

`contentView` children are added in this order (selection, earliest first):

| line | child |
|---|---|
| `SizeNotifierFrameLayout:368` | `backgroundView` (index 0, lazy) |
| `ChatActivity:4561` | `invalidateBlurredSourcesView` |
| `ChatActivity:6979` | `chatListView` (`MATCH_PARENT`) |
| `ChatActivity:6985` | `chatActivityFadeView` |
| `ChatActivity:6989` | `selectionReactionsOverlay` |
| `ChatActivity:6994` | `animatingImageView` |
| `ChatActivity:6998` | `progressView` |
| `ChatActivity:7797` | `actionBar` |
| `ChatActivity:7807` | `overlayView` |
| `ChatActivity:8986` | `CybergramHeaderDecorationView` (top z-order) |

The wallpaper therefore sits **below** the message list, and the message list is the first full-size child
after it.

### Sharing boundaries (the decisive constraint)

- `ChatActivity.ChatActivityFragmentView` is used **only by `ChatActivity`**. `ChannelAdminLogActivity` has its
  own separate nested class of the same name (`ChannelAdminLogActivity.java:4493`), so a seam inside
  `ChatActivity`'s container does not leak into the admin-log screen.
- `SizeNotifierFrameLayout` is **heavily shared**: 19 classes extend it, including `ArticleViewer`,
  `ChannelMonetizationLayout`, `AvatarConstructorFragment`, `NestedSizeNotifierLayout`,
  `SizeNotifierFrameLayoutPhoto`, `TrendingStickersAlert`, `DialogsActivity.ContentView`, `LocationActivity`,
  `PhotoViewer`, `ProfileActivity`, `ProfileActivity2`, `BotStarsActivity`, `PeerStoriesView`, `PaintView`,
  `StoryRecorder`, `BotWebViewAttachedSheet`, `BotWebViewSheet` and
  `ChannelAdminLogActivity.ChatActivityFragmentView`.
  **The seam must not be placed in `SizeNotifierFrameLayout`.**
- `ThanosEffect.java:874-875` casts `chatListView.getParent()` to `ChatActivity.ChatActivityFragmentView`,
  so the container's child structure is assumed by at least one other component; do not renumber existing
  children.

### Existing precedent to copy — `CybergramHeaderDecorationView`

`TMessagesProj/src/main/java/org/telegram/ui/CybergramHeaderDecorationView.java` (150 lines) is the landed
pattern for a dedicated Cybergram decoration layer and is the model for B7:

- non-interactive: `onTouchEvent` returns `false` (71-73), `setClickable(false)`, `setFocusable(false)`,
  `setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO)` (64-66);
- **runtime gate inside `onDraw`** — `if (actionBar == null || !CybergramTheme.isCybergramPresentation(resourcesProvider)) return;` (84-86);
- always present in the hierarchy so a Day ↔ Cybergram theme switch is reflected **without recreating the
  activity** (documented at 27-29);
- reuses the shared polygon: `CybergramBubbleDrawable.buildPath(...)` (143) — never a second chamfer
  implementation;
- installed by the host as a full-size sibling:
  `ChatActivity.java:8986-8987` → `contentView.addView(new CybergramHeaderDecorationView(context, actionBar, getResourceProvider()), LayoutHelper.createFrame(MATCH_PARENT, MATCH_PARENT))`;
  also used by `DialogsActivity.java:4750` with a `DecorStateProvider`.

### Available Cybergram primitives

- `CybergramTheme.isCybergramPresentation(Theme.ResourcesProvider)` — 81 (central gate);
- `CybergramTheme.useAngularMessageGeometry(Theme.ResourcesProvider)` — 102;
- `CybergramTheme` constants: `DANGER` (20), `HEADER_RULE_ALPHA` (49), `HEADER_TECH_ALPHA` (52),
  `DIALOGS_ROW_*_ALPHA` (55-61), palette values (12-34);
- `CybergramBubbleDrawable.buildPath(...)` — overloads at 122 and 144;
- `CybergramHudDrawable` — chamfered plate/frame drawable with per-corner cuts
  (`setCornerCuts(topLeft, topRight, bottomRight, bottomLeft)`, 115).

## Candidate seams (decision required at execution, not here)

### Recommended: Option A — dedicated non-interactive child View between the wallpaper and the list

Add a new `CybergramChatCanvasHudView` (name to be fixed at execution) as a full-size sibling inserted
immediately above the wallpaper layer and below `chatListView`, gated at runtime in `onDraw`.

- insertion index is deterministic: `indexOfChild(contentView.backgroundView) + 1`, or an
  `indexOfChild(chatListView)`-relative insert copied from the existing 17344-17345 precedent;
- `backgroundView` is only created lazily by the first `setBackgroundImage` call, so the HUD must be inserted
  after that call (or re-ordered defensively) — **this is the main implementation detail to resolve and prove
  at execution**, not to guess now;
- mirrors `CybergramHeaderDecorationView` exactly (non-interactive, always present, gated in `onDraw`);
- the new child will be routed through the container's existing `drawChild` (17550-…); the expected and
  `BlurBehindDrawable`-consistent result is that the HUD is **not** captured into a blur/background pass
  (17566-17581 returns `false` for non-listed children), but this must be verified against both blur modes;
- keeps the shared base class untouched.

### Rejected: Option B — draw from the container's existing canvas hooks

Both hooks already exist and neither places a HUD in the required layer:

- `onDraw` (17539-17547) runs before `dispatchDraw`, i.e. below the wallpaper child at index 0 → the HUD would
  be invisible behind the wallpaper;
- `dispatchDraw` (17710-…) before `super.dispatchDraw` (17731) has the same problem, and after
  `super.dispatchDraw` it would draw over the message list, risking readability and requiring alpha/saveLayer
  machinery that interacts with the topic-switch `saveLayerAlpha` (17726-17730) and the send-animation and
  scrim passes.

Higher risk than Option A, with a worse layer position in every variant.

### Rejected: Option C — draw inside `SizeNotifierFrameLayout.BackgroundView`

Would modify a class shared by 19 hosts. Rejected by the repository's operating contract.

## Required presentation rules

Derived from `docs/CYBERGRAM_UI_SPEC.md`; these are constraints, not suggestions:

- **Sparse and structural, never noisy** (line 19). Strong silhouettes, thin borders, restrained technical
  decoration.
- **HUD marks may occupy unused edge space only, and must remain sparse** (line 55).
- **Do not reduce message readability** (line 17: readable/usable at normal phone scale; line 25: message
  length, localization, large fonts, media, reactions and reply blocks must survive).
- **Do not reduce wallpaper/media behaviour** — the layer must not obscure, crop, tint away or re-scale the
  wallpaper, and must coexist with gradient/motion/pattern/custom-image wallpapers.
- **Rails, not surfaces**: cyan rails are neutral/interactive structure, thin red rails for framing/state
  emphasis; *"None of these accents should become large opaque surfaces without a functional reason"*
  (line 115).
- **No microtext and no fake security claims** — no `SECURE CHAT`, no `END-TO-END`, no invented IDs that
  misrepresent Telegram (line 117). This is a hard stop, not a style preference.
- **Non-interactive and inert** — never intercept touch, never focusable, never an accessibility node; must not
  interfere with typing, scrolling, selection, long-press or pull-down behaviour (line 23).
- **Theme switching without activity recreation** — the gate re-evaluates on every draw.
- **Non-Cybergram presentation draws absolutely nothing** — the upstream path must remain byte-identical in
  behaviour for Day/stock themes.
- Reuse `CybergramBubbleDrawable.buildPath(...)`/`CybergramHudDrawable`; introduce no second chamfer
  implementation.

## Explicit non-goals

Do not:

- modify `SizeNotifierFrameLayout` or any other shared canvas host;
- change wallpaper selection, motion wallpapers, `skipBackgroundDrawing`, blur configuration or the blur
  source pipeline;
- touch `ChatMessageCell`, `MessageDrawable`, `ChatActionCell`, `ReplyMessageLine`,
  `ReactionsLayoutInBubble` or any message renderer;
- add interactivity, hit targets, countdowns, network/security claims or live data microtext;
- overlay the message list (the layer is under the list, not over it);
- animate in a way that competes with typing/scrolling;
- combine this work with B4 reply/reaction styling or any other pass;
- start B8 secondary surfaces here.

## Mandatory re-reconnaissance at execution

Re-run and record, at minimum:

```text
git grep -n "class ChatActivityFragmentView" -- 'TMessagesProj/src/main/java/**/*.java'
git grep -n "extends SizeNotifierFrameLayout" -- 'TMessagesProj/src/main/java/**/*.java'
git grep -n "contentView.addView" -- TMessagesProj/src/main/java/org/telegram/ui/ChatActivity.java
git grep -n "setBackgroundImage\|backgroundView\|getBackgroundImage" -- TMessagesProj/src/main/java/org/telegram/ui/Components/SizeNotifierFrameLayout.java
git grep -n "Cybergram" -- TMessagesProj/src/main/java/org/telegram/ui/ChatActivity.java
git grep -n "CybergramHeaderDecorationView" -- 'TMessagesProj/src/main/java/**/*.java'
git grep -n "void dispatchDraw\|protected void onDraw\|boolean drawChild" -- TMessagesProj/src/main/java/org/telegram/ui/ChatActivity.java
git grep -n "TAG_DRAWING_AS_BACKGROUND" -- 'TMessagesProj/src/main/java/**/*.java'
```

Confirm before writing code: the child-index order at runtime, that `chatListView` is still the first
full-size child above the wallpaper, that the container still overrides `onDraw`/`drawChild`/`dispatchDraw` at
the lines above, and that `drawChild`'s blur branches still treat an unknown child as non-blur content.

## Pre-flight findings (verified 2026-09-15 — documentation only, grants no authorization)

Re-run against `dev` `7cf5b408d`; `TMessagesProj/src/main` is identical to `39c301bb4` / `9f8211503`.
This section answers the two questions the contract above left explicitly open. It is **static**
verification: runtime proof is still required at execution and is not replaced by this.

### 1. Blur capture — resolved for both modes

- The blur capture path never enumerates `contentView` children. `drawList` (17312) delegates to
  `Blur3Utils.captureRelativeParent(this::drawListImpl, …)` (17322), and `drawListImpl` iterates
  **`chatListView`'s** children only (`chatListView.getChildCount()` / `getChildAt(i)`, 17273-17307) and
  calls `chatListView.drawChild(...)` (17305). A HUD that is a sibling of `chatListView` under
  `contentView` is therefore outside the captured subtree by construction.
- When the container itself is drawn as a background/blur source (`BlurBehindDrawable` sets
  `TAG_DRAWING_AS_BACKGROUND`, `BlurBehindDrawable.java:167`), `drawChild` builds `needBlur` from a fixed
  allow-list and returns `false` for every other child (17566-17575):
  - `STATIC_CONTENT` → only `actionBar` (17568-17569);
  - otherwise → only `chatListView`, `chatInputViewsContainer`, `bottomChannelButtonsLayout` (17570-17571).
  A new HUD child is on neither list, so it is skipped in that pass. The contract's expectation is
  **confirmed by code**.
- In the third branch (`blurredView.fullyDrawing()`, 17576-17581) only `actionBar` and `chatListView` are
  skipped; the HUD falls through to normal drawing — visible, and still not captured. Intended result.
- `ChatActivityFragmentView.onDraw` (17539-17547) returns early in both blur branches and otherwise calls
  `super.onDraw`; since it runs below every child it is irrelevant to the HUD's layer.

### 2. Insertion index — resolved; the contract's formula is the fragile variant

`backgroundView` is created lazily **and conditionally**:

- `updateBackground()` (44179) runs early in `createView`, at `ChatActivity.java:4613`, i.e. **before**
  `chatListView` is added at 6979;
- it calls `contentView.setBackgroundImage(Theme.getCachedWallpaper(), …)` (44188);
- `SizeNotifierFrameLayout.setBackgroundImage` (363) returns immediately when
  `backgroundDrawable == bitmap` (364), so a **null** cached wallpaper creates nothing; otherwise it adds
  `backgroundView` at index `0` (367-370) and then calls `onUpdateBackgroundDrawable` (388);
- `getBackgroundImage()` (431) returns `backgroundDrawable` unguarded, so a fresh container always appears
  wallpaper-less and the setter is reached.

So at the moment a HUD is added (near the `CybergramHeaderDecorationView` insert at 8986) the wallpaper
child may or may not exist yet, and plain `indexOfChild(backgroundView) + 1` has no valid index when it
does not. The wallpaper-independent anchor is:

```text
contentView.addView(hudView, contentView.indexOfChild(chatListView),
        LayoutHelper.createFrame(MATCH_PARENT, MATCH_PARENT));
```

which produces the required layer in both cases — wallpaper already present (index 2), and wallpaper
created later at index 0 with the HUD shifting up with it — and matches the existing relative-index
precedent at 17344 and 44375.

### 3. Index-shift consequence that must be proven at execution

Adding one child above the wallpaper shifts every later sibling by one. Existing **hard-coded** indices in
the same container are:

- `videoPlayerContainer` at index `1` (12157) — round-video playback: the later `addView(…, 1)` lands
  below the HUD, so a playing round video would render under the decoration;
- `emptyViewContainer` at index `3` (32523) — the empty-chat state: index `3` no longer denotes the same
  sibling once the HUD exists;
- `topUndoView` at index `17` (11348);
- the relative pattern `1 + indexOfChild(chatListView)` for `thanosEffect` (44375), which this contract
  already flags because `ThanosEffect` casts the parent to `ChatActivityFragmentView` (874-875).

None of these is proven broken. Each must be exercised at execution (round video playing, empty chat, undo
visible, topic switch, blur on/off). If a real conflict appears, the fallback is to insert the HUD as early
as possible — before `invalidateBlurredSourcesView` at 4561, so only the wallpaper insert predates it — or
to re-assert its position from the existing `onUpdateBackgroundDrawable` hook (17222), rather than
restructuring the container.

## Allowed repository changes (if authorized)

- the new decoration-View file under `TMessagesProj/src/main/java/org/telegram/ui/` (or `.../Components/`);
- **one** insertion/wiring change in `ChatActivity.java` (add the view; no behavioural change);
- optionally a debug-only fixture/host in `TMessagesProj_App/src/debug/.../CybergramShowcaseActivity.java` and
  a debug-only control that proves the non-Cybergram path draws nothing.

Debug tooling must stay outside release source/manifests. It must **not** modify saved user theme
preferences; if a control run needs a theme switch, the previous value must be restored and that deviation
disclosed explicitly (see the disclosed deviation recorded for B6).

## Required evidence matrix

| tier | required evidence |
|---|---|
| E | `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64` BUILD SUCCESSFUL; install/launch on AVD `Cybergram_API36`; no FATAL/ANR/process death; screenshot of the chat canvas with the layer; **control run proving the layer draws nothing under a non-Cybergram provider**; wallpaper-variant sweep (gradient, motion, pattern, custom image); scroll/typing/selection/pull-down regression check; touch pass-through check; accessibility check (no node, not focusable); blur-capture check (`drawList` 17312 and `drawChild` 17566-17581 must not smear the HUD into a blur background); scrim/enter-transition check (the HUD must not flicker or survive incorrectly while `drawChild` 17551-17553 suppresses overlay children); preview/embedded host check (`parentThemeDelegate != null`, `isInsideContainer`) |
| A | authenticated chat: real wallpaper + real message history; scroll to both ends; selection/multi-select; emoji/attach panel open; topics/tags if reachable; visual density verdict against `CYBERGRAM_UI_SPEC.md` lines 19/55/115 |
| P | not required unless a device/OEM-specific defect appears |

Never promote E evidence into A or P. Pre-auth E cannot prove authenticated chat-canvas behaviour.

## Stop conditions

Stop and report rather than proceeding if:

- the insertion cannot be made index-deterministic without touching a shared class;
- the HUD cannot avoid being captured into a blur/background pass, or is suppressed/flashes during scrim and
  message-enter transitions, without invasive `drawChild` changes;
- the layer would need `SizeNotifierFrameLayout` changes to coexist with blur/wallpaper;
- the visual result cannot stay inside edge space without competing with messages;
- any non-Cybergram path changes;
- the work starts pulling in message renderers, reactions, replies or service cells.

## Success condition

B7 succeeds when:

- the layer is a dedicated, non-interactive, runtime-gated Cybergram component;
- nothing is drawn outside Cybergram presentation;
- wallpaper, blur, scroll, typing, selection and accessibility behaviour are provably unchanged;
- the change footprint is the new component plus one wiring edit in `ChatActivity.java`;
- E evidence exists, A tier is either closed or explicitly recorded as pending with the reason.

## Relationship to other open work

Recorded here so a future executor does not mistake this prepared contract for the current priority:

- **A-tier verification debt is the largest outstanding item.** B0 (filter tabs), B1 (main tabs) and B6
  (service/date, incl. rich states) are all integrated at E tier with **A pending**, and B2 carries 15
  `UNTESTED` account-dependent rows. B7 adds no A-coverage and should not be used to avoid that debt.
- **B4 remains `DESIGN-OPEN / NOT AUTHORIZED`** (reply/reaction styling). B7 changes nothing about B4.
- **B8 (secondary surfaces / Stage F) remains deferred.**
- **R1 carries an open release/install-compatibility item** (Redmi Note 10S / MIUI 14.0.4), which needs the
  exact installer error text and is unrelated to B7.

The user chooses which of these is executed first; this document authorizes none of them.
