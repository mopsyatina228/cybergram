# Cybergram current state

Last reconciled: 2026-09-13

This is the concise recovery entrypoint for `mopsyatina228/cybergram`.

Read in this order:

1. `AGENTS.md` — repository and safety rules;
2. this file — current state and validation boundary;
3. `docs/CYBERGRAM_UI_SPEC.md` — design/product authority;
4. `docs/EXECUTION_BACKLOG.md` — current pass status, dependencies and validation tiers;
5. the relevant file under `docs/passes/` before executing a bounded pass;
6. `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md` when ownership of remaining UI matters;
7. `docs/WORK_STATE.md` for chronological historical build/device evidence.

Detailed preservation snapshot: `docs/PROJECT_CHECKPOINT_2026-09-11.md`.

## Repository authority

Upstream: `DrKLO/Telegram`.

Upstream-aligned baseline:

- branch `master`;
- Telegram Android 12.10.1 (7038);
- commit `62b56a07ca7e30e39f7fd00a6728d6bbd716ca1c`.

Cybergram integration branch: `dev`.

Preserved product-code cut:

`52b8e219729d0a90dd3335165cf4ef44acf46e5e`

Subsequent preservation/planning/runbook commits do not imply product implementation unless explicitly stated. Always fresh-fetch `dev` rather than trusting a documentation HEAD copied into an old chat.

## Current product state

Durably landed on `dev`:

- built-in Cybergram theme and `.attheme` palette;
- fresh-install Cybergram day/night default without overwriting existing user choice;
- deterministic theme-key validator;
- shared Cybergram theme/HUD/angular geometry primitives;
- angular text/media message-body geometry and semantic borders/palette;
- chat header HUD decoration;
- composer frame and angular send control;
- Cybergram-only flat chat-header seam preserving upstream layout/state behaviour;
- dialogs row treatment and selected-state panel;
- dialogs action-bar decoration;
- opt-in angular dialogs FAB;
- dialogs search presentation;
- Cybergram primary-chrome `sans-serif-condensed` typography;
- original launcher icon treatment;
- Cybergram flat/angular dialog filter/folder tabs in `FilterTabsView`;
- Cybergram flat/angular main bottom navigation (B1): dark chamfered outer panel, angular selected plate and angular long-press selector, replacing the rounded/glass capsule for main tabs only.

No intentional Cybergram protocol, encryption, account/session, storage/database or networking redesign belongs to this state.

## Validation boundary

Earlier revisions have recorded successful arm64 debug builds and Samsung SM-A256E / Android 16 device smoke in `docs/WORK_STATE.md`.

A new reproducible emulator baseline was established on 2026-09-11 using `docs/runbooks/CYBERGRAM_EMULATOR_VALIDATION.md`.

Tested documentation HEAD was `5663bf329d9d78bad5a991350740fe51912f88fb`; the executor confirmed the product-source tree was byte-identical to preserved product cut `52b8e219...`.

Evidence:

- AVD `Cybergram_API36`, Android 16 / API 36 / Google APIs / `x86_64`;
- `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64` -> `BUILD SUCCESSFUL`;
- APK size `68,939,062` bytes;
- APK SHA-256 `66055766015812f92a7de4e396379671fca741b30d3b44b15995066699edef61`;
- `org.telegram.messenger.beta` installed successfully;
- normal pre-auth launch succeeded;
- DEBUG `CybergramShowcaseActivity` launched/rendered successfully;
- no immediate FATAL/ANR observed in either test window.

This proves current product-tree build/install/basic API 36 runtime. It does not prove authenticated Telegram surfaces, network/push behaviour or Samsung/OEM-specific behaviour.

In particular, the final `FilterTabsView` patch is no longer accurately described as “build validation pending”. Its state is:

`LANDED / GENERIC E BUILD+RUNTIME BASELINE PASSED / AUTHENTICATED FILTER-TABS MATRIX PENDING`

B0 in `docs/passes/B0_FILTER_TABS_VALIDATION.md` now contains only the remaining authenticated-surface validation work.

B1 (main bottom navigation) is integrated on `dev` at
`ab314d882b193ec5df9dd188b7e945ea7ef35c98` (production) plus `6802e001012f2cad8eddcc89d137c17534e8f1ba` (DEBUG-only fixture). Its state is:

`INTEGRATED / STATIC PASS / E PASS / A PENDING / P PENDING`

