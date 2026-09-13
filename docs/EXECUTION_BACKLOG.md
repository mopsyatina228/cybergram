# Cybergram execution backlog

Status: active planning / execution handoff

Last reconciled: 2026-09-13

Repository: `mopsyatina228/cybergram`

Upstream baseline: Telegram Android 12.10.1 (7038), `master` at `62b56a07ca7e30e39f7fd00a6728d6bbd716ca1c`.

Preserved product-code cut: `52b8e219729d0a90dd3335165cf4ef44acf46e5e`.

Current documentation/validation HEAD at the start of this reconciliation: `a5ab7c0de81738e89282dd441ca5f845294c5708`.

This file is the status/dependency index for remaining Cybergram work. Detailed implementation instructions live in `docs/passes/`; source-ownership evidence lives in `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md`; design authority remains `docs/CYBERGRAM_UI_SPEC.md`; chronological machine evidence remains `docs/WORK_STATE.md`.

Do not duplicate full pass instructions here. Humans are already quite capable of creating two contradictory copies of the same plan without our assistance.

## Operating contract

Cybergram remains a presentation fork. Preserve Telegram behaviour and state machines. Prefer a narrow Cybergram-only presentation seam around an upstream renderer rather than replacing or refactoring the component that owns behaviour.

Use `CybergramTheme.isCybergramPresentation(Theme.ResourcesProvider provider)` as the central presentation gate. Do not scatter theme-name checks and do not infer Cybergram from colour values.

Use `CybergramBubbleDrawable.buildPath(...)` / `CybergramHudDrawable` for angular geometry instead of introducing another chamfer implementation.

Any shared component that serves both the main Cybergram surface and unrelated Telegram surfaces requires an explicit local opt-in in addition to the central theme gate. The important current examples are `GlassTabView` (main bottom navigation vs attach/bot tabs and other `createMainTab` callers) and `MainTabsLayout` (main bottom navigation vs `StatisticActivity`); a gate-only branch inside either class would leak main-navigation styling into unrelated surfaces.

For high-risk upstream files, do not combine presentation changes with cleanup/refactoring.

## Validation baseline established 2026-09-11

A reproducible emulator path now exists in `docs/runbooks/CYBERGRAM_EMULATOR_VALIDATION.md`.

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

Status: `PARTIAL — E BASELINE PASSED / AUTHENTICATED FILTER-TABS MATRIX PENDING`

Type: validation only.

Spec: `docs/passes/B0_FILTER_TABS_VALIDATION.md`.

The 2026-09-11 emulator run proves the exact current product tree builds, installs and starts. It did not reach an authenticated dialogs screen, so it does not prove the final `FilterTabsView` interaction/visual matrix. B0 now consists only of closing that remaining authenticated-surface gap. No production fixes are authorized inside B0.

B0 no longer blocks static design of B1. It blocks only claims that final filter tabs are fully runtime-validated.

### B1 — flat/angular main bottom navigation

Status: `INTEGRATED / STATIC PASS / E PASS / A PENDING / P PENDING`.

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

### B5 — service/date ownership and geometry audit

Status: `INTEGRATED / AUDIT COMPLETE / STATIC OWNERSHIP PASS / E BUILD+RUNTIME+VISUAL-DENSITY PASS / STRATEGY B SELECTED / A RICH+AUTHENTICATED CASES PENDING / P NOT REQUIRED FOR AUDIT`.

Spec: `docs/passes/B5_SERVICE_DATE_AUDIT.md`. Evidence and B6 proposal: `docs/B5_SERVICE_DATE_AUDIT_2026-09-13.md`.

Integrated on `dev` against base `2db3b48e7f3425518278a909ff65594a5410962a` by fast-forward from `audit/cybergram-service-date`, commits kept separate (no squash), integration final SHA `a5ab7c0de81738e89282dd441ca5f845294c5708`:

- DEBUG-only fixture `34aba0884` and debug-only measurement correction `4ae5a973ae953700cc209d520136fde461eef6a0` (debug source set only; the correction fixed a fixture plate-width measurement bug by reading raw `ChatActionCell` background bounds);
- audit/docs `9b3793230` and evidence-correction docs-only `a5ab7c0de`.

