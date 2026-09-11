# Cybergram remaining UI architecture — 2026-09-11

Status: static reconnaissance supporting `docs/EXECUTION_BACKLOG.md`.

This document records source ownership and narrow presentation seams for the highest-value remaining Cybergram surfaces. It is evidence for planning, not runtime validation and not design authority.

Source/product basis: Cybergram product cut `52b8e219729d0a90dd3335165cf4ef44acf46e5e`, with later repository-only documentation commits on `dev`.

Read `AGENTS.md`, `docs/CURRENT_STATE.md`, `docs/CYBERGRAM_UI_SPEC.md` and `docs/EXECUTION_BACKLOG.md` before acting on this material.

## 1. Bottom navigation ownership

The remaining bottom navigation is not owned by `DialogsActivity`. It is split across three behavioural/rendering layers plus one glass-colour provider.

### `MainTabsActivity`

File:

`TMessagesProj/src/main/java/org/telegram/ui/MainTabsActivity.java`

Observed ownership:

- creates `MainTabsLayout`;
- creates five `GlassTabView` instances for Chats, Contacts, Settings, Calls and Profile;
- maps the five concrete views onto four tab positions because Calls and Settings share the same position;
- owns click and long-click routing;
- coordinates tab selection/reselection with the ViewPager;
- owns Calls/Settings visibility switching;
- owns `tabsViewWrapper`, bottom fade and window/navigation-bar inset placement;
- creates and stores the `BlurredBackgroundDrawable` used as the visible main-tabs glass background;
- owns the render-node/color blur source lifecycle;
- updates tab/glass colours on theme changes;
- drives badge/counter updates and profile avatar state.

Presentation conclusion:

The Cybergram seam belongs at the point where the visible `tabsView` background is chosen. The existing blur drawable/source may continue to exist for non-Cybergram themes and theme switching. Do not globally disable the blur system or remove its lifecycle simply because Cybergram should not display a glass capsule.

### `MainTabsLayout`

File:

`TMessagesProj/src/main/java/org/telegram/ui/MainTabsLayout.java`

Observed ownership:

- responsive tab measurement and width allocation;
- animated child visibility;
- visual-width interpolation;
- selected-tab propagation to `GlassTabView`;
- long-press gesture capture and drag-across-tabs selection;
- hit testing and click suppression;
- spring/scale state;
- custom long-press selector.

The custom long-press selector is drawn in `dispatchDraw(...)` as a rounded rectangle using bounds already computed from the active tab/drag position.

Presentation conclusion:

Do not touch measurement or gesture code. A Cybergram branch may reuse the existing computed selector bounds and substitute only the selector shape with the shared chamfered path/HUD primitive. The original `drawRoundRect` remains the non-Cybergram path.

### `GlassTabView`

File:

`TMessagesProj/src/main/java/org/telegram/ui/Components/glass/GlassTabView.java`

Observed ownership:

- normal per-tab selected state;
- selected-state animation factor;
- icon/Lottie state;
- labels/typeface transition;
- counters/badges;
- profile avatar presentation.

The normal selected plate is independently drawn by each `GlassTabView` as a scaled rounded plate.

Presentation conclusion:

The Cybergram pass should substitute only this plate shape/surface while keeping the existing selected factor, scale animation, icons, counters and avatar state. Counters and the profile avatar need not be angularized in the first pass.

### `BlurredBackgroundProviderImpl.mainTabs(...)`

File:

`TMessagesProj/src/main/java/org/telegram/ui/Components/blur3/drawable/color/impl/BlurredBackgroundProviderImpl.java`

Observed ownership:

`mainTabs(...)` supplies glass background colour, top/bottom stroke colours, shadow parameters and stroke width. It is a colour/effect provider, not the navigation state machine and not the correct location for Cybergram geometry.

Presentation conclusion:

Do not modify this shared provider for the first Cybergram bottom-navigation pass. Keep upstream glass configuration intact for normal themes. Select a Cybergram-visible background at the `MainTabsActivity` presentation seam instead.

## 2. Filter/folder tabs as precedent

File:

`TMessagesProj/src/main/java/org/telegram/ui/Components/FilterTabsView.java`

The final landed Cybergram implementation establishes a useful pattern for shared Telegram components:

- gate on `CybergramTheme.isCybergramPresentation(resourcesProvider)`;
- retain the upstream rounded selector and blurred background object;
- create Cybergram-specific `CybergramHudDrawable` panel/selector instances;
- draw the Cybergram panel only in the Cybergram branch;
- suppress the visible blurred background only in that branch;
- use `CybergramBubbleDrawable.buildPath(...)` for angular clipping;
- leave scrolling, reorder/edit state, delegates and animation logic intact;
- retain the exact upstream rounded/blurred path when Cybergram presentation is inactive.

This pattern should be copied conceptually, not mechanically, into bottom navigation.

## 3. Message-body ownership

### `MessageDrawable`

File:

`TMessagesProj/src/main/java/org/telegram/ui/ActionBar/MessageDrawable.java`

Observed ownership:

- base body path/drawable;
- incoming/outgoing and selected body colours;
- grouped top/bottom-near geometry state;
- shadows/gradients/nine-patch caching;
- three drawable types: `TYPE_TEXT`, `TYPE_MEDIA`, `TYPE_PREVIEW`.

