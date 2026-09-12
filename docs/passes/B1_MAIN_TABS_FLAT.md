# B1 — Cybergram flat/angular main tabs

Status: DESIGN-READY / EXECUTION NOT STARTED

Type: production presentation change

Suggested branch: `feature/cybergram-main-tabs-flat`

Risk: HIGH

Design authority: `docs/CYBERGRAM_UI_SPEC.md`

Planning/status authority: `docs/EXECUTION_BACKLOG.md`

Static ownership evidence: `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md`

Validation runbook: `docs/runbooks/CYBERGRAM_EMULATOR_VALIDATION.md`

## Mission

Remove the remaining large rounded/glass bottom-navigation panel and selected pills in Cybergram while preserving Telegram's complete tab/ViewPager/gesture/layout behaviour and preserving upstream presentation for non-Cybergram themes.

This is a presentation seam, not a bottom-navigation rewrite.

## Fresh architecture facts

The production bottom navigation is active root UI: an activated client enters `MainTabsActivity` from `LaunchActivity`.

Ownership is split across:

- `MainTabsActivity`: tab instances, ViewPager coordination, visible outer glass background, fade/insets/wrappers, badges and Calls/Settings switching;
- `MainTabsLayout`: measurement, animated visibility, long-press/drag selection and its custom selector;
- `GlassTabView`: normal selected plate, icon/Lottie state, labels, counters and profile avatar.

At the preserved product cut, `MainTabsLayout` and `GlassTabView` are still upstream-identical. B1 therefore adds the first Cybergram seam there and must keep it narrow.

Critical shared-component fact: `GlassTabView` is also used by attach/bot tabs (`createAttachTab`, `createAttachBotTab`). Therefore **the central Cybergram theme gate alone is not sufficient inside `GlassTabView`**. A global Cybergram branch would leak main-navigation geometry into unrelated Telegram surfaces.

## Startup

1. Fetch `dev` and `master`.
2. Read `AGENTS.md`, `docs/CURRENT_STATE.md`, `docs/EXECUTION_BACKLOG.md`, `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md`, this file and the dialog-list/bottom-navigation language in `docs/CYBERGRAM_UI_SPEC.md`.
3. Record current `dev` SHA and local `git status`.
4. Branch from fresh current `dev` as `feature/cybergram-main-tabs-flat` unless explicit human instruction says otherwise.
5. Do not reset/discard unknown local work.

## Allowed production scope

Primary files only:

- `TMessagesProj/src/main/java/org/telegram/ui/MainTabsActivity.java`
- `TMessagesProj/src/main/java/org/telegram/ui/MainTabsLayout.java`
- `TMessagesProj/src/main/java/org/telegram/ui/Components/glass/GlassTabView.java`

`TMessagesProj/src/main/java/org/telegram/ui/ActionBar/CybergramTheme.java` may be changed only if a genuinely shared semantic constant is required and no existing constant fits.

Do **not** modify `BlurredBackgroundProviderImpl.mainTabs(...)` in this pass. It is shared upstream glass/effect configuration and must remain available to normal themes.

Debug-only showcase/harness code may be changed for E validation, but it must remain in the debug source set and must not alter release behaviour.

Any other production file requires stopping and reporting why it is necessary before scope expands.

## Activation model

Use the central presentation gate:

`CybergramTheme.isCybergramPresentation(resourcesProvider)`

For `MainTabsActivity` and `MainTabsLayout`, that gate is sufficient because those classes own the main-tabs surface.

For `GlassTabView`, require **both**:

1. central Cybergram presentation is active; and
2. the individual `GlassTabView` instance has been explicitly opted into main-tabs Cybergram presentation.

Implement the opt-in as a small presentation-only field/setter with an upstream-safe default, for example conceptually:

`setCybergramMainTabsPresentation(boolean enabled)`

The exact name may differ, but these semantics are mandatory:

- default `false`;
- no inference from text/icon/tab animation;
- only the five instances created by `MainTabsActivity` are opted in;
- attach/bot tabs remain on their upstream selector path even while Cybergram is active;
- opt-in changes drawing only, not measurement/hit targets/state.

Do not add direct theme-name checks and do not infer Cybergram from colours.

Reuse `CybergramHudDrawable` and/or `CybergramBubbleDrawable.buildPath(...)`. Do not create another independent angular geometry implementation.

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
- update-layout offsets;
- system/navigation-bar insets;
- tabs visible/hidden animation.

Required presentation seam:

- retain creation/storage of the upstream `tabsViewBackground` blurred drawable;
- non-Cybergram: assign the existing upstream blurred background exactly as today;
- Cybergram: assign a dark angular `CybergramHudDrawable` panel instead;
- re-evaluate/assign the visible presentation when theme colours/presentation update, so switching away from Cybergram restores upstream glass without recreating navigation state.

A small `applyTabsPresentationBackground()`-style helper is preferred over scattered branches.

Opt the five main-tab `GlassTabView` instances into their main-tabs-only Cybergram presentation immediately after creation or in one obvious local block. Do not modify the `GlassTabView.createAttach*` factories.

Leave the separate bottom `fadeView` unchanged on the first implementation. Only alter it if runtime visual evidence proves it remains visibly incompatible after the main panel is flattened. Any such follow-up must remain Cybergram-only and preserve its layout/visibility role.

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

