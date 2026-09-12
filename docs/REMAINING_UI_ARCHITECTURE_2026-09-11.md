# Cybergram remaining UI architecture

Last reconciled: 2026-09-12

Status: static reconnaissance supporting `docs/EXECUTION_BACKLOG.md`.

This document records source ownership and safe presentation seams for the highest-value remaining Cybergram surfaces. It is planning evidence, not runtime validation and not design authority.

Product basis: Cybergram product cut `52b8e219729d0a90dd3335165cf4ef44acf46e5e`; later `dev` commits through the 2026-09-12 reconciliation are documentation/validation planning unless explicitly stated otherwise.

Read `AGENTS.md`, `docs/CURRENT_STATE.md`, `docs/CYBERGRAM_UI_SPEC.md` and `docs/EXECUTION_BACKLOG.md` before acting on this material.

## 1. Bottom navigation ownership

The main bottom navigation is active root UI for authenticated clients. `LaunchActivity` creates `MainTabsActivity` when the current account is activated.

The surface is split across three rendering/behaviour layers. `MainTabsLayout` and `GlassTabView` were still byte-identical to upstream `master` at the preserved product cut, so B1 is their first Cybergram-specific presentation seam.

### `MainTabsActivity`

File:

`TMessagesProj/src/main/java/org/telegram/ui/MainTabsActivity.java`

Observed ownership:

- creates `MainTabsLayout`;
- creates five concrete `GlassTabView` instances for Chats, Contacts, Settings, Calls and Profile;
- maps five views onto four logical tab positions because Calls and Settings share a position;
- owns click/long-click routing and ViewPager selection/reselection;
- owns Calls/Settings visibility switching;
- owns `tabsViewWrapper`, bottom fade and window/navigation-bar inset placement;
- creates/stores the `BlurredBackgroundDrawable` used as the visible main-tabs glass background;
- owns render-node/color blur-source lifecycle;
- updates badges/counters/profile state.

Presentation seam:

Choose the visible `tabsView` background here. Cybergram can display a `CybergramHudDrawable` dark/angular panel while the existing upstream blur drawable remains constructed/stored for non-Cybergram themes and theme switching. Do not globally disable blur3.

### `MainTabsLayout`

File:

`TMessagesProj/src/main/java/org/telegram/ui/MainTabsLayout.java`

Observed ownership:

- responsive text measurement and tab-width allocation;
- animated child visibility;
- visual-width interpolation;
- selected-tab propagation;
- long-press capture and drag-across-tabs selection;
- hit testing / click suppression;
- spring/scale state;
- special selector used while long-press dragging.

The long-press selector is currently a rounded rectangle drawn from already-computed animated bounds.

Presentation seam:

Do not touch measurement or gesture/state code. Under Cybergram only, render a chamfered selector using the same computed bounds. Preserve the existing rounded branch otherwise.

### `GlassTabView`

File:

`TMessagesProj/src/main/java/org/telegram/ui/Components/glass/GlassTabView.java`

Observed ownership:

- normal selected-state plate and selection factor;
- icon/Lottie state;
- label/typeface transition;
- counters/badges;
- profile avatar presentation.

The normal selected plate is drawn per tab as a scaled rounded rectangle.

Important sharing boundary:

`GlassTabView` is **not main-tabs-only**. The same class also constructs attachment and attachment-bot tabs through `createAttachTab(...)` / `createAttachBotTab(...)`.

Therefore a branch based only on `CybergramTheme.isCybergramPresentation(resourcesProvider)` would incorrectly change unrelated attach/bot-tab geometry whenever Cybergram is active.

Required presentation model for B1:

- add a presentation-only explicit main-tabs opt-in, default false;
- set it only on the five instances created by `MainTabsActivity`;
- draw the angular selected plate only when explicit opt-in **and** central Cybergram presentation are both true;
- leave attach/bot tabs on the upstream rounded path;
- keep counters/badges and profile avatar rounded in B1.

This is a general rule for future Cybergram work: the global theme gate says whether Cybergram is active; an explicit local opt-in is additionally required when the shared component serves unrelated surfaces.

### `BlurredBackgroundProviderImpl.mainTabs(...)`

File:

`TMessagesProj/src/main/java/org/telegram/ui/Components/blur3/drawable/color/impl/BlurredBackgroundProviderImpl.java`

This supplies glass colours/strokes/shadow parameters. It is not the navigation state machine and not the right place for Cybergram geometry.

Do not modify it in B1. Keep upstream glass configuration intact for normal themes.

### Existing palette bridge

`cybergram.attheme` already defines dark/cyan values for:

- `glass_targetMainTabs`;
- `glass_tabSelected`;
- `glass_tabSelectedText`;
- `glass_tabUnselected`.

Those keys deliberately darken the inherited glass system while geometry remains upstream. B1 therefore primarily replaces shape/effect ownership; it should not invent a second bottom-navigation palette.

## 2. Filter/folder tabs precedent

File:

`TMessagesProj/src/main/java/org/telegram/ui/Components/FilterTabsView.java`

The landed Cybergram implementation is the reference pattern for shared Telegram presentation seams:

