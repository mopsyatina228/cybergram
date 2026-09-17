# Cybergram current state

Last reconciled: 2026-09-16

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

Subsequent preservation/planning/runbook commits do not imply product implementation unless explicitly stated. Any documentation HEAD cited in a record is a historical snapshot of that record, not the current repository state; **re-check the branch, HEAD and `git status` yourself instead of trusting a hash copied into an old chat.**

**Documentation drift note (2026-09-16 reconciliation):** a documentation HEAD/status hash quoted in an older record (for example `execution_backlog` line 13 citing `0c4172346`) is that record's *starting* snapshot, not the live HEAD. At the time of this reconciliation the live `dev` HEAD was `937bbb8c5`. Always resolve the current HEAD from git.

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
- ordinary angular Cybergram service/date plate (B6): one compact chamfered `CybergramBubbleDrawable.buildPath(...)` envelope for the ordinary `ChatActionCell.backgroundPath` only, integrated on `dev` by fast-forward;
- owner-ruled palette alignment (D2, 2026-09-15): reference amber `#FFB300`, service red `#FF003C`, muted `#6B7A8A`, UI text `#E6F7FF`, applied to `CybergramTheme` and `cybergram.attheme`;
- default **replaceable** Cybergram backdrop (D4, 2026-09-16): a faint cyan grid plus minimal edge framing, implemented as a `ColorDrawable` subclass returned from `ChatActivity.ChatActivityFragmentView.getNewDrawable()`, so there is no child-view insertion and no z-order change; suppressed whenever the user has a wallpaper of their own;
- distinct-bubble spacing (D6, 2026-09-16): a paint-only, join-aware vertical inset in `MessageDrawable.generateCybergramPath(...)`; measurement, layout, scroll, metadata and grouped joins are unchanged.

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

`FIX LANDED 2026-09-16 / E BUILD+INSTALL+VISUAL PASS ON THE DEFECT / REMAINING A MATRIX ITEMS OPEN`

On the authenticated AVD the dialog filter/folder row works — chips switch, the list changes, `crash_matches=0` — but the **selected** chip rendered as an empty chamfered plate with no title, because the Cybergram selector plate is opaque (alpha 255 versus upstream 31) and `FilterTabsView.drawChild` painted it after the labels (`FilterTabsView.java:1531`). Finding: `docs/B0_FILTER_TABS_DEFECT_2026-09-15.md`. **The defect is fixed** by the bounded B0-FIX change (production commit `3911690fe`, ff-only into `dev`; E evidence `docs/B0_FIX_IMPLEMENTATION_2026-09-16.md`): the Cybergram plate is now drawn before the labels for the Cybergram branch only, and E captures show the selected `All Chats` chip and then the selected `12412412 104` chip rendered with their labels. B0 itself is validation-only; the remaining work is the authenticated filter-tabs matrix (including the fixed selection behaviour, horizontal overflow/scroll and edit/reorder/delete mode) plus the non-Cybergram control re-run.

B1 (main bottom navigation) is integrated on `dev` at
`ab314d882b193ec5df9dd188b7e945ea7ef35c98` (production) plus `6802e001012f2cad8eddcc89d137c17534e8f1ba` (DEBUG-only fixture). Its state is:

`INTEGRATED / STATIC PASS / E PASS / A PARTIAL / P PENDING`

Its E evidence (build, install, launch, no FATAL/ANR, DEBUG opt-in fixture, APK size/SHA-256) is recorded in `docs/WORK_STATE.md` and `docs/passes/B1_MAIN_TABS_FLAT.md`. The first authenticated run on 2026-09-15 confirmed the tab navigation, the cyan selected plate, the unread badge, the profile avatar, tab long-press, the angular dark outer panel and the non-Cybergram theme control (`crash_matches=0`) — record `docs/A_TIER_VALIDATION_2026-09-15.md` — so B1 is `A PARTIAL`, not `A PENDING`. Still open on the authenticated surface: the Calls enable/disable swap, show/hide animation, attach/bot tab geometry. Physical-device confidence remains `P PENDING`.

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

### Round-4 validation update (2026-09-17)

- **B0 (filter tabs)** — an authenticated re-run confirmed the selected chip renders **with its label
  and badge** (B0-FIX verified at A). A further interaction follow-up on the physical Redmi closed
  **page swipe, long-press/menu and edit/reorder/delete mode**, plus labels, unread counters and
  tap-between-tabs; **horizontal overflow/scroll is not testable** with only four folders. The
  non-Cybergram control PASSED at P (upstream rounded chips).
