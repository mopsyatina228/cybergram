# Cybergram execution backlog

Status: active planning / execution handoff

Last reconciled: 2026-09-16

Repository: `mopsyatina228/cybergram`

Upstream baseline: Telegram Android 12.10.1 (7038), `master` at `62b56a07ca7e30e39f7fd00a6728d6bbd716ca1c`.

Preserved product-code cut: `52b8e219729d0a90dd3335165cf4ef44acf46e5e`.

Current documentation/validation HEAD **at the start of this reconciliation** was `0c4172346764c5622a5cdbfa06a4ce624d3cd56a` on `dev` (that was equal to `origin/dev` at that moment, but it is a historical snapshot and is **not** the live HEAD — re-check `git rev-parse HEAD`). At that point `dev` had reached `0c4172346` by **ff-only** integration of `feature/cybergram-service-date-angular` from base `cefa15eb7ba42d32b60d31ecf16d626f356344d5` — no squash and no merge commit — carrying production `f86ef812bb585db7c753335c7f573406e5129323`, debug control `53dd1368e478ce6a1f9845d1a1797ecfa4e3e0fc` and evidence/docs `0c4172346764c5622a5cdbfa06a4ce624d3cd56a`. B6 is **integrated on `dev`**. The live `dev` HEAD at this reconciliation was `937bbb8c5`.

This file is the status/dependency index for remaining Cybergram work. Detailed implementation instructions live in `docs/passes/`; source-ownership evidence lives in `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md`; design authority remains `docs/CYBERGRAM_UI_SPEC.md`; chronological machine evidence remains `docs/WORK_STATE.md`.

Do not duplicate full pass instructions here. Humans are already quite capable of creating two contradictory copies of the same plan without our assistance.

## Operating contract

Cybergram remains a presentation fork. Preserve Telegram behaviour and state machines. Prefer a narrow Cybergram-only presentation seam around an upstream renderer rather than replacing or refactoring the component that owns behaviour.

Use `CybergramTheme.isCybergramPresentation(Theme.ResourcesProvider provider)` as the central presentation gate. Do not scatter theme-name checks and do not infer Cybergram from colour values.

Use `CybergramBubbleDrawable.buildPath(...)` / `CybergramHudDrawable` for angular geometry instead of introducing another chamfer implementation.

Any shared component that serves both the main Cybergram surface and unrelated Telegram surfaces requires an explicit local opt-in in addition to the central theme gate. The important current examples are `GlassTabView` (main bottom navigation vs attach/bot tabs and other `createMainTab` callers) and `MainTabsLayout` (main bottom navigation vs `StatisticActivity`); a gate-only branch inside either class would leak main-navigation styling into unrelated surfaces.

For high-risk upstream files, do not combine presentation changes with cleanup/refactoring.

## Validation baseline established 2026-09-11

A reproducible emulator path now exists in `docs/runbooks/CYBERGRAM_EMULATOR_VALIDATION.md`. The authenticated counterpart is `docs/runbooks/CYBERGRAM_A_TIER_VALIDATION.md`, which consolidates the pending B0/B1/B2/B5/B6 A-tier matrices into one executable checklist so the largest outstanding debt can be closed in a single run when an authenticated session is available.

Current product tree was validated from documentation HEAD `5663bf329d9d78bad5a991350740fe51912f88fb`, which was confirmed byte-identical to product cut `52b8e219729d0a90dd3335165cf4ef44acf46e5e` for product sources.

Machine evidence from the completed run:

- AVD: `Cybergram_API36`, Android 16 / API 36, Google APIs, `x86_64`, 1080x2400;
- emulator 37.1.11 with WHPX hardware acceleration;
- build: `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64`;
- result: `BUILD SUCCESSFUL`;
- APK: `TMessagesProj_App/build/outputs/apk/afat/debug/app.apk`;
- size: `68,939,062` bytes;
- SHA-256: `66055766015812f92a7de4e396379671fca741b30d3b44b15995066699edef61`;
- package: `org.telegram.messenger.beta`, version `12.10.1` / versionCode `70389`;
- install succeeded;
- normal onboarding launch succeeded;
- DEBUG `CybergramShowcaseActivity` launch/render succeeded;
- no immediate FATAL/ANR was observed in the validation windows.

This closes the generic current-tree build/install/basic-start gap. It does **not** prove authenticated Telegram surfaces such as `FilterTabsView` or `MainTabsActivity`, network/push behaviour, or Samsung/OEM-specific behaviour.

Use two validation tiers from now on:

- **E (emulator):** build/install/start, debug showcase/harness, generic Android 16 runtime, screenshots and logcat. This is the default remote validation tier and should be run for every implementation pass that can use it.
- **A (authenticated UI):** the real product surface is exercised with an authenticated Telegram session, on an emulator or physical device. Required when the pass changes UI that cannot be reached pre-auth.
- **P (physical device):** final/OEM confidence on real hardware. Required before calling Samsung/OEM-sensitive behaviour validated; otherwise it is a release-confidence check rather than a reason to block every repository-side implementation.

Never promote E evidence into A or P evidence.

## Execution queue

### B0 — final FilterTabs authenticated validation