Static ownership is narrowed to the ordinary `ChatActionCell.backgroundPath` pipeline. `ChatActionCell` also owns many rich cards/actions, so rich gift/offer/community/wallpaper/birthday/story states, bot buttons/ribbons and the suggested-post-approval override remain on their separate upstream paths.

E validation passed on the real host via Remote Desktop Commander (not the DSH sandbox): the existing AVD `Cybergram_API36` booted as `emulator-5554`, normal app launch followed by the B5 fixture, FATAL/ANR = 0, final APK SHA-256 `e4411dd071f8600a58bfa6b9ff08044d0d3b0dc24fad8db99bb10f906dd556ad`, corrected screenshot `.local-artifacts/b5/b5_corrected.png` (git-excluded). The E visual-density gate passed, so **Strategy B** (one compact enclosing `CybergramBubbleDrawable.buildPath(...)` plate) is selected. No production rendering change was made: the production diff from base over `TMessagesProj/src/main` is empty. A-tier rich/authenticated cases remain pending; P was not required for this audit.

### B6 — ordinary angular plain service/date plate

Status: `READY / AUTHORIZED FOR BOUNDED IMPLEMENTATION` — derived from the integrated B5 audit; **NOT IMPLEMENTED** in this docs pass.

Spec source: `docs/B5_SERVICE_DATE_AUDIT_2026-09-13.md` §5 (Strategy B, one compact enclosing `CybergramBubbleDrawable.buildPath(...)` plate).

Production scope stays narrow: ideally only `TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatActionCell.java` plus imports of the existing `CybergramBubbleDrawable`/`CybergramTheme` helpers, and only the ordinary service/date `backgroundPath`. The central gate is `CybergramTheme.useAngularMessageGeometry(themeDelegate)` combined with `!isButtonLayout(currentMessageObject)` and `!isMessageActionSuggestedPostApproval()`.

Explicitly excluded: `isButtonLayout`/rich gift/offer/community/wallpaper/birthday/story states, the suggested-post-approval special geometry, reactions, and non-Cybergram presentation. Preserve paints/shaders/dim, measurement, interaction and the rich `backgroundPath2`/card/ribbon paths. The `ThemePreviewActivity` provider-leak concern is a **B6 verification item**, not a reason to broaden scope in advance. `docs/CYBERGRAM_UI_SPEC.md` must not be modified.

### B7 — optional chat-canvas HUD/background layer

Status: `DEFERRED`.

Do not start before B1 and the primary message/service presentation are coherent. It must be non-interactive and must not reduce message readability or wallpaper/media behaviour.

Likely ownership must be re-audited at execution time around `ChatActivity` / chat-background layers. Do not infer a file scope from this backlog entry alone.

### B8 — secondary client surfaces and onboarding

Status: `DEFERRED / STAGE F`.

Settings, profiles, media viewers, calls, login/onboarding and secondary sheets come only after the primary messaging flow is coherent. The current emulator screenshot confirms pre-auth onboarding is still essentially upstream Telegram; that is known, not a regression in the current primary-flow scope.

Split secondary work by surface rather than creating a global theme rewrite.

### R1 — release identity / credentials / signing / Firebase / package policy

Status: `SEPARATE RELEASE TRACK`.

Do not mix with UI cleanup. Real credentials remain local/secret; package/application ID, signing and Firebase decisions require explicit release work.

## Dependency order

B0 can be completed whenever an authenticated session is available and does not need to block B1 design/execution.

Recommended production order is B1 first (now integrated, with A/P tiers open), then the evidence-oriented B2 and B5 audits (which may run independently), followed only by the B3/B4/B6 tasks actually justified by those audits. B2 is complete with zero confirmed defects: B3 is closed and B4 is `DESIGN-OPEN / NOT AUTHORIZED`. B5 is integrated and has selected Strategy B, which authorizes B6 for a bounded implementation; B6 remains `NOT IMPLEMENTED` in this docs pass. B7 and B8 remain later.

A pass does not authorize the next pass. The user chooses execution priority.

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
