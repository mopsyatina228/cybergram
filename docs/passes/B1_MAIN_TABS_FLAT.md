# B1 — Cybergram flat/angular main tabs

Status: DESIGN-READY, EXECUTE AFTER B0 OR EXPLICIT PRIORITY OVERRIDE

Type: production presentation change

Suggested branch: `feature/cybergram-main-tabs-flat`

Risk: HIGH

Design authority: `docs/CYBERGRAM_UI_SPEC.md`

Planning authority: `docs/EXECUTION_BACKLOG.md`

Static ownership evidence: `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md`

## Mission

Remove the remaining large rounded/glass bottom-navigation panel and selected pills in Cybergram while preserving Telegram's complete tab/ViewPager/gesture/layout behaviour and preserving the exact upstream presentation for non-Cybergram themes.

This is a presentation seam, not a bottom-navigation rewrite.

## Startup

1. Fetch `dev` and `master`.
2. Read `AGENTS.md`, `docs/CURRENT_STATE.md`, `docs/EXECUTION_BACKLOG.md`, `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md` and the dialog-list section of `docs/CYBERGRAM_UI_SPEC.md`.
3. Record current `dev` SHA and local `git status`.
4. Branch from fresh current `dev` as `feature/cybergram-main-tabs-flat` unless an explicit human instruction says otherwise.
5. Do not reset/discard unknown local work.

## Allowed production scope

Primary files only:

- `TMessagesProj/src/main/java/org/telegram/ui/MainTabsActivity.java`
- `TMessagesProj/src/main/java/org/telegram/ui/MainTabsLayout.java`
- `TMessagesProj/src/main/java/org/telegram/ui/Components/glass/GlassTabView.java`

`TMessagesProj/src/main/java/org/telegram/ui/ActionBar/CybergramTheme.java` may be changed only if a genuinely shared semantic constant is required and no existing constant fits.

Do NOT modify `BlurredBackgroundProviderImpl.mainTabs(...)` in this first pass. It is upstream glass/effect configuration and should remain available to non-Cybergram presentation.

Any other production file requires stopping and reporting why it is necessary before broadening scope.

Debug showcase/docs may be changed only when needed for validation/evidence and must not become a substitute for real-device testing of the actual main tabs.

## Mandatory activation seam

Use only:

`CybergramTheme.isCybergramPresentation(resourcesProvider)`

Do not add direct `Theme.getCurrentTheme().getName()` checks and do not infer Cybergram from colours.

Reuse `CybergramHudDrawable` or `CybergramBubbleDrawable.buildPath(...)`. Do not create a third independent angular geometry implementation.

## `MainTabsActivity` change boundary

Preserve without semantic changes:

- five concrete tab views and four logical positions;
- Chats/Contacts/Settings/Calls/Profile mapping;
- Calls/Settings visibility swap;
- click and long-click actions;
- ViewPager movement and manual-scroll guards;
- reselect-current-tab scroll-to-top behaviour;
- notification/badge/profile update logic;
- blur source lifecycle and source rendering;
- `tabsViewWrapper` layout;
- update layout offsets;
- system/navigation-bar insets;
- tabs visible/hidden animation.

Introduce the smallest presentation seam that chooses the visible `tabsView` background:

- non-Cybergram: existing `tabsViewBackground` blurred drawable;
- Cybergram: dark angular HUD panel using existing Cybergram panel/cyan/corner-cut language.

A helper such as `applyTabsPresentationBackground()` is acceptable if it simply selects between those two existing presentation objects and is called at creation/theme-colour update.

The upstream blurred drawable may still be created/stored when Cybergram is active so that normal themes/theme switching remain intact. Do not delete the blur subsystem.

Leave the separate bottom `fadeView` unchanged on the first implementation. Only change it if device evidence proves it remains visibly incompatible after the tabs panel is flattened. Such a change must stay Cybergram-only and preserve fade layout/visibility semantics.

## `MainTabsLayout` change boundary

Do not alter:

- measurement/weighting;
- visible-child calculation;
- animated visibility;
- hit testing;
- ClickHelper/long-press semantics;
- drag-across-tabs selection;
- `performClick()` routing;
- spring values/state;
- scaling/layer-type behaviour.

Only the custom long-press selector is in scope.

Today it uses existing computed bounds and draws a full rounded rectangle. Under Cybergram, draw a chamfered Cybergram selected plate using the same bounds/animation state. Under non-Cybergram, preserve the exact existing `drawRoundRect` path.

## `GlassTabView` change boundary

Do not alter:

- icon/Lottie setup;
- selected animator timing/factor;
- click behaviour;
- tab measurement/visual width;
- counter contents/logic;
- avatar loading/state;
- semantic selected/unselected colours.

Only the normal selected plate is in scope.

Use the existing selected factor, scale and rectangle, but render an angular Cybergram plate when the central gate is active. Keep upstream rounded rendering otherwise.

Keep counters/badges rounded in B1. Keep profile avatar round. No typography redesign in B1.

## Visual target

Outer tabs panel:

- fill: existing Cybergram `PANEL` family;
- restrained cyan outline using existing 1dp-ish project language;
- chamfer compatible with `BUBBLE_CORNER_CUT_DP`;
- no large translucent/glass capsule.

Selected tab plate:

- `PANEL_RAISED`-family dark fill;
- restrained cyan outline/accent;
- same animation/bounds as upstream selection;
- no opaque cyan block.

No decorative microtext and no fake security labels. Red/amber embellishment is out of scope unless needed for an existing semantic state.

## Explicit non-goals

Do NOT:

- rewrite tab navigation;
- replace `ViewPagerActivity` behaviour;
- remove blur3 globally;
- change tab count/order;
- redesign counters;
- angularize profile avatar;
- restyle secondary bottom sheets/menus;
- alter app update or inset logic;
- combine this with FilterTabs, message, typography or release work.

## Stop conditions

Stop and report before continuing if:

- desired presentation requires changing hit targets or measurements;
- ViewPager or Calls/Settings state would need behavioural changes;
- the non-Cybergram branch cannot remain functionally/presentationally upstream;
- more shared production files appear necessary;
- a supposed visual change begins requiring counter/avatar/state-machine rewrites.

## Static acceptance before build

Require:

- `git diff --check` passes;
- changed production files stay within allowed scope;
- Cybergram checks are central-gate calls, not theme-name/color heuristics;
- non-Cybergram `drawRoundRect`/blur background paths remain present;
- no unrelated cleanup/refactor appears in diff.

## Build/device acceptance

Build the established afat arm64 debug variant when the machine supports it and record exact evidence.

Exercise on the real app:

- Chats -> Contacts -> Settings -> Profile;
- Calls tab enabled and disabled, including Settings/Calls position swap;
- current-tab reselect and scroll-to-top;
- long press on tabs;
- long-drag selection across tabs;
- badge/counter cases available on device;
- profile avatar;
- tabs hide/show animation;
- navigation-bar/inset placement;
- configuration/orientation change where practical;
- Cybergram shows angular dark panel/selected plate and no large glass capsule;
- non-Cybergram theme still shows upstream glass/rounded tabs;
- no FATAL/ANR.

Static-only review is not sufficient for integration.

## Required handoff report

Return:

- base SHA and branch name;
- final commit SHA;
- exact changed files;
- concise diff rationale per file;
- `git diff --check` result;
- build command/result;
- APK evidence if built;
- device/OS/package;
- interaction matrix result;
- screenshot/artifact paths;
- FATAL/ANR result;
- unresolved visual defects;
- explicit statement whether B1 is ready to integrate into `dev`.