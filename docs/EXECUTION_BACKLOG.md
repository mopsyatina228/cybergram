# Cybergram execution backlog

Status: active planning / execution handoff

Planning snapshot inspected: `dev` at `0c9cd8ebccab3122003905a08be3408926ea689e` on 2026-09-11.

Product-code cut captured by the preservation checkpoint: `52b8e219729d0a90dd3335165cf4ef44acf46e5e`.

Upstream-aligned baseline: Telegram Android 12.10.1 (7038), `master` at `62b56a07ca7e30e39f7fd00a6728d6bbd716ca1c`.

This file converts the remaining Cybergram work into bounded, executable passes. It is not design authority. Read `AGENTS.md`, `docs/CURRENT_STATE.md` and `docs/CYBERGRAM_UI_SPEC.md` first. Use `docs/WORK_STATE.md` for chronological build/device evidence.

## Operating rule

Cybergram remains a presentation fork. Preserve Telegram behaviour and state machines. Prefer a small Cybergram-only presentation branch around an existing upstream renderer over replacing, subclassing or refactoring the component that owns the behaviour.

The central presentation gate is:

`CybergramTheme.isCybergramPresentation(Theme.ResourcesProvider provider)`

Do not introduce scattered theme-name checks and do not infer Cybergram from colour values.

When angular geometry is required, reuse `CybergramBubbleDrawable.buildPath(...)` or `CybergramHudDrawable`. Do not create another independent chamfer/path implementation unless the existing primitives demonstrably cannot represent the target.

A pass that cannot preserve the exact non-Cybergram path is not a presentation-only pass and must stop for redesign.

## Architecture map and risk boundary

### Low-conflict Cybergram-owned primitives

These are project-owned seams and are the preferred place for reusable constants/helpers:

- `TMessagesProj/src/main/java/org/telegram/ui/ActionBar/CybergramTheme.java`
- `TMessagesProj/src/main/java/org/telegram/ui/ActionBar/CybergramBubbleDrawable.java`
- `TMessagesProj/src/main/java/org/telegram/ui/ActionBar/CybergramHudDrawable.java`
- `TMessagesProj/src/main/java/org/telegram/ui/ActionBar/CybergramTypography.java`
- `TMessagesProj/src/main/java/org/telegram/ui/CybergramHeaderDecorationView.java`

Changes here are still subject to regression risk because several screens share them, but upstream merge conflict risk is comparatively low.

### Medium-risk presentation components

These are upstream-owned components with already-established narrow Cybergram seams:

- `FilterTabsView.java`
- `FragmentSearchField.java`
- `FragmentFloatingButton.java`
- `DialogCell.java`
- `ChatAvatarContainer.java`

The `FilterTabsView` implementation is the current reference pattern: keep the scrolling/reorder/delegate machinery untouched, branch only the visible background/selector/clip/typography under the central Cybergram gate, and retain the original rounded/blurred path otherwise.

### High-risk shared/rendering components

These own significant upstream state, gesture, layout or rendering behaviour and must only be changed by tightly bounded passes:

- `MainTabsActivity.java`
- `MainTabsLayout.java`
- `Components/glass/GlassTabView.java`
- `MessageDrawable.java`
- `ChatMessageCell.java`
- `ReplyMessageLine.java`
- `Components/Reactions/ReactionsLayoutInBubble.java`
- `ChatActionCell.java`
- `ActionBar.java`
- `ChatActivityEnterView.java`

Do not mix a visual change with cleanup/refactoring in these files.

## Remaining-surface ownership findings

### Bottom navigation

The bottom navigation is owned by three layers, not by `DialogsActivity` alone.

`MainTabsActivity` creates the `MainTabsLayout`, five `GlassTabView` instances (Chats, Contacts, Settings, Calls, Profile), the blur source/background, bottom fade, wrappers, insets, call/settings visibility switching and ViewPager selection/reselection behaviour.

`MainTabsLayout` owns responsive measurement, animated child visibility, visual-width interpolation, long-press drag selection, touch handling and the special long-press selector.

`GlassTabView` owns normal selected-state rendering, icon/Lottie state, labels, counters/badges and profile avatar presentation.