- central Cybergram gate;
- upstream rounded selector/blur objects remain present;
- Cybergram-specific HUD panel/selected plate/angular clip;
- blurred background is visually suppressed only in Cybergram;
- scrolling/reorder/edit/delegate machinery remains untouched;
- exact upstream presentation remains available when Cybergram is inactive.

The 2026-09-11 x86_64 emulator run proves the current product tree still builds/installs/starts, but it did not reach authenticated `FilterTabsView`. B0 remains a surface-specific authenticated validation task, not a generic build task.

## 3. Message-body ownership

### `MessageDrawable`

File:

`TMessagesProj/src/main/java/org/telegram/ui/ActionBar/MessageDrawable.java`

Observed ownership:

- base message-body path/drawable;
- incoming/outgoing/selected body colours;
- grouped top/bottom-near geometry;
- shadows/gradients/nine-patch caching;
- drawable types `TYPE_TEXT`, `TYPE_MEDIA`, `TYPE_PREVIEW`.

Current Cybergram geometry intentionally covers `TYPE_TEXT` and `TYPE_MEDIA`. Both the angular path branch and Cybergram border branch exclude `TYPE_PREVIEW`.

`TYPE_PREVIEW` also has special theme/density behaviour: its colour path uses global Theme access and its `dp(...)` scaling is preview-specific. Do not angularize it merely for symmetry. B2 must identify its actual callers/product surface first.

### `ReplyMessageLine`

File:

`TMessagesProj/src/main/java/org/telegram/ui/Components/ReplyMessageLine.java`

Owns reply-line/background paths, paints, peer-colour resolution, animation/loading state and related decoration. Reply visuals are not simply part of the `MessageDrawable` polygon.

### `ReactionsLayoutInBubble`

File:

`TMessagesProj/src/main/java/org/telegram/ui/Components/Reactions/ReactionsLayoutInBubble.java`

Owns reaction-button layout/drawing, counters, selected state, animations and touch behaviour.

Conclusion:

Do not create a broad “fix ChatMessageCell” pass. B2 first assigns each mismatch to its real owner. B3/B4 are generated only from observed defects.

## 4. Service/date ownership

Primary file:

`TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatActionCell.java`

`ChatActionCell` is a large mixed-purpose class. It contains ordinary service/date labels and many rich/special actions, gifts, buttons, images, reactions and cards.

### Plain date/service path

`setCustomDate(...)` formats the date/scheduled-date text and routes it through the same text/background pipeline used by ordinary service text.

The ordinary background is rebuilt from text line widths/heights into `backgroundPath`. The upstream outline is line-following and uses rounded outer/inner `arcTo(...)` segments (including the familiar `corner=11dp` / inner-corner calculations), then the same path is drawn with service background, optional darken/gradient and dim paints.

This is the narrow owner for ordinary service/date silhouette geometry.

### Rich/special states

Several special layouts construct their own secondary paths/rounded cards/buttons/ribbons. They are not safe collateral for a simple service/date restyle.

Required B5/B6 boundary:

- audit and identify ordinary versus rich states first;
- preserve existing text measurement and bounds;
- choose an explicit angular strategy for ordinary single-line and multi-line service/date backgrounds;
- change only the ordinary background-path construction/drawing in B6;
- preserve the existing background/darken/dim paint pipeline;
- leave rich/special card/button/ribbon geometry upstream unless separately authorized.

A single enclosing angular rectangle would simplify the upstream line-following silhouette and must be an explicit visual decision, not an accidental implementation shortcut.

## 5. Debug/emulator validation architecture

`docs/runbooks/CYBERGRAM_EMULATOR_VALIDATION.md` establishes API 36 `x86_64` AVD validation as the default remote machine baseline.

The existing DEBUG `CybergramShowcaseActivity` can be extended for deterministic rendering cases that do not require an account. This is especially useful for B1/B2/B5 visual primitives.

However, a debug fixture validates drawing/compilation only. It must not be confused with an authenticated production-surface test of `MainTabsActivity`, `FilterTabsView` or real message/state data.

Validation tiers and their current meaning are maintained in `docs/EXECUTION_BACKLOG.md`.

## 6. Derived architectural rules

1. Presentation detection stays central in `CybergramTheme`.
2. Shared Telegram state machines remain upstream-owned.
3. Cybergram geometry reuses project-owned primitives.
4. Non-Cybergram presentation remains present and testable.
5. A globally shared component needs local opt-in when only one of its surfaces is being restyled.
6. Complex visual work is split by actual renderer/owner before implementation.
7. Emulator evidence, authenticated-surface evidence and physical-device/OEM evidence are separate claims.
8. A large upstream class is a reason to narrow scope, not permission to refactor it.

## 7. Backlog relationship

- B0: authenticated final FilterTabs validation; generic E/build baseline already exists.
- B1: main bottom navigation through `MainTabsActivity` + `MainTabsLayout` + explicitly opted-in main-tab `GlassTabView` instances.
- B2: message-state evidence/ownership matrix.
- B3/B4: conditional message-owner fixes derived only from B2.
- B5: service/date audit and geometry decision.
- B6: conditional ordinary `ChatActionCell.backgroundPath` implementation derived only from B5.
- B7: optional later chat-canvas HUD.
- B8: secondary screens/onboarding after primary flow is coherent.

This document is static source evidence. Exact execution status belongs in `docs/EXECUTION_BACKLOG.md`.