- **B1 (main tabs)** — an authenticated re-run exercised Chats → Contacts → Settings → Profile →
  Chats with the angular panel and the cyan selected plate tracking the active tab
  (`FATAL=0`, `ANR=0`). A further follow-up on the physical Redmi closed **reselect/scroll-to-top, tab
  long-press (quick-actions menu over a scrim) and orientation change**, and confirmed the search tab
  row renders with the Cybergram angular plate; still open: Calls enable/disable and the
  Settings/Calls swap, long-drag selection, tabs show/hide animation and attach/bot tab geometry. The
  non-Cybergram control PASSED at P (upstream rounded navigation).
- **B2 (15 account-dependent rows)** — all 15 verdicted: 11 **PASS** (8 group slicing, 10 incoming
  media, 12 caption/time-on-media, 14 cell multi-select overlay, 18 reply layout, 25 reaction glyphs,
  27 time/checks/views, 28 forwarded header, 29 links + metadata incl. link-preview cards, 31
  service/date adjacency, 35 Cybergram theme preview), 4 **PARTIAL** (13 media clipping/touch targets,
  19 quote/code/link/contact lines, 26 reaction touch — the reactor popup scrim was captured but the
  particle animation is not observable in a still, 30 bot buttons — composer Menu and a bottom
  comments button seen, no inline keyboard), **0 defects**.
- **B5/B6** — the ordinary `April 13` date separator renders as a compact dark **angular** plate with
  chamfered corners (A PASS); ordinary service actions, service-message reactions and the
  `SharedMediaLayout` floating date were not exercised; rich/special rows remain `UNAVAILABLE` (not
  faked).
- **P (physical/OEM)** — a **Redmi Note 10S** (`4H8L598LAME6CEX4`, `M2101K7BNY`, Android 13, MIUI
  `V140`, arm64-v8a) became reachable during the run: the arm64 build installed
  (`adb install -r` → `Success`, resolving the R1 install item), launched with `FATAL=0` / `ANR=0`,
  and renders the Cybergram chrome with the microphone glyph centred and pale blue (`8FBFCC`). The
  Samsung SM-A256E target was not attached, so genuine Samsung/OEM confidence is still outstanding.
- **R-series implemented (2026-09-17)** — the three recorded findings are fixed on `dev` as bounded,
  Cybergram-gated/neutral changes: **R-PERF** caches the Cybergram outline path instead of rebuilding
  it every frame; **R-STUB** stops the D4 grid decorating the per-chat default-theme stub;
  **R-OVERLAY** drives the inert header view's visibility from the gate (`GONE` when non-Cybergram)
  with an attach/theme-change re-evaluation, so runtime Day ↔ Cybergram switching still needs no
  activity recreation. Record: `docs/R_SERIES_IMPLEMENTATION_2026-09-17.md`.
- Record: `docs/A_TIER_VALIDATION_2026-09-17_ROUND4.md`; journal: `docs/WORK_STATE.md`.

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

**The largest outstanding item is A-tier verification debt, not new presentation work.** B1 (main tabs) is `A PARTIAL` and B6 is `A PENDING`; B0's confirmed defect is fixed by B0-FIX and the fixed behaviour carries A-partial evidence; and B2 carries 15 `UNTESTED` account-dependent rows. The long-standing assumption that no authenticated session was available was **wrong**: the `Cybergram_API36` AVD is authenticated, and the first authenticated run on 2026-09-15 partially validated **B1** (tab navigation, angled nav panel, selected plate, badge, avatar, long-press; `crash_matches=0`) while also confirming the B0 selected-chip defect — record: `docs/A_TIER_VALIDATION_2026-09-15.md`. B0 reached the authenticated filter row and stopped on the defect (it is not untested; it is blocked on an unauthorized fix); B2, B5 and B6 remain untested on an authenticated surface; those tranches that require opening conversations mark messages as read in a live account and therefore need an explicit owner decision. E evidence must still not be read as product-surface validation.

B7 (optional chat-canvas HUD) now has a prepared bounded contract at `docs/passes/B7_CHAT_CANVAS_HUD.md`, status `SPEC PREPARED / NOT AUTHORIZED`. Its precondition (coherent primary message/service presentation) is met at E tier, and its ownership was re-audited rather than inferred: the chat canvas host is `ChatActivity.ChatActivityFragmentView` (used only by `ChatActivity`; `ChannelAdminLogActivity` has its own separate class), the wallpaper is `SizeNotifierFrameLayout.BackgroundView` at child index 0, and the landed `CybergramHeaderDecorationView` is the precedent to copy. Writing the spec authorizes nothing: B7 is optional, needs an explicit user priority decision, and `SizeNotifierFrameLayout` (19 hosts) must not receive the seam. B8 remains deferred.

