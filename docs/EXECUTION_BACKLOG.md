# Cybergram execution backlog

Status: active planning / execution handoff

Last reconciled: 2026-09-12

Repository: `mopsyatina228/cybergram`

Upstream baseline: Telegram Android 12.10.1 (7038), `master` at `62b56a07ca7e30e39f7fd00a6728d6bbd716ca1c`.

Preserved product-code cut: `52b8e219729d0a90dd3335165cf4ef44acf46e5e`.

Current documentation/validation HEAD at the start of this reconciliation: `5663bf329d9d78bad5a991350740fe51912f88fb`.

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

Status: `READY — AUDIT/DEBUG ONLY`.

Spec: `docs/passes/B2_MESSAGE_STATE_AUDIT.md`.

Goal: replace the vague item “message edge cases” with an evidence matrix assigning every visible mismatch to its real owner. No production rendering changes are authorized in B2.

Current known boundaries:

- `MessageDrawable`: body geometry; Cybergram currently covers `TYPE_TEXT` / `TYPE_MEDIA` only;
- `TYPE_PREVIEW`: deliberately excluded until actual product callers/desired behaviour are proven;
- `ReplyMessageLine`: reply-specific paths/colour state;
- `ReactionsLayoutInBubble`: reaction layout/drawing/state;
- `ChatMessageCell`: caller/layout integration, not an excuse for an omnibus restyle.

Extend DEBUG showcase fixtures where useful so the audit is reproducible without a Telegram account.

### B3 — conditional MessageDrawable TYPE_PREVIEW geometry

Status: `NOT AUTHORIZED — DERIVE FROM B2`.

Create this implementation pass only if B2 proves a real Cybergram product surface using `TYPE_PREVIEW` should be angular. Do not enable it merely for conceptual symmetry with text/media.

Likely owner: `MessageDrawable.java` only, unless B2 produces contrary evidence.

### B4 — conditional reply/reaction/message-owner fixes

Status: `NOT AUTHORIZED — DERIVE FROM B2`.

Split by actual owner. Do not create one broad “fix message UI” change. A reply defect and a reaction defect are separate bounded passes unless evidence proves they require the same seam.

### B5 — service/date ownership and geometry audit

Status: `READY — AUDIT/DEBUG FIRST`.

Spec: `docs/passes/B5_SERVICE_DATE_AUDIT.md`.

Static ownership is already narrowed to the ordinary `ChatActionCell.backgroundPath` pipeline. `ChatActionCell` also owns many rich cards/actions, so B5 must decide the plain service/date angular strategy and prove which states are ordinary versus special before production code changes.

Use single-line date, multi-line ordinary service text and representative rich/special actions in the evidence matrix.

### B6 — conditional angular plain service/date plate

Status: `NOT AUTHORIZED — DERIVE FROM B5`.

Expected owner: `ChatActionCell.java`, limited to the ordinary text/date background path. Rich gifts/cards/buttons/ribbons remain upstream unless B5 explicitly proves a separate need.

Preserve text measurement, line-width calculations, paints, darken/dim layers, touch behaviour and special-state geometry.

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

Recommended production order is B1 first (now integrated, with A/P tiers open), then B2 and B5 audits (which may run independently because they are evidence-oriented), followed only by the B3/B4/B6 tasks actually justified by those audits. B7 and B8 remain later.

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