Status: `FIX LANDED 2026-09-16 — E PASS / A PARTIAL ON THE FIXED BEHAVIOUR / REMAINING A MATRIX ITEMS OPEN`

A re-run on 2026-09-17 (round 4, `docs/A_TIER_VALIDATION_2026-09-17_ROUND4.md`) confirmed on the
authenticated surface that the selected chip renders **with its label and badge** (tapping `263679`
and `12412412 108` switches folders with labels intact), closing the B0-FIX re-run item at A tier.
Still open: horizontal overflow/scroll, page swipe, long-press/menu, edit/reorder/delete mode and the
non-Cybergram control (the last needs a persisted theme mutation).

Type: validation only. The 2026-09-15 authenticated run was stopped at a confirmed presentation defect and no fix was attempted; the record is `docs/B0_FILTER_TABS_DEFECT_2026-09-15.md`. The defect was fixed on 2026-09-16 by a separate bounded change (B0-FIX below), so the remaining B0 work is the authenticated matrix re-run, not the defect.

On the authenticated AVD the dialog filter/folder row works — chips can be switched, the list changes, `crash_matches=0` — but the **selected** chip rendered as an empty chamfered plate with no title, while unselected chips are labelled normally. Root cause was verified statically: `FilterTabsView.drawChild` calls `drawSelector` *after* the labels are drawn, and the Cybergram branch sets the plate alpha to `255` (upstream uses `31`), so an opaque plate painted over the text hides it (`FilterTabsView.java:1531` before the fix, `:1539` after). **Fixed 2026-09-16** — the Cybergram plate is now painted before the labels and the selected chip renders with its label (`docs/B0_FIX_IMPLEMENTATION_2026-09-16.md`).

Spec: `docs/passes/B0_FILTER_TABS_VALIDATION.md`.

The 2026-09-11 emulator run proves the exact current product tree builds, installs and starts. It did not reach an authenticated dialogs screen, so it does not prove the final `FilterTabsView` interaction/visual matrix. B0 now consists only of closing that remaining authenticated-surface gap (horizontal overflow/scroll to the last folder, edit/reorder/delete mode) plus the A re-run of the fixed selection behaviour.

B0 no longer blocks static design of B1. It blocks only claims that final filter tabs are fully runtime-validated.

### B0-FIX — selected filter-tab label (bounded fix)

Status: `IMPLEMENTED 2026-09-16 / E BUILD+INSTALL+RUNTIME+VISUAL PASS / A PARTIAL (fixed selection behaviour confirmed on the authenticated surface; remaining matrix items not run) / P NOT RUN`.

Record: `docs/B0_FIX_IMPLEMENTATION_2026-09-16.md` (finding: `docs/B0_FILTER_TABS_DEFECT_2026-09-15.md`). Owner: `TMessagesProj/src/main/java/org/telegram/ui/Components/FilterTabsView.java` (`drawChild` 1407-1471, new guard at 1412-1419, `drawSelector` 1473-1543, alpha at 1539).

The selected dialog filter/folder tab lost its title because the Cybergram selector plate is opaque and was drawn after the labels. The applied fix draws the Cybergram plate before the children instead of after `super.drawChild`, keeping the upstream branch untouched (production commit `3911690fe`, ff-only into `dev`). E evidence: build + install + `crash_matches=0`, and captures showing the selected `All Chats` chip and then the selected `12412412 104` chip rendered with their labels, while the previously selected chip returns to its labelled unselected state.

Not done: the non-Cybergram control re-run (needs a persisted theme mutation, deliberately not performed; the branch is statically unchanged) and the remaining authenticated matrix items (horizontal overflow/scroll, page swipe, long-press/menu, edit/reorder/delete mode) — the run stopped because the emulator produced repeated input-dispatch ANRs whose traces sit in upstream/system code (StaticLayout text layout, `ChatActivity.createView`, `BatteryManager` Binder call).

A same-class sweep of every other Cybergram seam is recorded in `docs/CYBERGRAM_PLATE_DRAWORDER_AUDIT_2026-09-15.md`. It found the defect to be **isolated**: every other filled plate (main-tabs selector, `GlassTabView`, search field, FAB, dialog rows, bubbles) is already drawn below the content it frames, so the applied fix follows that existing pattern rather than inventing a new one. The audit also records one latent hazard: the composer frame is likewise drawn after its children and is safe only because its fill is disabled (`ChatActivityEnterView.java:2672-2692`).

### B1 — flat/angular main bottom navigation

Status: `INTEGRATED / STATIC PASS / E PASS / A PARTIAL / P PENDING`.

A re-run on 2026-09-17 (round 4, `docs/A_TIER_VALIDATION_2026-09-17_ROUND4.md`) exercised the
authenticated navigation Chats → Contacts → Settings → Profile → Chats: the angular dark panel and the
cyan selected plate track the active tab, the Contacts badge renders, and `crash_matches=0`,
`anr_matches=0`. Still open: Calls enable/disable and the Settings/Calls swap, reselect/scroll-to-top,
long-press/long-drag selection, tabs show/hide animation, orientation change, attach/bot tab geometry
and the non-Cybergram control.

