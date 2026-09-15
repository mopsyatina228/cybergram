# Cybergram current state

Last reconciled: 2026-09-14

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
- Cybergram flat/angular main bottom navigation (B1): dark chamfered outer panel, angular selected plate and angular long-press selector, replacing the rounded/glass capsule for main tabs only;
- ordinary angular Cybergram service/date plate (B6): one compact chamfered `CybergramBubbleDrawable.buildPath(...)` envelope for the ordinary `ChatActionCell.backgroundPath` only, integrated on `dev` by fast-forward.

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

**B6 is now INTEGRATED on `dev`, not merely implemented on a feature branch** (see the next section), because B5 selected
Strategy B and `docs/CYBERGRAM_UI_SPEC.md` (line 63) explicitly requires compact dark service/date plates. The
authorization's production scope was respected exactly: only `ChatActionCell.java` plus imports of the existing
`CybergramBubbleDrawable`/`CybergramTheme` helpers, and only the ordinary service/date `backgroundPath`.
Explicitly excluded: `isButtonLayout`/rich gift/offer/community/wallpaper/birthday/story states, the
suggested-post-approval special geometry, reactions, and non-Cybergram presentation. Paints/shaders/dim,
measurement, interaction and the rich `backgroundPath2`/card/ribbon paths are preserved. The
`ThemePreviewActivity` provider-leak concern was resolved by independent review: **no leak reachable today, no
extra production opt-out required now**, with the latent future-scope caveat recorded in the B6 record.

B6 (ordinary angular Cybergram service/date plate) is **integrated on `dev`** by **ff-only** fast-forward (no
squash, no merge commit) from `feature/cybergram-service-date-angular` (branch HEAD
`53dd1368e478ce6a1f9845d1a1797ecfa4e3e0fc`), whose base was `origin/dev`
`cefa15eb7ba42d32b60d31ecf16d626f356344d5`; integration HEAD on `dev` is evidence/docs commit
`0c4172346764c5622a5cdbfa06a4ce624d3cd56a`, reached without a merge commit, with production commit
`f86ef812bb585db7c753335c7f573406e5129323` touches **`ChatActionCell.java` only, +22/−1**: Strategy B is a
minimal additive seam taken *after* upstream ordinary path generation, replacing only the ordinary Cybergram
service/date path with one compact `CybergramBubbleDrawable.buildPath(...)` chamfer and updated
`backgroundLeft/Right`; rich/`isButtonLayout` states and the suggested-post-approval override keep upstream
geometry, and the non-Cybergram upstream path is unchanged. Debug-only control commit
`53dd1368e478ce6a1f9845d1a1797ecfa4e3e0fc` adds the B6 non-Cybergram fixture/wrapper
(`CybergramShowcaseActivity`, debug source set). Independent review verdict: **`SAFE_MINIMAL_SEAM`**; upstream
two-pass side effects are safely superseded only inside `invalidatePath`, and the
`ThemePreviewActivity` foreign app-theme `SCREEN_TYPE_PREVIEW` has no `ChatActionCell`/`contentType == 1` row
today, so **no extra `ThemePreviewActivity` production opt-out is required now**. Latent caveat recorded: a
future foreign-theme preview row that did instantiate a `ChatActionCell` would need an explicit opt-out,
because the global `Theme.getCurrentTheme()` fallback is not preview-scoped. Its state is:

`INTEGRATED ON dev / BOUNDED PRODUCTION SEAM (ChatActionCell.java ONLY) / INDEPENDENT REVIEW SAFE_MINIMAL_SEAM / E BUILD+INSTALL+RUNTIME+VISUAL PASS / E-CONTROL PASS / E-RICH UNAVAILABLE PRE-AUTH / A PENDING / P NOT REQUIRED / RELEASE INSTALL COMPATIBILITY OPEN FOR REDMI MIUI`

E evidence: x86_64 production-commit build BUILD SUCCESSFUL, APK `73,306,179` bytes, SHA-256
`e415b00305d83122575bd81a18061d0e0b935487cbf0b9a7871388c0407eb621`, install on `emulator-5554`, normal launch
plus real `ChatActionCell` B5/B6 fixture stable with no FATAL/ANR/process death, screenshot
`.local-artifacts/b6/b6_f86ef812.png`, plate widths `98/160/288/230/310/642` px → visual Strategy B pass.
arm64-v8a production-commit build BUILD SUCCESSFUL, `68,452,403` bytes, SHA-256
`9d5f936b820766aec4f6c825e8625c147820b2fbc81b9690fe419ad7364e71c8` — **build evidence only, not a physical
install or runtime**. Debug-control E run (x86_64, `53dd1368e`) BUILD SUCCESSFUL in 29s, `68,948,592` bytes,
SHA-256 `ef97f76daf99bbd327287fb25c3a08edefa09b168f0ba9b439ab8b52fac12452`, install success, emulator saved
theme temporarily Cybergram→Blue and **restored to Cybergram** afterwards, screenshot
`.local-artifacts/b6/b6_control_blue.png` proving the upstream rounded ordinary path under the non-Cybergram
provider, `CRASH_MATCHES=0` → **E-control PASS**. The universal 4-ABI compatibility build on `53dd1368e` is
BUILD SUCCESSFUL in 1m 23s, `113,631,225` bytes, SHA-256
`0c781a6b71f13034b36586b4c596a48a3b8719e108d51195dc89c305a78e12f0`, ABIs
`arm64-v8a/armeabi-v7a/x86/x86_64`, minSdk 21 / targetSdk 36, signed v1+v2, published as a separate prerelease
asset — **compatibility/build evidence and an installer probe, NOT device runtime validation**. Full record:
`docs/B6_SERVICE_DATE_IMPLEMENTATION_2026-09-14.md`.