The owner's design target (`design/DESIGN_TARGET.md` and `design/references/design-target-hex-chat.jpg`, commit `39c301bb4`) is reconciled against this state and the design authority in `docs/DESIGN_TARGET_RECONCILIATION_2026-09-15.md`. It **confirms** the landed colour roles (cyan self / amber peer / red service), the near-black palette, thin bubble outlines, the compact service/date plate and edge-only non-interactive decoration. It **raised three owner decisions** — all since ruled: the message silhouette (the reference's rounded corners with a tail versus the landed angular geometry that `docs/CYBERGRAM_UI_SPEC.md` requires), two palette hues, and what service-label copy may say — `SECURE CHAT` / `END-TO-END` remain banned by spec line 117. **D1 was ruled by the owner on 2026-09-15: keep the landed chamfer**, so no silhouette change, Stage C re-opening or spec amendment follows from the reference. **All the owner's round-2 decisions are now ruled and recorded** in `docs/OWNER_DECISIONS_2026-09-15.md`: D2 (palette) and D7/D8 (composer outline, header rudiment) were implemented on 2026-09-15; D4 (default replaceable backdrop) and D6 (distinct-bubble spacing) were implemented on 2026-09-16; D5 (Cyrillic-first typography) was verified on the device on 2026-09-16 and resolved without a new asset; D3 (label copy) is a copy policy, not an implementation; and the owner's `флажки` item is still unresolved pending clarification. The design authority `docs/CYBERGRAM_UI_SPEC.md` was amended on 2026-09-16 to match the ruled palette, the label-copy policy, the default backdrop and the bubble spacing. That reconciliation authorizes no pass, and it re-verified B7's ownership anchors on 2026-09-15 (they hold, with two details corrected in the backlog).

## Open blockers and decisions (2026-09-16)

A reader resuming this project should treat the following as **not done and not authorized**:

1. **B0-FIX — landed and independently verified; only the A-matrix remainder is open.** The selected
   dialog filter/folder tab lost its label; the fix is the draw-order change in `FilterTabsView`
   (production commit `3911690fe`, ff-only into `dev`). Defect record:
   `docs/B0_FILTER_TABS_DEFECT_2026-09-15.md`; implementation evidence:
   `docs/B0_FIX_IMPLEMENTATION_2026-09-16.md`; independent device verification:
   `docs/R1_INDEPENDENT_DEVICE_VERIFICATION_2026-09-16.md`. Still open: horizontal overflow/scroll to the
   last folder, page swipe, long-press/menu, edit/reorder/delete mode and the non-Cybergram control re-run.
2. **A-tier verification debt.** B1 is `A PARTIAL`; B2, B5 and B6 are untested on an authenticated
   surface. Tranches that open conversations mark messages as read in a live account and therefore need
   an explicit owner decision; checklist: `docs/runbooks/CYBERGRAM_A_TIER_VALIDATION.md`.
3. **Owner item `флажки` is unresolved.** `docs/OWNER_DECISIONS_2026-09-15.md` §4/§8 records the
   ambiguity (send-state check marks versus a literal flag/marker decoration); nothing was implemented.
4. **Open E gaps.** D4/D6 have no non-Cybergram control screenshot; `E-rich` service/date states were
   never faked pre-auth and are unavailable; the 2026-09-16 pre-build ANR is recorded in
   `docs/WORK_STATE.md` with an unknown cause and no logcat.
5. **Release/install issue.** The Redmi Note 10S / MIUI 14.0.4 arm64 install report is unresolved and
   has no `INSTALL_FAILED_*` text; the universal 4-ABI APK is the compatibility probe (track R1).
6. **R-series code-review follow-ups** (`R-D6`, `R-STUB`, `R-PERF`, `R-OVERLAY`) are recorded
   in `docs/EXECUTION_BACKLOG.md` and are **not authorized**. `R-GATE` is no longer one of them: it
   landed as production commit `4629e98a3` (ff-only into `dev`) and was independently re-verified on the
   device — evidence `docs/R_GATE_IMPLEMENTATION_2026-09-16.md` and
   `docs/R1_INDEPENDENT_DEVICE_VERIFICATION_2026-09-16.md`.
7. **R1 acceptance is recorded but not applied by the harness gate.** The independent reviewer session
   `b7a79579-8005-4791-bd81-02c6680a31f5` (`deepseek-v4-pro`) returned `APPROVE` on the two R1 edits with
   file-, build-log- and screenshot-level grounds, but the task stayed `NEEDS_HUMAN`: the review was
   requested while the task was already outside `RUNNING`, so the gate never moved it to `REVIEWING`, and
   its verdict digest only visits tasks in that state. Re-running `check` starts a second paid reviewer
   for the same result. R1 state is therefore `REVIEWED / ACCEPTANCE NOT APPLIED — gate-side fix`. This
   is harness behaviour, not a Cybergram product or documentation defect.

None of the above is a reason to treat the landed presentation work as unvalidated at E tier; they are
the open items that remain after the E-tier work.

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