First authenticated (A) run: 2026-09-15, `docs/A_TIER_VALIDATION_2026-09-15.md`. On a real authenticated
session the tab navigation (Chats → Contacts → Settings → Profile → Chats), the angular dark outer panel,
the cyan selected plate tracking the active tab, the unread badge, the profile avatar and tab long-press
were all confirmed, with `crash_matches=0` and the installed APK proven (by device pull + hash) to match
the current product sources. Not yet exercised: Calls enable/disable and the Settings/Calls swap, reselect
and scroll-to-top, long-drag selection, show/hide animation, orientation change, the non-Cybergram theme
check, and attach/bot tab geometry. The earlier "A pending" reason — no authenticated session exists — was
wrong and has been corrected in the status documents.

Priority: was the highest-value remaining production presentation pass.

Spec: `docs/passes/B1_MAIN_TABS_FLAT.md`. Machine evidence: `docs/WORK_STATE.md`.

Integrated on `dev` by fast-forward from `feature/cybergram-main-tabs-flat`, commits kept separate:

- production `ab314d882b193ec5df9dd188b7e945ea7ef35c98`;
- DEBUG-only fixture `6802e001012f2cad8eddcc89d137c17534e8f1ba`.

Primary owners:

- `TMessagesProj/src/main/java/org/telegram/ui/MainTabsActivity.java`
- `TMessagesProj/src/main/java/org/telegram/ui/MainTabsLayout.java`
- `TMessagesProj/src/main/java/org/telegram/ui/Components/glass/GlassTabView.java`

Architecture rule (corrected during execution): `GlassTabView` is shared with attach/bot tabs and is also reached from `StatisticActivity`/`StarGiftPreviewSheet`, and `MainTabsLayout` is **also** hosted by `StatisticActivity`. B1 therefore uses an explicit main-tabs presentation opt-in, default false, in **both** `MainTabsLayout` and `GlassTabView`, combined with the central Cybergram presentation gate. A gate-only branch in either class would leak main-navigation geometry into unrelated surfaces.

B1 preserved all ViewPager, long-press, drag-selection, visibility, badge, counter, avatar, inset and update-layout behaviour; only the visible outer panel and the selected-plate/selector shapes/surfaces changed.

E validation passed. A validation is still required before the actual main-tabs interaction matrix can be called complete, and P remains desirable before release confidence. The outer-panel/fadeView visual result is not yet confirmed on an authenticated surface.

### B2 — message-state coverage audit

Status: `INTEGRATED / AUDIT COMPLETE`.

Spec: `docs/passes/B2_MESSAGE_STATE_AUDIT.md`. Evidence matrix: `docs/B2_MESSAGE_STATE_AUDIT_2026-09-12.md`.

Tested revision `e000ef8286a406fc27fc55889c286ebbffef2890`. Integrated into `dev` by fast-forward from `audit/cybergram-message-states`, commits kept separate: DEBUG-only fixture `b2bfdd4180377563ff1cc53a8deb3abaf8fa7dea`, audit/docs `6c682e15385c07dd4cf28cf1bd69567e0e2692c1`, semantic correction docs-only `2e9a6a1d066975727cbb4b3888c490a8acbf798a`. Result after semantic correction: **14 PASS / 0 CONFIRMED DEFECT / 6 DESIGN-OPEN / 15 UNTESTED** across 35 cases; A/P tiers open.

Goal: replace the vague item “message edge cases” with an evidence matrix assigning every visible mismatch to its real owner. No production rendering changes were made in B2.

Current known boundaries (confirmed by the audit):

- `MessageDrawable`: body geometry; Cybergram covers `TYPE_TEXT` / `TYPE_MEDIA`; the angular work is additive-only versus upstream `master` (+111/-0), and grouped joins rely on near-corner cuts plus cell-side slice clipping;
- `TYPE_PREVIEW`: both production call sites are theme-preview surfaces (`Theme.createThemePreviewImage`, `ThemePreviewDrawable`); remaining rounded is correct there, so B3 is closed;
- `ReplyMessageLine`: reply/quote/link/contact plates and bars; rounded, no Cybergram seam — measured and `DESIGN-OPEN`, not a defect;
- `ReactionsLayoutInBubble`: reaction measure/draw/touch and pill geometry (`ReactionButton.drawRoundRect`); rounded, no Cybergram seam — measured and `DESIGN-OPEN`, not a defect;
- `ChatMessageCell`: caller/layout integration, not an excuse for an omnibus restyle — it consumes the angular path and owns no geometry defect.

The debug probe `CybergramB2MessageStatesFixture` (debug source set only) keeps the geometry part of this audit reproducible without a Telegram account.

### B3 — conditional MessageDrawable TYPE_PREVIEW geometry

Status: `CLOSED / NOT REQUIRED`.

B2 identified every `TYPE_PREVIEW` instantiation: `Theme.java:7362` (inside `createThemePreviewImage`, only caller `MessagesController.java:9119`) and `Components/ThemePreviewDrawable.java:78` (`.attheme` thumbnail via `ImageLoader.java:882`). No normal messaging surface uses it, and its provider bypass/slot/no-invalidation behaviour exists for standalone preview rasterization.