Therefore a correct flat/angular Cybergram bottom-navigation pass must preserve all three behavioural layers and replace only their presentation seams.

### Message body and adjacent states

`MessageDrawable` owns the message-body drawable. Current Cybergram geometry is intentionally limited to supported text/media paths; historical implementation evidence explicitly left `TYPE_PREVIEW` on the upstream path.

Replies are not merely a small branch inside the bubble renderer. `ReplyMessageLine` has its own paths, paints, peer-colour resolution, animation and loading state.

Reactions are similarly independent. `ReactionsLayoutInBubble` owns reaction-button layout, drawing, counters, animations and touch state.

Do not create a single broad "fix ChatMessageCell" task for replies/reactions/forwards/selection. First identify the actual owner of each visible mismatch.

### Service/date cells

`ChatActionCell` is a large mixed-purpose component containing ordinary service messages plus many special actions/cards, gift states, reactions, buttons, images, spoilers and navigation behaviour.

Do not restyle or clip the whole cell just to obtain an angular date/service plate. First isolate the ordinary text/date background drawing path from special interactive states. A first production pass must leave special cards/actions on their upstream presentation unless explicitly scoped later.

## Global executor contract

Every implementation or validation pass starts from a fresh repository check, not chat/model memory.

Before work:

1. fetch `dev` and `master`;
2. read `AGENTS.md`, `docs/CURRENT_STATE.md`, this file and the relevant `docs/CYBERGRAM_UI_SPEC.md` section;
3. record the starting `dev` SHA;
4. inspect local status before claiming the worktree is clean;
5. for implementation work, use a dedicated feature/fix branch when the pass changes a high-risk shared component.

During work:

- touch only the explicitly allowed files unless new evidence proves another owner is required;
- do not perform unrelated formatting or cleanup;
- do not alter protocol/networking/storage/auth/encryption behaviour;
- do not weaken the non-Cybergram path to simplify the Cybergram path;
- use the central Cybergram presentation gate;
- reuse existing Cybergram geometry/HUD primitives;
- stop rather than silently broadening scope.

Before handoff:

- run `git diff --check`;
- run `python tools/validate_cybergram_theme.py` whenever theme/palette data is touched;
- run the smallest relevant build, then the established debug build when practical;
- never report a build/device pass without exact machine evidence;
- return starting SHA, resulting SHA/commit, changed files, commands run, results, unresolved observations and whether the branch is ready to integrate.

Historical local build command:

`:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a`

Historical output APK:

`TMessagesProj_App/build/outputs/apk/afat/debug/app.apk`

Historical validation device/package:

- Samsung SM-A256E / Android 16 / arm64-v8a
- development package `org.telegram.messenger.beta`

These are evidence of the existing workflow, not permission to assume the same machine/toolchain is currently available.

---

# Bounded pass B0: validate final flat filter tabs

Type: validation only

Priority: immediate blocker cleanup

Current target result: product patch `52b8e219729d0a90dd3335165cf4ef44acf46e5e`, now contained by later documentation-only `dev` commits.

## Goal

Close the only explicit validation gap in the preserved primary-dialogs UI: prove the final `FilterTabsView` implementation builds and behaves correctly on a real device at the exact source revision being tested.

## Production-code scope

No production code changes are authorized by this pass.

If validation exposes a defect, stop and report the smallest reproducible defect. Do not opportunistically repair or refactor `FilterTabsView` in the validation pass.

## Required checks

Repository/static:

- record exact checked-out SHA;
- `git diff --check`;
- `python tools/validate_cybergram_theme.py`;
- verify the inspected `FilterTabsView.java` still contains the Cybergram-only presentation gate and retains the non-Cybergram blurred/rounded path.

Build/device:

- build the established single-ABI afat debug variant if the machine supports it;
- record command, result, APK path, APK size and SHA-256;
- install/update `org.telegram.messenger.beta`, leaving official Telegram untouched;
- launch and record device/OS/package evidence;
- check for FATAL/ANR around the test window.

Visual/interaction matrix:

- selected and unselected folder/filter tabs;
- horizontal overflow/scroll if enough tabs exist;
- tap selection;
- page-swipe/manual selection interpolation;
- unread counters where available;
- long-press/menu behaviour;
- edit/reorder/delete mode where available;
- active Cybergram presentation has the dark chamfered panel and selected plate without the old large blurred pill;
- a non-Cybergram theme retains the upstream rounded/blurred presentation when practical to verify.

## Acceptance

Pass only when build + device evidence exists for the exact tested SHA and the interaction matrix shows no functional regression.

After success, update `docs/WORK_STATE.md` with the exact evidence and update `docs/CURRENT_STATE.md` so the final filter-tabs result is no longer described as validation pending.

---

# Bounded pass B1: flat/angular main bottom navigation

Suggested branch: `feature/cybergram-main-tabs-flat`

Type: production presentation change

Risk: high, because shared navigation/gesture/layout components are involved

## Goal

Replace the remaining large rounded/glass bottom-navigation presentation with a Cybergram dark technical panel and angular selected plates while preserving the complete Telegram navigation state machine.

This pass is deliberately narrower than "redesign bottom navigation". It changes panel/selector shape and surface treatment only.

## Allowed production files

Primary allowed files:

- `TMessagesProj/src/main/java/org/telegram/ui/MainTabsActivity.java`
- `TMessagesProj/src/main/java/org/telegram/ui/MainTabsLayout.java`
- `TMessagesProj/src/main/java/org/telegram/ui/Components/glass/GlassTabView.java`

`CybergramTheme.java` may be changed only if a truly shared semantic constant is needed. Prefer existing `PANEL`, `PANEL_RAISED`, `CYAN` and `BUBBLE_CORNER_CUT_DP` first.

No other production file is in scope without evidence and an explicit scope update.

## Required design seam

Use `CybergramTheme.isCybergramPresentation(resourcesProvider)`.

Reuse `CybergramHudDrawable` for panel-like drawables and `CybergramBubbleDrawable.buildPath(...)` when direct Canvas path drawing is required.

### `MainTabsActivity`

Keep unchanged in behaviour:

- tab creation/order;
- Chats/Contacts/Settings/Calls/Profile mapping;
- call/settings visibility switching;
- click and long-click actions;
- ViewPager selection/reselection/scroll-to-top;
- blur-source lifecycle;
- bottom wrapper/insets/navigation-bar handling;
- update-layout offsets;
- tab visibility animation;
- notification observers and badge updates.

Cybergram may replace the *visible background assigned to `tabsView`* with an angular dark panel. Do not globally disable or delete the blur infrastructure merely because Cybergram does not display the glass background. Keep the upstream blur drawable available for non-Cybergram presentation and theme switching.

A safe shape is an `applyTabsPresentationBackground()`-style seam that selects the Cybergram HUD background or the existing upstream `tabsViewBackground` based on the central gate and is also called when theme colours/presentation update.

Do not alter the separate bottom `fadeView` in the first implementation unless real-device evidence shows that it creates an unacceptable visible glass capsule/wash in Cybergram. If it must be changed, do so with a Cybergram-only presentation branch while retaining its layout/visibility role.

### `MainTabsLayout`

Do not change measurement, animated visibility, hit testing, long-press navigation, `performClick()` behaviour, spring state or ClickHelper logic.

The only intended Cybergram change is the long-press custom selector currently rendered as a full rounded `drawRoundRect`: under Cybergram, render the same computed bounds as a shared chamfered Cybergram plate. The non-Cybergram draw path must remain the existing rounded selector.

### `GlassTabView`

Do not change icon/Lottie state, selection animator timing, labels, counters, profile avatar loading, click behaviour or tab sizing.

The normal selected-state background currently renders a scaled full rounded plate. Under Cybergram, use the same `selectedFactor`, scaling and bounds but draw a chamfered dark/cyan selected plate.

Keep counters/badges rounded in this first pass. They are compact semantic indicators and are not the large glass capsule anti-target this pass is intended to remove.

Keep the profile avatar round.

Typography changes are out of scope unless needed to preserve an already-established Cybergram chrome typeface through an existing helper. Do not combine a typography redesign with this pass.