Its E evidence (build, install, launch, no FATAL/ANR, DEBUG opt-in fixture, APK size/SHA-256) is recorded in `docs/WORK_STATE.md` and `docs/passes/B1_MAIN_TABS_FLAT.md`. The authenticated main-tabs interaction matrix and physical-device confidence remain open, and the outer-panel footprint has no authenticated visual confirmation yet.

B2 (message-state coverage and ownership audit) is complete and integrated on `dev` against
`e000ef8286a406fc27fc55889c286ebbffef2890`: **14 PASS / 0 CONFIRMED DEFECT / 6 DESIGN-OPEN / 15 UNTESTED**
across 35 cases, no production rendering fix. Matrix: `docs/B2_MESSAGE_STATE_AUDIT_2026-09-12.md`.
`TYPE_PREVIEW` is closed as theme-preview-only, so **B3 = `CLOSED / NOT REQUIRED`**. The six remaining rows
are the reply plate/bar and reaction pill geometry; after consultant review they were reclassified from
`DEFECT` to `DESIGN-OPEN`, because `docs/CYBERGRAM_UI_SPEC.md` requires angular **outer** message
silhouettes plus the survival of replies/reactions, and names *large* rounded/glass capsules as the
anti-target — it does not require compact internal semantic controls to become angular. Reply/reaction
styling is therefore **B4 = `DESIGN-OPEN / NOT AUTHORIZED`**: no production implementation spec is written
and `ReplyMessageLine` / `ReactionsLayoutInBubble` stay untouched. Its state is:

`INTEGRATED / AUDIT COMPLETE`

Integration (2026-09-12, fast-forward from `audit/cybergram-message-states`, commits kept separate):
DEBUG-only fixture `b2bfdd4180377563ff1cc53a8deb3abaf8fa7dea`, audit/docs
`6c682e15385c07dd4cf28cf1bd69567e0e2692c1`, semantic correction docs-only
`2e9a6a1d066975727cbb4b3888c490a8acbf798a`. The production diff from base over `TMessagesProj/src/main`
is empty. A/P tiers remain open.

B5 (service/date ownership and geometry audit) is integrated on `dev` against
`2db3b48e7f3425518278a909ff65594a5410962a` by fast-forward from `audit/cybergram-service-date`
(2026-09-13, commits kept separate, no squash), integration final SHA
`a5ab7c0de81738e89282dd441ca5f845294c5708`. Its state is:

`INTEGRATED / AUDIT COMPLETE / STATIC OWNERSHIP PASS / E BUILD+RUNTIME+VISUAL-DENSITY PASS / STRATEGY B SELECTED / A RICH+AUTHENTICATED CASES PENDING / P NOT REQUIRED FOR AUDIT`

The ordinary service/date plate owner is `ChatActionCell.backgroundPath`; rich gift/offer/community/
wallpaper/birthday/story states, bot buttons/ribbons and the suggested-post-approval override use
separate paths and stay upstream. The E visual-density gate passed, so **Strategy B is selected**: one
compact enclosing `CybergramBubbleDrawable.buildPath(...)` plate for the ordinary path only. B5 made
**no production rendering change** — the only non-doc change is a DEBUG-source-set fixture — and the
production diff from base over `TMessagesProj/src/main` is empty. Supporting B5 commits preserved:
`34aba0884` (debug fixture), `9b3793230` (audit docs), `4ae5a973ae953700cc209d520136fde461eef6a0`
(debug-only measurement correction) and `a5ab7c0de` (evidence-correction docs).

**B5 E runtime was completed on the real host via Remote Desktop Commander, not the DSH sandbox**:
the existing AVD `Cybergram_API36` booted as `emulator-5554`, the app was normal-launched first and the
B5 fixture second, FATAL/ANR scan = 0, final APK SHA-256
`e4411dd071f8600a58bfa6b9ff08044d0d3b0dc24fad8db99bb10f906dd556ad`, corrected screenshot
`.local-artifacts/b5/b5_corrected.png` (git-excluded). Commit `4ae5a973ae953700cc209d520136fde461eef6a0`
is a **debug-only measurement fix** in the fixture, not a production change. Full evidence and the B6
proposal: `docs/B5_SERVICE_DATE_AUDIT_2026-09-13.md`.