**Open release/install issue (non-rendering):** a Redmi Note 10S / MIUI 14.0.4 user reports the arm64 APK did
not install; the exact `INSTALL_FAILED_*` code is unavailable because the device is not currently in `adb`. No
ABI root cause is claimed; the universal APK is the explicit compatibility probe, and because the package is
`org.telegram.messenger.beta` a signature/package conflict with an existing Telegram Beta remains a plausible
unresolved install cause. This is tracked as a release/install issue, **not** as a B6 rendering defect.

**E-rich remains unavailable pre-auth** (rich gift/offer/community/wallpaper/birthday/story rows were not
faked); **A (authenticated service/date and rich cases) remains pending**; **P is not required** unless a
device/OEM runtime defect appears — the Redmi report is unresolved installation compatibility, not runtime
rendering evidence. B6 **is integrated on `dev`** by ff-only fast-forward from
`feature/cybergram-service-date-angular` (no squash, no merge commit); this status reconciliation is docs-only
and performs no push, no product-code change and no release-asset change.

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

B2 has audited message-state ownership without production fixes: bodies (`TYPE_TEXT`/`TYPE_MEDIA`) are angular and pass; `TYPE_PREVIEW` is theme-preview-only, so B3 is `CLOSED / NOT REQUIRED`; reply plates and reaction pills are rounded (owner-proven, measured) and are `DESIGN-OPEN`, not defects — B4 is `DESIGN-OPEN / NOT AUTHORIZED` and no implementation spec is written. B5 has audited ordinary service/date geometry and is integrated: the ordinary plate owner is `ChatActionCell.backgroundPath`, the E visual-density gate passed, and Strategy B is selected. That authorized B6, which is now **integrated on `dev`** (ff-only from `feature/cybergram-service-date-angular`, no squash and no merge commit) and carries E build/install/runtime/visual and E-control passes with A/E-rich open. Conditional B3/B4/B6 are generated only from those audits; B3 is closed by B2 evidence, B4 remains `DESIGN-OPEN / NOT AUTHORIZED` and next product work must not silently start B4, and B6's bounded implementation is integrated at E tier pending A.

Optional chat-canvas HUD and secondary screens/onboarding remain later work. Release identity/signing/Firebase/package decisions remain a separate release track.

**The largest outstanding item is A-tier verification debt, not new presentation work.** B0 (filter tabs), B1 (main tabs) and B6 (ordinary service/date and rich states) are integrated at E tier, and B2 carries 15 `UNTESTED` account-dependent rows. The long-standing assumption that no authenticated session was available was **wrong**: the `Cybergram_API36` AVD is authenticated, and the first authenticated run on 2026-09-15 partially validated **B1** (tab navigation, angled nav panel, selected plate, badge, avatar, long-press; `crash_matches=0`) — record: `docs/A_TIER_VALIDATION_2026-09-15.md`. B0, B2, B5 and B6 remain untested on an authenticated surface; those tranches that require opening conversations mark messages as read in a live account and therefore need an explicit owner decision. E evidence must still not be read as product-surface validation.

B7 (optional chat-canvas HUD) now has a prepared bounded contract at `docs/passes/B7_CHAT_CANVAS_HUD.md`, status `SPEC PREPARED / NOT AUTHORIZED`. Its precondition (coherent primary message/service presentation) is met at E tier, and its ownership was re-audited rather than inferred: the chat canvas host is `ChatActivity.ChatActivityFragmentView` (used only by `ChatActivity`; `ChannelAdminLogActivity` has its own separate class), the wallpaper is `SizeNotifierFrameLayout.BackgroundView` at child index 0, and the landed `CybergramHeaderDecorationView` is the precedent to copy. Writing the spec authorizes nothing: B7 is optional, needs an explicit user priority decision, and `SizeNotifierFrameLayout` (19 hosts) must not receive the seam. B8 remains deferred.

The owner's design target (`design/DESIGN_TARGET.md` and `design/references/design-target-hex-chat.jpg`, commit `39c301bb4`) is reconciled against this state and the design authority in `docs/DESIGN_TARGET_RECONCILIATION_2026-09-15.md`. It **confirms** the landed colour roles (cyan self / amber peer / red service), the near-black palette, thin bubble outlines, the compact service/date plate and edge-only non-interactive decoration. It **raises three pending owner decisions**: the message silhouette (the reference's rounded corners with a tail versus the landed angular geometry that `docs/CYBERGRAM_UI_SPEC.md` requires), two palette hues, and what service-label copy may say — `SECURE CHAT` / `END-TO-END` remain banned by spec line 117. That reconciliation authorizes no pass, and it re-verified B7's ownership anchors on 2026-09-15 (they hold, with two details corrected in the backlog).

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