## Visual target

- outer panel: `CybergramTheme.PANEL`, restrained cyan outline, approximately the existing 1dp project language, 6dp-family chamfer;
- selected plate: `CybergramTheme.PANEL_RAISED` plus restrained cyan outline/accent;
- existing icon/text colour theme keys continue to carry state;
- no large opaque cyan fill;
- no decorative microtext and no fake security labels;
- no new red treatment is required in this pass.

## Stop conditions

Stop and report instead of broadening scope if:

- the desired result appears to require rewriting ViewPager/tab selection behaviour;
- the Cybergram branch would change child measurement or hit targets;
- non-Cybergram presentation cannot be preserved exactly enough;
- more shared files need modification for reasons unrelated to visible panel/selector presentation;
- counters/avatar/icon state would need a behavioural rewrite.

## Validation matrix

Build + real-device smoke is required before integration.

Exercise:

- Chats -> Contacts -> Settings/Profile navigation;
- Calls tab enabled and disabled, including the Settings/Calls position swap;
- reselect current tab and verify scroll-to-top behaviour;
- long press on each supported tab and long-drag selection behaviour;
- unread/permission badge presentation;
- profile avatar rendering;
- tab show/hide animation;
- app-update layout if reasonably reproducible;
- portrait plus configuration/insets change where practical;
- Android navigation-bar bottom inset;
- Cybergram active: no large rounded glass outer capsule or selected pill;
- non-Cybergram theme: upstream glass/rounded navigation remains intact;
- no FATAL/ANR.

Do not integrate on static inspection alone.

---

# Bounded pass B2: message-state coverage audit

Suggested branch: documentation/debug-only branch if repository changes are needed

Type: evidence/audit, not a production fix

Risk: low to production because fixes are forbidden in this pass

## Goal

Replace the vague backlog item "remaining message edge cases" with an evidence matrix that assigns each visible mismatch to the component that actually owns it.

## Production-code scope

No production rendering change is authorized.

The debug-only `CybergramShowcaseActivity` may be extended if necessary to produce deterministic cases that are difficult to create in a live account, provided it remains in the debug source set/manifests only.

## Coverage matrix

Capture or inspect, at minimum:

- incoming and outgoing plain text;
- selected/pressed incoming and outgoing;
- grouped message top/middle/bottom adjacency;
- incoming/outgoing media;
- media caption;
- reply block incoming/outgoing;
- forwarded header/state;
- reactions: none, one, multiple, selected reaction where reproducible;
- links and metadata/checks;
- bot-buttons-bottom interaction with message body where reproducible;
- service/date boundary adjacent to normal messages;
- any real product surface that instantiates `MessageDrawable.TYPE_PREVIEW`.

For each mismatch, record:

- screenshot/reproduction;
- expected Cybergram result;
- actual result;
- owning component, selected from `MessageDrawable`, `ChatMessageCell`, `ReplyMessageLine`, `ReactionsLayoutInBubble`, `ChatActionCell` or another evidenced owner;
- whether the defect is colour, geometry, clipping, layout, state transition or interaction;
- smallest candidate file scope for a later fix.

## Special rule for `TYPE_PREVIEW`

Do not automatically angularize `MessageDrawable.TYPE_PREVIEW` just because `TYPE_TEXT` and `TYPE_MEDIA` are angular.

`TYPE_PREVIEW` has distinct scaling/theme behaviour in `MessageDrawable`, including its own dp scaling and global-theme colour path. First identify where it is actually used and whether changing it is a product requirement or merely changes a theme-preview/editor surface.

Only create a production `TYPE_PREVIEW` geometry task after evidence establishes the desired surface and regression boundary.

## Acceptance

The pass succeeds when every observed mismatch has a concrete owner and no broad "fix ChatMessageCell" placeholder remains.

Write the evidence/result into `docs/WORK_STATE.md` or a dedicated dated audit document and derive separate bounded implementation passes only for confirmed defects.

---

# Conditional pass B3: message preview geometry

Type: production change only if B2 establishes a real requirement

Default status: NOT AUTHORIZED / CONDITIONAL

Likely owner: `MessageDrawable.java`