**B6 = `READY / AUTHORIZED FOR BOUNDED IMPLEMENTATION`**, because B5 has selected Strategy B and
`docs/CYBERGRAM_UI_SPEC.md` (line 63) explicitly requires compact dark service/date plates. B6 remains
**NOT IMPLEMENTED** in this docs pass. Production scope stays narrow: ideally only `ChatActionCell.java`
plus imports of the existing `CybergramBubbleDrawable`/`CybergramTheme` helpers, and only the ordinary
service/date `backgroundPath`. Explicitly excluded: `isButtonLayout`/rich gift/offer/community/wallpaper/
birthday/story states, the suggested-post-approval special geometry, reactions, and non-Cybergram
presentation. Paints/shaders/dim, measurement, interaction and the rich `backgroundPath2`/card/ribbon
paths are preserved. The `ThemePreviewActivity` provider-leak concern remains a **B6 verification item**,
not a reason to broaden scope in advance.

Validation tiers are defined in `docs/EXECUTION_BACKLOG.md`: E = emulator, A = authenticated production UI, P = physical-device/OEM confidence.

## Remaining architecture / next work

B1 (flat/angular main bottom navigation) is implemented and integrated; see the validation boundary above for its open A/P tiers. What fresh static reconciliation established remains true and is now a corrected architecture fact:

- authenticated root navigation is `MainTabsActivity`;
- outer panel/glass ownership is in `MainTabsActivity`;
- long-press/drag selector ownership is in `MainTabsLayout`;
- normal selected plate/icon/label/counter/avatar ownership is in `Components/glass/GlassTabView`;
- `MainTabsLayout` and `GlassTabView` were still upstream-identical at the preserved product cut;
- **neither `GlassTabView` nor `MainTabsLayout` is main-tabs-only.** `GlassTabView` is also used by attach/bot tabs and by `StatisticActivity`/`StarGiftPreviewSheet`, and `MainTabsLayout` is also hosted by `StatisticActivity`. B1 therefore must not use a gate-only branch in either class: main-tab instances require explicit local opt-in (`setCybergramMainTabsPresentation`) in addition to `CybergramTheme.isCybergramPresentation(...)`. This is the landed implementation.

The B1 spec is `docs/passes/B1_MAIN_TABS_FLAT.md`.

B2 has audited message-state ownership without production fixes: bodies (`TYPE_TEXT`/`TYPE_MEDIA`) are angular and pass; `TYPE_PREVIEW` is theme-preview-only, so B3 is `CLOSED / NOT REQUIRED`; reply plates and reaction pills are rounded (owner-proven, measured) and are `DESIGN-OPEN`, not defects — B4 is `DESIGN-OPEN / NOT AUTHORIZED` and no implementation spec is written. B5 has now audited ordinary service/date geometry and is integrated: the ordinary plate owner is `ChatActionCell.backgroundPath`, the E visual-density gate passed, and Strategy B is selected. That authorizes B6 for a bounded implementation, which remains `NOT IMPLEMENTED` in this docs pass. Conditional B3/B4/B6 are generated only from those audits; B3 is closed by B2 evidence, B4 is blocked on a design ruling, and B6 is ready but not started.

Optional chat-canvas HUD and secondary screens/onboarding remain later work. Release identity/signing/Firebase/package decisions remain a separate release track.

## Documentation semantics

`docs/CYBERGRAM_UI_SPEC.md` decides what Cybergram should look/behave like as a product.

`docs/EXECUTION_BACKLOG.md` is the current status/dependency index. It deliberately no longer duplicates full implementation specs.

`docs/passes/` contains handoff-ready bounded execution contracts.

`docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md` records static ownership/seam evidence and does not claim runtime validation.

`docs/WORK_STATE.md` is chronological historical evidence. Older statements remain historically true for their revision and must not override this newer state boundary.

## Current operating boundary

The user requested repository-only project control, without autonomous orchestration.

Allowed in this mode: inspect/reconcile GitHub state, improve repository documentation, prepare bounded specs/runbooks and perform explicit repository mutations requested by the user.

Do not infer permission to start background agents, supervisor loops, local builds/runtime sessions or implementation merely because a pass is marked ready. Hermes/local execution is used only when the user explicitly routes a bounded task there.

## Recovery rule

Before a new decision:

1. fresh-fetch `dev` and `master`;
2. read `AGENTS.md` and this file;
3. read `docs/EXECUTION_BACKLOG.md` and the selected pass spec;
4. read the relevant UI spec/architecture evidence;
5. distinguish repository evidence from local-machine evidence and validation tier;
6. never claim local workspace cleanliness from GitHub state alone.

There is currently no known required Cybergram product patch that exists only outside GitHub. A future local-machine inspection may discover new local work; treat that as new evidence, not something to infer from this checkpoint.