Current Cybergram implementation/history establishes angular geometry for text/media paths and deliberately leaves `TYPE_PREVIEW` outside that initial geometry pass.

Important distinction:

`TYPE_PREVIEW` also has special theme and density/scaling behaviour. Its colour access bypasses the instance `ResourcesProvider` in favour of global Theme values, and its `dp(...)` path uses preview-specific scaling. Therefore it must not be angularized solely for conceptual symmetry. First identify its actual product surfaces.

### `ReplyMessageLine`

File:

`TMessagesProj/src/main/java/org/telegram/ui/Components/ReplyMessageLine.java`

This class owns reply-line/background paths, paints, animated colours, peer-colour resolution, loading state and emoji/sticker decoration. Reply visuals are not simply part of the main `MessageDrawable` polygon.

Conclusion:

Any reply mismatch must be assigned here (or to a proven caller/layout owner) before a production fix is written.

### `ReactionsLayoutInBubble`

File:

`TMessagesProj/src/main/java/org/telegram/ui/Components/Reactions/ReactionsLayoutInBubble.java`

This class owns reaction-button layout, drawing, counters, selected state, animations and touch handling.

Conclusion:

Reaction geometry/state work requires its own bounded pass. Do not bury it inside a generic `ChatMessageCell` restyle.

## 4. Service/date ownership

Primary file:

`TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatActionCell.java`

Static reconnaissance confirms that ordinary service/date presentation has a narrower seam than the overall size of `ChatActionCell` suggests.

### Date path

`setCustomDate(...)` formats the date/scheduled-date label, stores it as `customText` and routes it through `updateTextInternal(...)`. The date therefore participates in the same text-layout/background pipeline as ordinary simple service text rather than requiring a separate date-only renderer.

### Ordinary background path

`drawBackground(Canvas, boolean)` selects:

- `Theme.key_paint_chatActionBackground`;
- `Theme.key_paint_chatActionBackgroundDarken`;
- `Theme.key_paint_chatActionText`.

When `invalidatePath` is set, the cell rebuilds `backgroundPath` from the current text layout. The upstream path follows line widths and line heights and uses rounded outer/inner arc segments. It is then drawn through `canvas.drawPath(backgroundPath, backgroundPaint)` with the optional service-gradient darkening/dim layers drawn through the same path.

This is the concrete owner for the ordinary multi-line service/date bubble silhouette.

### Rich/special states are separate

`ChatActionCell` also contains many unrelated rich states: premium/star gifts, offers, wallpaper actions, community changes, buttons, stickers/images, reactions, ribbons and other special cards.

Several of those paths explicitly construct their own `backgroundPath2`, round-rect button/card paths, ribbons or other geometry after/beside the ordinary background path.

Conclusion:

A Cybergram plain service/date pass can target only the ordinary `backgroundPath` generation/drawing branch. It must not globally clip the `ChatActionCell` canvas and must not replace rich-card/button paths.

### Candidate narrow implementation seam

A later production pass should:

- add the central Cybergram presentation gate to `ChatActionCell`;
- preserve the existing text measurement, line-width calculations and background bounds;
- replace only the ordinary rounded path construction with a Cybergram-specific chamfered path strategy;
- preserve the same background/darken/dim paint pipeline;
- leave rich/special card/button/ribbon paths upstream;
- verify single-line date, multi-line ordinary service text and representative rich actions separately.

The exact multi-line angular strategy still needs implementation design. A single enclosing rectangle would change the current line-hugging silhouette and should not be introduced accidentally. Either reproduce the line-following outline with chamfered transitions or explicitly decide that Cybergram service plates use one compact enclosing plate after visual evidence.

## 5. Architectural rules derived from the remaining surfaces

The current Cybergram codebase now has a consistent safe pattern:

1. presentation detection stays central in `CybergramTheme`;
2. shared Telegram state machines remain upstream-owned;
3. Cybergram geometry uses shared project primitives;
4. non-Cybergram presentation remains present and testable;
5. complex surfaces are split by actual visual owner before implementation;
6. validation evidence belongs to the exact tested revision and is never inherited automatically from older builds.

The high-risk failure mode to avoid is an omnibus restyle of `MainTabsActivity`, `ChatMessageCell` or `ChatActionCell`. Their size is evidence that scope should become narrower, not permission to refactor more of them.

## 6. Relationship to execution backlog

`docs/EXECUTION_BACKLOG.md` contains the executable passes. This architecture note supports those pass boundaries:

- `B0`: final FilterTabs validation;
- `B1`: bottom-navigation presentation through `MainTabsActivity` + `MainTabsLayout` + `GlassTabView`, with `BlurredBackgroundProviderImpl` intentionally left upstream;
- `B2`: message-state evidence/ownership matrix before production corrections;
- `B3/B4`: conditional owner-specific message fixes;
- `B5/B6`: service/date work, now statically narrowed to the ordinary `ChatActionCell.backgroundPath` pipeline while rich action geometry remains out of scope.

This reconnaissance is static repository evidence only. It does not claim any new Android build or runtime validation.