Therefore the deliberate `TYPE_PREVIEW` exclusion in `MessageDrawable` stays, and **no B3 implementation task is created**. Do not reopen this merely for symmetry with text/media.

### B4 — reply/reaction styling

Status: `DESIGN-OPEN / NOT AUTHORIZED`.

Owner-proven and measured: `ReplyMessageLine.drawBackground` plates and `ReactionsLayoutInBubble.ReactionButton.drawRoundRect` pills are circular while the message bodies are 45-degree chamfered. Classified as `DESIGN-OPEN`, **not** as a confirmed defect: `docs/CYBERGRAM_UI_SPEC.md` requires angular **outer** message silhouettes and the survival of replies/reactions, and names *large* rounded/glass Material capsules as the anti-target — it does not require compact internal semantic controls (reply plate/bar, reaction pill) to become angular.

No production implementation spec is written. `ReplyMessageLine.java` and `ReactionsLayoutInBubble.java` must not be modified on this basis. If a design ruling later requires angularity, split the work by actual owner (reply plate and reaction pill are separate bounded passes unless one seam is proven), and require an explicit per-instance opt-in in addition to the central gate, because both classes are shared well beyond the message flow (`StoryCaptionView`, rich-text editors, `ChatActionCell`, `ActionBarMenuItem`/`SearchTagsList`). Closure of the authenticated matrix items in B2 (reply layout, reaction interaction, metadata, service/date adjacency) would also be required before such work could be validated.

B4 remains `DESIGN-OPEN / NOT AUTHORIZED` after the 2026-09-14 B6 integration reconciliation; nothing in the B6 integration changes B4's status, and next product work must not silently start B4.

### B5 — service/date ownership and geometry audit

Status: `INTEGRATED / AUDIT COMPLETE / STATIC OWNERSHIP PASS / E BUILD+RUNTIME+VISUAL-DENSITY PASS / STRATEGY B SELECTED / A RICH+AUTHENTICATED CASES PENDING / P NOT REQUIRED FOR AUDIT`.

Spec: `docs/passes/B5_SERVICE_DATE_AUDIT.md`. Evidence and B6 proposal: `docs/B5_SERVICE_DATE_AUDIT_2026-09-13.md`.

Integrated on `dev` against base `2db3b48e7f3425518278a909ff65594a5410962a` by fast-forward from `audit/cybergram-service-date`, commits kept separate (no squash), integration final SHA `a5ab7c0de81738e89282dd441ca5f845294c5708`:

- DEBUG-only fixture `34aba0884` and debug-only measurement correction `4ae5a973ae953700cc209d520136fde461eef6a0` (debug source set only; the correction fixed a fixture plate-width measurement bug by reading raw `ChatActionCell` background bounds);
- audit/docs `9b3793230` and evidence-correction docs-only `a5ab7c0de`.

Static ownership is narrowed to the ordinary `ChatActionCell.backgroundPath` pipeline. `ChatActionCell` also owns many rich cards/actions, so rich gift/offer/community/wallpaper/birthday/story states, bot buttons/ribbons and the suggested-post-approval override remain on their separate upstream paths.

E validation passed on the real host via Remote Desktop Commander (not the DSH sandbox): the existing AVD `Cybergram_API36` booted as `emulator-5554`, normal app launch followed by the B5 fixture, FATAL/ANR = 0, final APK SHA-256 `e4411dd071f8600a58bfa6b9ff08044d0d3b0dc24fad8db99bb10f906dd556ad`, corrected screenshot `.local-artifacts/b5/b5_corrected.png` (git-excluded). The E visual-density gate passed, so **Strategy B** (one compact enclosing `CybergramBubbleDrawable.buildPath(...)` plate) is selected. No production rendering change was made: the production diff from base over `TMessagesProj/src/main` is empty. A-tier rich/authenticated cases remain pending; P was not required for this audit.

### B6 — ordinary angular plain service/date plate

Status: `INTEGRATED ON dev / BOUNDED PRODUCTION SEAM (ChatActionCell.java ONLY) / INDEPENDENT REVIEW SAFE_MINIMAL_SEAM / E BUILD+INSTALL+RUNTIME+VISUAL PASS / E-CONTROL PASS / E-RICH UNAVAILABLE PRE-AUTH / A PENDING / P NOT REQUIRED / RELEASE INSTALL COMPATIBILITY OPEN FOR REDMI MIUI`.

Spec source: `docs/B5_SERVICE_DATE_AUDIT_2026-09-13.md` §5 (Strategy B, one compact enclosing `CybergramBubbleDrawable.buildPath(...)` plate). Evidence and reconciliation record: `docs/B6_SERVICE_DATE_IMPLEMENTATION_2026-09-14.md`.

Integrated on `dev` by **ff-only** fast-forward (no squash, no merge commit) from branch `feature/cybergram-service-date-angular`, HEAD `53dd1368e478ce6a1f9845d1a1797ecfa4e3e0fc`, base `origin/dev` `cefa15eb7ba42d32b60d31ecf16d626f356344d5`, integration final SHA `0c4172346764c5622a5cdbfa06a4ce624d3cd56a`:

- production commit `f86ef812bb585db7c753335c7f573406e5129323` — `TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatActionCell.java` only, **+22/−1**; Strategy B is a minimal additive seam placed **after** upstream ordinary path generation, so the upstream ordinary two-pass build stays byte-identical and is superseded only inside `invalidatePath`;
- debug-only control commit `53dd1368e478ce6a1f9845d1a1797ecfa4e3e0fc` — B6 non-Cybergram fixture/wrapper in `TMessagesProj_App/src/debug/.../CybergramShowcaseActivity.java` only;
- superseded pre-review iteration `1f31cfa91a8b734e414ca960f2124659ba45bf2f` (side branch `dev-b6-20260913-1f31cfa91`, +136/−100) is retained only as provenance for the earlier prerelease asset; it is not an ancestor of the current HEAD;
- evidence/docs commit `0c4172346764c5622a5cdbfa06a4ce624d3cd56a` — docs-only reconciliation, integration HEAD on `dev`.

Gate: `CybergramTheme.useAngularMessageGeometry(themeDelegate)` combined with `!isButtonLayout(currentMessageObject)` and `!isMessageActionSuggestedPostApproval()` — the existing central gate on the provider actually available at the draw site, no theme-name or colour heuristic. Explicitly excluded and left on upstream paths: `isButtonLayout`/rich gift/offer/community/wallpaper/birthday/story states, new-style cards, bot buttons/ribbons, the suggested-post-approval override, reactions, and non-Cybergram presentation. Paints/shaders/dim, measurement, interaction and the rich `backgroundPath2`/card/ribbon paths are preserved; `docs/CYBERGRAM_UI_SPEC.md` was not modified.

Evidence (details and exact artifacts in the B6 record): x86_64 production-commit build BUILD SUCCESSFUL, APK `73,306,179` bytes, SHA-256 `e415b00305d83122575bd81a18061d0e0b935487cbf0b9a7871388c0407eb621`, install + normal launch + real `ChatActionCell` fixture on `emulator-5554` stable with no FATAL/ANR/process death, plate widths `98/160/288/230/310/642` px → visual Strategy B pass; arm64-v8a production-commit build BUILD SUCCESSFUL, `68,452,403` bytes, SHA-256 `9d5f936b820766aec4f6c825e8625c147820b2fbc81b9690fe419ad7364e71c8` (**build evidence only**); debug-control x86_64 E run BUILD SUCCESSFUL in 29s, `68,948,592` bytes, SHA-256 `ef97f76daf99bbd327287fb25c3a08edefa09b168f0ba9b439ab8b52fac12452`, install success, emulator theme temporarily Cybergram→Blue and restored, non-Cybergram provider reproduces the upstream rounded ordinary path, `CRASH_MATCHES=0` → **E-control PASS**; universal 4-ABI compatibility build on the branch HEAD BUILD SUCCESSFUL in 1m 23s, `113,631,225` bytes, SHA-256 `0c781a6b71f13034b36586b4c596a48a3b8719e108d51195dc89c305a78e12f0`, ABIs `arm64-v8a/armeabi-v7a/x86/x86_64`, minSdk 21 / targetSdk 36, signed v1+v2, published as a separate prerelease asset — **compatibility/build evidence, not device runtime validation**.

Independent review verdict `SAFE_MINIMAL_SEAM`, including: upstream two-pass side effects are safely superseded only inside `invalidatePath`; `ThemePreviewActivity` foreign app-theme `SCREEN_TYPE_PREVIEW` has no `ChatActionCell`/`contentType == 1` row today, so **no additional `ThemePreviewActivity` production opt-out is required now**; latent caveat recorded — a future foreign-theme preview row that did instantiate a `ChatActionCell` would need an explicit opt-out, because the global fallback is not preview-scoped.

Open items: **A tier pending** (authenticated ordinary service/date rows and all rich/special rows; service-message reactions re-check); **E-rich unavailable pre-auth** and not faked; **P not required** unless a device/OEM runtime defect appears; **integrated on `dev`** (ff-only, no squash and no merge commit); **open release/install compatibility issue** — the universal 4-ABI APK is the explicit compatibility probe for the Redmi Note 10S / MIUI 14.0.4 install report (see R1).

### B7 — optional chat-canvas HUD/background layer

Status: `SPEC PREPARED / NOT AUTHORIZED`.

Spec: `docs/passes/B7_CHAT_CANVAS_HUD.md` (prepared 2026-09-14; writing it grants nothing).

B7 is **optional**. Its stated precondition is met at E tier — B1 is integrated, message bodies are angular (B2) and ordinary service/date plates are angular (B6) — so the backlog's earlier "do not start before B1 and the primary message/service presentation are coherent" gate no longer blocks preparation.

Ownership was re-audited rather than inferred, as this entry previously required. Verified 2026-09-14 against `dev` `23882dbf00e23b4a521f1185aeea2556d1c5afe7`:

- chat canvas host is `ChatActivity.ChatActivityFragmentView` (`ChatActivity.java:17085`), used **only** by `ChatActivity`; `ChannelAdminLogActivity` has its own separate nested class of the same name (`ChannelAdminLogActivity.java:4493`), so a seam here does not leak into the admin-log screen;
- the wallpaper layer is `SizeNotifierFrameLayout.BackgroundView` (`:166`, public field `backgroundView` at `:92`), installed lazily at **child index 0** by `setBackgroundImage` (`:363-368`) and driven from `ChatActivity.updateBackground()` (`:44179-44190`);
- `chatListView` is added at `ChatActivity.java:6979`, i.e. the layer must sit above the wallpaper and below the message list;
- `SizeNotifierFrameLayout` itself is extended by 19 classes and **must not** receive the seam;
- the landed precedent to copy is `CybergramHeaderDecorationView` (non-interactive, runtime-gated in `onDraw`, reuses `CybergramBubbleDrawable.buildPath`, installed by `ChatActivity.java:8986`).

Recommended seam (not executed): a dedicated non-interactive, runtime-gated child View inserted between the wallpaper and `chatListView`, with the insertion index derived from `indexOfChild(backgroundView)`; the `dispatchDraw`-override and base-class options are recorded as rejected with reasons.

B7 must be non-interactive, must draw nothing outside Cybergram presentation, must not reduce message readability or wallpaper/media behaviour, and must not become a large opaque surface or carry microtext/fake security claims (`docs/CYBERGRAM_UI_SPEC.md` lines 19, 21, 23, 25, 55, 113, 115, 117). Its ownership must be re-verified again at execution time — do not trust the prepared copy.

The 2026-09-15 owner design target is reconciled in `docs/DESIGN_TARGET_RECONCILIATION_2026-09-15.md`. That document re-verified B7's ownership anchors at `39c301bb4` (they hold) and corrected two contract details: `20` files match `extends SizeNotifierFrameLayout` (19 hosts besides `ChatActivity`), and index `1` is not free at runtime because `videoPlayerContainer` is inserted there (`ChatActivity.java:12157`). B7 draws structural marks only and no label copy; decision D3 has since been ruled (neutral Latin technical copy including meaningless gibberish is allowed, security/live-network claims stay banned), so the copy blocker is cleared while B7 itself remains `NOT AUTHORIZED`.

### B8 — secondary client surfaces and onboarding

Status: `DEFERRED / STAGE F`.

Settings, profiles, media viewers, calls, login/onboarding and secondary sheets come only after the primary messaging flow is coherent. The current emulator screenshot confirms pre-auth onboarding is still essentially upstream Telegram; that is known, not a regression in the current primary-flow scope.

Split secondary work by surface rather than creating a global theme rewrite.

### Design target alignment (new input, 2026-09-15)

Status: `ANALYSIS / OWNER DECISIONS PENDING`.

`design/DESIGN_TARGET.md` plus the owner reference image `design/references/design-target-hex-chat.jpg`
(commit `39c301bb4`) were reconciled against the design authority and the landed state in
`docs/DESIGN_TARGET_RECONCILIATION_2026-09-15.md`. That document's pre-flight also re-verified B7's
ownership at `39c301bb4` and corrected two contract details.

The reference **confirms** the landed direction: cyan/amber self/peer roles, near-black palette, thin
bubble outlines, compact service/date plate, composer shape, and edge-only non-interactive decoration.

It **conflicts with or adds decisions about**:

- **D1 — message silhouette**: the reference asks for rounded ~8 px corners with a tail, while
  `docs/CYBERGRAM_UI_SPEC.md` lines 71-74 require clipped/angled corners with the tail removed, and that
  is what landed and passed B2. Adopting the reference would re-open Stage C and supersede recorded B2
  evidence; it would need a new bounded spec, not an opportunistic edit. **Ruled by the owner on
  2026-09-15: keep the landed chamfer** — the angular silhouette stands, Stage C stays closed and no
  code or spec change follows.
- **D2 — two palette hues** (amber `#FFB300` vs landed `#E8D93A`; red `#FF003C` vs landed `#FF2E46`).
  Cheap and central, but should be decided from an E-tier visual comparison, not from JPEG-sampled hex.
  **Ruled by the owner on 2026-09-15: adopt the reference hues** (`#FFB300` / `#FF003C`, muted
  `#6B7A8A`) — implemented in `CybergramTheme` + `cybergram.attheme`; the A/B recommendation is
  superseded.