Today it uses the already-computed selector bounds and draws a full rounded rectangle. Under Cybergram, use those exact bounds/animation values and draw a chamfered selected plate. Under non-Cybergram, preserve the existing `drawRoundRect` branch.

Do not change selector bounds to make the new shape fit. Geometry adapts to the established bounds, not vice versa.

## `GlassTabView` change boundary

Do not alter:

- icon/Lottie setup or animation resources;
- selected animator timing/factor;
- click behaviour;
- tab measurement/visual width;
- counter contents/logic;
- avatar loading/state;
- semantic selected/unselected colours;
- attach/bot tab presentation.

Only the normal selected plate of explicitly opted-in main-tab instances is in scope.

Use the existing `selectedFactor`, scale transform and rectangle. When explicit main-tab opt-in **and** Cybergram presentation are both true, draw a chamfered dark/cyan plate. Otherwise execute the existing rounded path.

Keep counters/badges rounded in B1. Keep profile avatar round. No typography redesign in B1.

## Visual target

Outer tabs panel:

- dark `CybergramTheme.PANEL` family fill;
- restrained cyan outline in the existing ~1dp project language;
- 6dp-family chamfer compatible with existing Cybergram geometry;
- no large translucent/glass capsule.

Selected tab plate:

- dark `PANEL_RAISED`-family fill;
- restrained cyan outline/accent;
- same animation factor/scale/bounds as upstream selection;
- no opaque cyan block.

Existing theme keys continue to control icon/text selected/unselected colour state. The `.attheme` already supplies dark/cyan `glass_*` keys; B1 is principally a geometry/effect seam, not a palette rewrite.

## Explicit non-goals

Do not:

- rewrite tab navigation or `ViewPagerActivity` behaviour;
- remove blur3 globally;
- change tab count/order;
- redesign counters;
- angularize the profile avatar;
- restyle attach/bot tabs;
- restyle secondary bottom sheets/menus;
- alter app-update or inset logic;
- combine this with FilterTabs, message, typography or release work.

## Stop conditions

Stop and report before continuing if:

- desired presentation requires changing hit targets or measurements;
- ViewPager or Calls/Settings state would need behavioural changes;
- non-Cybergram rendering cannot remain on the upstream path;
- the `GlassTabView` change cannot be contained by explicit opt-in;
- more shared production files appear necessary;
- a visual change begins requiring counter/avatar/state-machine rewrites.

## Static acceptance

Require:

- `git diff --check` passes;
- changed production files remain inside allowed scope;
- all Cybergram detection uses the central gate;
- `GlassTabView` main-tabs styling additionally requires explicit instance opt-in;
- attach/bot factories are not opted in;
- non-Cybergram `drawRoundRect` and upstream blur-background paths remain present;
- no unrelated cleanup/refactor appears in diff.

## Validation tiers

### E — emulator, required before handoff

Use `Cybergram_API36` / API 36 `x86_64` when available and the emulator runbook.

Build at least:

`:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64`

Install/update `org.telegram.messenger.beta`, launch normally, check logcat for FATAL/ANR and record exact APK evidence.

Because an unauthenticated emulator cannot reach production `MainTabsActivity`, E validation may also extend the existing DEBUG showcase or add a DEBUG-only fixture that exercises the opted-in `GlassTabView` selected geometry and `MainTabsLayout` long-selector geometry. Such a fixture is evidence for drawing/compilation only; it is not A validation of the actual root navigation state machine.

### A — authenticated UI, required before B1 is called functionally complete

On an authenticated emulator or physical device exercise the actual `MainTabsActivity`:

- Chats -> Contacts -> Settings -> Profile;
- Calls tab enabled/disabled and Settings/Calls position swap;
- current-tab reselect and scroll-to-top;
- long press and long-drag selection across tabs;
- badge/counter cases available;
- profile avatar;
- tabs show/hide animation;
- navigation-bar/inset placement;
- configuration/orientation change where practical;
- Cybergram: angular dark panel/selected plate, no large glass capsule;
- non-Cybergram theme: upstream glass/rounded tabs remain intact;
- attach/bot tabs: retain upstream selector geometry;
- no FATAL/ANR.

### P — physical-device confidence

Repeat a representative main-tabs smoke on Samsung/other physical hardware before release confidence is claimed. P may remain explicitly pending after repository integration if E + A are clean and no OEM-sensitive change was made.

## Integration rule

Do not call B1 fully validated from static review or unauthenticated E evidence alone.

A branch may be technically ready for repository integration after clean static review + E and an explicit recorded A-validation limitation, but the handoff must state that limitation plainly. Do not silently convert “builds on emulator” into “main tabs validated”.

## Required handoff report

Return:

- base SHA and branch name;
- final commit SHA;
- exact changed files;
- diff rationale per file;
- proof that `GlassTabView` opt-in is main-tabs-only;
- `git diff --check` result;
- E build/APK/install/start/logcat evidence;
- DEBUG fixture evidence if used;
- A matrix result or explicit `A PENDING`;
- P result or explicit `P PENDING`;
- screenshots/artifact paths;
- unresolved visual defects;
- explicit statement whether the branch is ready to integrate and which validation tiers remain open.