The pass must preserve the existing `TYPE_PREVIEW` theme/scaling semantics while introducing only the required Cybergram geometry branch. A debug showcase case is required before production integration when possible.

Do not execute B3 merely because this entry exists.

---

# Conditional pass B4: reply/reaction/message-state corrections

Type: production fixes derived from B2

Default status: NOT AUTHORIZED UNTIL OWNER IS KNOWN

Each confirmed problem becomes its own pass. Do not combine `ReplyMessageLine`, `ReactionsLayoutInBubble` and `ChatMessageCell` into one omnibus patch.

Examples of acceptable scopes after evidence:

- reply-line palette/plate only;
- reaction selected/unselected plate only;
- one message-body clipping interaction;
- one forwarded-header visual mismatch.

Each pass must retain all touch/animation/content behaviour and preserve the non-Cybergram branch.

---

# Bounded pass B5: service/date ownership audit

Type: evidence/static audit first

Risk: high if converted prematurely into implementation, because `ChatActionCell` owns many unrelated interactive/special states

## Goal

Identify the exact drawing path for ordinary date separators and ordinary text service messages, and prove it can be restyled without clipping or changing gift/special/action states.

## Audit questions

- Which method/drawable owns the simple date/service text background?
- Is the background shared with special `ChatActionCell` cards or only simple actions?
- Which Theme keys feed its surface/text?
- Can a Cybergram-only chamfered background be injected without changing measurement/touch?
- Are there shader/blur paths that must remain intact for special states?

## Stop condition

Do not create a whole-cell clip, background override or broad `ChatActionCell` restyle as part of this audit.

## Result

Produce a concrete implementation scope for a later `B6` pass limited to ordinary service/date plates. Special cards, gifts, interactive buttons, story/giveaway states and other rich action cells remain upstream unless separately authorized.

---

# Conditional pass B6: plain service/date plates

Type: production presentation change derived from B5

Default status: CONDITIONAL ON B5 FINDINGS

Visual target from `CYBERGRAM_UI_SPEC.md`:

- compact dark technical plate;
- restrained amber/cyan semantic accent;
- chamfered rather than large translucent rounded bubble;
- existing text/layout/accessibility/touch behaviour unchanged.

Use the central Cybergram gate and shared geometry. Do not clip the full `ChatActionCell` canvas if that can affect effects, images or interactive special content.

---

# Optional pass B7: sparse chat-canvas HUD layer

Type: optional polish after primary messaging chrome is stable

Priority: low

The layer must be dedicated, non-interactive and sparse. It must not add per-message noise, fake security claims or intercept touches. It must not silently replace Telegram wallpaper semantics or degrade scroll performance.

Prefer one screen-level background/overlay seam over decorations inside every message cell.

Do not start this pass while bottom navigation or confirmed message-state defects remain open.

---

# Later inventory B8: secondary surfaces

Do not globally restyle the remainder of the client in one pass.

Inventory settings, profiles, media viewer, calls and other secondary screens first. For each surface, identify ownership and create one bounded presentation pass with explicit files and non-Cybergram regression requirements.

Primary messaging coherence remains the gate before broad Stage F work.

---

# Separate release track

The following are intentionally outside the UI backlog and must not be mixed into visual commits:

- final application/package identity;
- release signing and keystores;
- production Telegram API credentials;
- Firebase/service configuration;
- release/distribution packaging;
- any migration implications of changing application ID/package.

Keep secrets outside version control. A future release-preparation pass must define its own security and migration boundaries.

## Recommended execution order

Unless new evidence changes priority:

1. `B0` final filter-tabs build/device validation;
2. `B1` bottom-navigation flat/angular presentation;
3. `B2` message-state coverage audit;
4. derive only evidenced `B3`/`B4` message fixes;
5. `B5` service/date ownership audit;
6. `B6` plain service/date plate if B5 proves a narrow seam;
7. optional `B7` chat-canvas HUD;
8. `B8` secondary-screen inventory and bounded passes;
9. release track separately.

This order is not permission for unattended execution. It exists so a weaker future planning session or a local coding agent does not need to rediscover project structure before each task.