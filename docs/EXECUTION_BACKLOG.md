# Cybergram execution backlog

Status: active planning / execution handoff

Last reconciled: 2026-09-14

Repository: `mopsyatina228/cybergram`

Upstream baseline: Telegram Android 12.10.1 (7038), `master` at `62b56a07ca7e30e39f7fd00a6728d6bbd716ca1c`.

Preserved product-code cut: `52b8e219729d0a90dd3335165cf4ef44acf46e5e`.

Current documentation/validation HEAD at the start of this reconciliation: `0c4172346764c5622a5cdbfa06a4ce624d3cd56a` on `dev`, equal to `origin/dev`. `dev` reached this point by **ff-only** integration of `feature/cybergram-service-date-angular` from base `cefa15eb7ba42d32b60d31ecf16d626f356344d5` — no squash and no merge commit — carrying production `f86ef812bb585db7c753335c7f573406e5129323`, debug control `53dd1368e478ce6a1f9845d1a1797ecfa4e3e0fc` and evidence/docs `0c4172346764c5622a5cdbfa06a4ce624d3cd56a`. B6 is **integrated on `dev`**.

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

Open install-compatibility item (B6 prerelease, non-rendering): a Redmi Note 10S / MIUI 14.0.4 user reports the arm64 APK did not install. The exact `INSTALL_FAILED_*` code is unavailable because the device is not currently reachable through `adb`, so **no ABI root cause is claimed**. The universal 4-ABI APK (`0c781a6b...`, §B6) is the explicit compatibility probe and has not been run on that device; because the package is `org.telegram.messenger.beta`, a signature/package conflict with an already-installed Telegram Beta remains a plausible unresolved cause. Closure requires the exact package-installer / `adb install` error text. This is a release/install issue, not a B6 rendering defect.

## Dependency order

B0 can be completed whenever an authenticated session is available and does not need to block B1 design/execution.

Recommended production order is B1 first (now integrated, with A/P tiers open), then the evidence-oriented B2 and B5 audits (which may run independently), followed only by the B3/B4/B6 tasks actually justified by those audits. B2 is complete with zero confirmed defects: B3 is closed and B4 is `DESIGN-OPEN / NOT AUTHORIZED`. B5 is integrated and selected Strategy B; B6's bounded implementation is **integrated on `dev`** (ff-only) at E tier, with A (and E-rich) still open. B7 and B8 remain later. B4 stays `DESIGN-OPEN / NOT AUTHORIZED`: the B6 integration changes nothing about B4 and next product work must not silently start B4.

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