- **D3 — service-label copy**: the reference's `SECURE CHAT`, `END-TO-END`, lock icon and
  `CONNECTION STABLE` are the class of claim banned by `docs/CYBERGRAM_UI_SPEC.md` line 117. The
  *layer* is sanctioned by line 113; the *copy* is not. Until D3 is ruled, no label copy is approved.
  **Ruled by the owner on 2026-09-15 (owner's own label `D2`): neutral Latin technical copy is allowed,
  including deliberately meaningless technical gibberish; every security and live-network claim stays
  banned.**
- **D4 — default wallpaper** (the owner's own label `D3`): a default replaceable Cybergram wallpaper
  with minimal framing is **allowed and expected**; a user wallpaper always overrides it.
  **Implemented 2026-09-16** as a `ColorDrawable` subclass returned from
  `ChatActivity.ChatActivityFragmentView.getNewDrawable()` — no child-view insertion, no z-order
  change, gated on Cybergram presentation **and** `ThemeInfo.overrideWallpaper == null` **and** a plain
  `ColorDrawable` source. Pass file: `docs/passes/D4_DEFAULT_BACKDROP.md`.
- **D5 — Cyrillic-first typography**: **verified on device 2026-09-16.** The font `sans-serif-condensed`
  actually resolves to on `Cybergram_API36` carries 256/256 Cyrillic, 48/48 Cyrillic Supplement and
  32/32 Cyrillic Ext-A, so the landed chrome already satisfies the ruling; the reference's own
  `Rajdhani` and `Share Tech Mono` have no Cyrillic and are disqualified. A geometric OFL face needs an
  owner-supplied asset. Record: `docs/D5_TYPOGRAPHY_2026-09-16.md`.
- **D6 — bubble spacing**: **implemented 2026-09-16** as a paint-only, join-aware inset in
  `MessageDrawable.generateCybergramPath(...)`; measurement, layout, scroll, metadata and grouped
  joins are untouched and `TYPE_MEDIA` is excluded. Pass file: `docs/passes/D6_BUBBLE_SPACING.md`.

The binding record for these rulings, the owner's numbering clash, and the further taste rulings
(palette, Cyrillic-first font, bubble spacing, composer outline, header-tick removal) is
`docs/OWNER_DECISIONS_2026-09-15.md`, whose §8 records the 2026-09-16 follow-up round.

Also unowned from the reference: a bundled monospace HUD font (spec line 108 allows an open,
redistributable font later; nothing is bundled today, and the HUD label layer that would consume it is
B7, which is **not authorized**). The faint background pattern layer is no longer unowned: it was ruled
as D4 and implemented.

None of this authorizes a pass, and none of it changes B7's file scope. A pass does not authorize the
next pass.

### R1 — release identity / credentials / signing / Firebase / package policy

Status: `SEPARATE RELEASE TRACK`.

Do not mix with UI cleanup. Real credentials remain local/secret; package/application ID, signing and Firebase decisions require explicit release work.

Open install-compatibility item (B6 prerelease, non-rendering): a Redmi Note 10S / MIUI 14.0.4 user reports the arm64 APK did not install. The exact `INSTALL_FAILED_*` code is unavailable because the device is not currently reachable through `adb`, so **no ABI root cause is claimed**. The universal 4-ABI APK (`0c781a6b...`, §B6) is the explicit compatibility probe and has not been run on that device; because the package is `org.telegram.messenger.beta`, a signature/package conflict with an already-installed Telegram Beta remains a plausible unresolved cause. Closure requires the exact package-installer / `adb install` error text. This is a release/install issue, not a B6 rendering defect.

## Dependency order

B0 can be completed whenever an authenticated session is available and does not need to block B1 design/execution.

Recommended production order is B1 first (now integrated, with A/P tiers open), then the evidence-oriented B2 and B5 audits (which may run independently), followed only by the B3/B4/B6 tasks actually justified by those audits. B2 is complete with zero confirmed defects: B3 is closed and B4 is `DESIGN-OPEN / NOT AUTHORIZED`. B5 is integrated and selected Strategy B; B6's bounded implementation is **integrated on `dev`** (ff-only) at E tier, with A (and E-rich) still open. B7 now has a prepared contract but is **optional and NOT AUTHORIZED**; B8 remains deferred.

**The largest outstanding item is A-tier verification debt, not new presentation work.** B1 (main tabs) and B6 (ordinary service/date + rich states) are integrated at E tier, B0 (filter tabs) has been exercised at E tier and is **stopped on a confirmed defect** in the authenticated run, and B2 carries 15 `UNTESTED` account-dependent rows. The assumption that A-tier was unreachable has been **corrected**: the `Cybergram_API36` AVD hosts an authenticated session, and the first authenticated run on 2026-09-15 partially validated B1 (`docs/A_TIER_VALIDATION_2026-09-15.md`) and confirmed the B0 selected-chip defect. B2, B5 and B6 remain entirely untested on an authenticated surface (B0 *was* exercised on 2026-09-15 and stopped at the confirmed defect — see §B0); the tranches that require opening conversations carry a read-marking side effect and need an explicit owner decision. B7 does not reduce that debt, and preparing it is not a reason to defer closing it.

B4 stays `DESIGN-OPEN / NOT AUTHORIZED`: neither the B6 integration nor the B7 preparation changes anything about B4, and next product work must not silently start B4.

The 2026-09-15 owner design target is reconciled in `docs/DESIGN_TARGET_RECONCILIATION_2026-09-15.md`. It adds three owner decisions (D1 message silhouette, D2 palette hues, D3 service-label copy) and authorizes nothing: **D1, D2 and D3 are all ruled by the owner (2026-09-15)** — see `docs/OWNER_DECISIONS_2026-09-15.md`, which also records the owner's `D3` = default wallpaper (repo `D4`) and the further taste rulings. B7 remains the only unblocked implementation pass in this queue and is still NOT AUTHORIZED.

A pass does not authorize the next pass. The user chooses execution priority.

### Code-review follow-ups (R-series, recorded 2026-09-16)

A read-only review of the landed product code (`22dfacce0` and earlier Cybergram commits) produced five
bounded findings. **R-GATE was fixed on 2026-09-16** (§ below); the other four remain unauthorized and are
recorded so they are not lost.

- **R-GATE — the presentation gate read the DAY theme while the active theme may be the NIGHT theme
  (code review 2026-09-16; accessors confirmed at source). FIXED 2026-09-16 at E tier** — production
  commit `4629e98a3`, evidence `docs/R_GATE_IMPLEMENTATION_2026-09-16.md`. `CybergramTheme.isCybergramPresentation(...)`
  read `Theme.getCurrentTheme()` (`CybergramTheme.java:105`), which upstream returns as `currentDayTheme`
  (`Theme.java:6444-6446`), while `CybergramBackdropDrawable.isEligible(...)` reads
  `Theme.getActiveTheme()` (`CybergramBackdropDrawable.java:92`), which returns `currentTheme`
  (`Theme.java:6460-6462`). The two `ThemeInfo` objects differ whenever the night slot holds a different
  theme from the day slot, with two user-visible outcomes: (A) Cybergram selected as the **night** theme →
  the gate was false, so the whole Cybergram chrome and the D4 backdrop were suppressed even though
  Cybergram was the active theme; (B) Cybergram as the **day** theme with another night theme active → the
  gate was true, so Cybergram chrome and the D4 grid could appear over a non-Cybergram palette. The gate
  now reads `Theme.getActiveTheme()`, so both sites agree. E regression PASS (dialog chrome and the D4 grid
  in-chat); the two night-slot scenarios are **not** exercised (they need a persisted theme-slot mutation,
  deliberately not performed) and A/P remain open.
- **R-D6 — the `E < dp(3)` ceiling of `BUBBLE_GAP_EXTRA_DP` is documented but unenforced.** The value
  was raised from `2f` to `3f` in round 3, i.e. to exactly the documented ceiling. Re-measured on
  2026-09-17 (round 4; `docs/OWNER_DECISIONS_2026-09-17_ROUND4.md` §8): the painted top is
  `dp(3) + E` (drawable padding `dp(2)` at `MessageDrawable.java:564/630/907`, `bounds.top` `dp(1)`
  at `ChatMessageCell.java:20560/20643`). The top edge keeps 10 px, but the binding bottom edge — the
  time cluster at `layoutHeight − dp(6.5)`, or `− dp(7.5)` when grouped —
  (`ChatMessageCell.java:24505-24509`) has exactly 0 px standalone and −1 px inside the time box when
  grouped at E = 3f. The old emoji-only rationale does not bind: `TYPE_EMOJIS` sets
  `drawBackground = false`, so no bubble is painted for it. Recommended: `2.5f` (restores ≥1 px on
  both edges) plus an optional defensive clamp; the javadoc is corrected but the value is left to an
  owner ruling. **Unauthorized.**
- **R-STUB — D4 grid may decorate the per-chat default-theme stub.** `ChatActivity.java:44139` returns
  `new ColorDrawable(Color.BLACK)` for the `chatTheme.showAsDefaultStub` branch; with a null global
  `overrideWallpaper` this can pass `isEligible`, so the faint grid/frame can appear on that stub.
  Cosmetic only; decide whether a per-chat default theme is "a user wallpaper". **Unauthorized.**
- **R-PERF — the Cybergram bubble border is rebuilt and stroked uncached every frame.** Per-bubble,
  per-frame cost only; no correctness impact. **Unauthorized.**
- **R-OVERLAY — every `ChatActivity`, including non-Cybergram, now carries a full-size inert overlay
  view** (`CybergramHeaderDecorationView`) with a per-draw gate call. No functional change (the view is
  non-interactive); a minor always-on cost. **Unauthorized.**

R-D6, R-STUB, R-PERF and R-OVERLAY are recorded findings only. Acting on any of them still requires an
explicit owner authorization; B0-FIX and R-GATE were the two exceptions, authorized by the owner's
2026-09-16 instruction to continue the work and choose the tasks.

## Executor startup rule

For any pass:

1. fresh-fetch `dev` and `master`;
2. read `AGENTS.md`, `docs/CURRENT_STATE.md`, this file, the relevant `docs/passes/<PASS>.md`, and the applicable section of `docs/CYBERGRAM_UI_SPEC.md`;
3. record starting SHA and local `git status`;
4. do not destroy unknown local work;
5. use a feature/fix branch for production changes to high-risk shared components;
6. stay inside the allowed file scope; stop and report before broadening it;
7. preserve the exact non-Cybergram behaviour/presentation path;
8. run `git diff --check` and the smallest relevant checks;
9. use the emulator runbook for E validation when possible;
10. report exact machine evidence and unresolved validation tier gaps.

Historical arm64 Samsung evidence remains valid only for the revisions recorded in `docs/WORK_STATE.md`. The new x86_64 AVD is the default remote development/runtime baseline, not proof of physical-device behaviour.
