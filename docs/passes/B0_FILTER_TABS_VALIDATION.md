# B0 — final FilterTabs validation

Status: READY TO EXECUTE

Type: validation only

Design authority: `docs/CYBERGRAM_UI_SPEC.md`

Planning authority: `docs/EXECUTION_BACKLOG.md`

Current known product result: `52b8e219729d0a90dd3335165cf4ef44acf46e5e`, contained by later documentation-only commits on `dev`.

## Mission

Produce exact build/device evidence for the final landed Cybergram `FilterTabsView` result. Do not modify production code during this pass.

## Startup

1. Fetch `dev` and `master`.
2. Read `AGENTS.md`, `docs/CURRENT_STATE.md`, `docs/EXECUTION_BACKLOG.md` and the filter-tabs section of `docs/PROJECT_CHECKPOINT_2026-09-11.md`.
3. Check local `git status` and record it.
4. Check out current `dev` and record `git rev-parse HEAD`.
5. Do not reset/discard unknown local work without explicit human instruction.

## Static checks

Run:

- `git diff --check`
- `python tools/validate_cybergram_theme.py`

Inspect `TMessagesProj/src/main/java/org/telegram/ui/Components/FilterTabsView.java` and confirm that:

- Cybergram presentation is gated by `CybergramTheme.isCybergramPresentation(resourcesProvider)`;
- Cybergram uses its own panel/selected plate/angular clip;
- Cybergram suppresses the visible blurred background only in its own branch;
- the upstream rounded/blurred branch remains present for non-Cybergram presentation;
- scroll/reorder/delegate behaviour was not replaced by Cybergram-specific machinery.

## Build

Use the locally appropriate Gradle wrapper invocation for the established task:

`:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a`

Historical successful environment was Gradle 8.11.1, AGP 8.10.1, JDK 17, SDK 36, NDK 27.2.12479018. Treat that as reference, not proof of the current machine.

Expected historical APK path:

`TMessagesProj_App/build/outputs/apk/afat/debug/app.apk`

Record:

- full command;
- exit/result;
- APK path;
- APK byte size;
- APK SHA-256.

If the build cannot run, stop with status `UNVALIDATED — BUILD ENVIRONMENT BLOCKED`. Do not convert static inspection into a claimed pass.

## Device validation

Historical target is Samsung SM-A256E / Android 16 / arm64-v8a and debug package `org.telegram.messenger.beta`.

Install/update the debug APK without touching official `org.telegram.messenger`.

Record device model, Android version, package name/version, install result and launch result.

Exercise the final folder/filter tabs:

- active/unselected tabs;
- tapping between tabs;
- horizontal scrolling/overflow if enough tabs exist;
- page swipe/manual interpolation;
- unread counters if available;
- long-press/menu behaviour;
- edit/reorder/delete mode if available;
- Cybergram visual: dark chamfered outer panel and selected plate, no old large blurred/rounded pill;
- non-Cybergram visual retains upstream rounded/blurred behaviour when practical to test.

Capture at least one screenshot of the Cybergram result. Check logs for FATAL/ANR around the test window.

## Stop rule

If a defect appears, record the reproduction and stop. Do not fix production code on this pass.

## Success condition

`PASS` requires successful build plus real-device evidence for the exact checked-out SHA and no functional regression in the exercised filter-tab behaviours.

After PASS, repository documentation may be updated in a separate documentation commit:

- append exact evidence to `docs/WORK_STATE.md`;
- change `docs/CURRENT_STATE.md` so the final filter-tabs patch is no longer marked validation pending.

## Required handoff report

Return exactly these facts:

- starting/current tested SHA;
- local status before work;
- commands run;
- static-check results;
- build result;
- APK path/size/SHA-256;
- device/OS/package;
- interaction cases exercised;
- screenshot/artifact paths;
- FATAL/ANR result;
- final verdict: `PASS`, `FAIL`, or `UNVALIDATED`;
- any defect reproduction, without opportunistic fixes.