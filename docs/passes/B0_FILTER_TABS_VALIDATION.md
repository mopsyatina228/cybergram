# B0 — final FilterTabs authenticated validation

Status: PARTIAL — E BASELINE PASSED / AUTHENTICATED FILTER-TABS MATRIX PENDING

Type: validation only

Design authority: `docs/CYBERGRAM_UI_SPEC.md`

Planning/status authority: `docs/EXECUTION_BACKLOG.md`

Current known product result: `52b8e219729d0a90dd3335165cf4ef44acf46e5e`, contained by later documentation-only commits on `dev`.

## What is already proven

On 2026-09-11 the current product tree was validated from repository HEAD `5663bf329d9d78bad5a991350740fe51912f88fb`; the executor confirmed no product-source difference from product cut `52b8e219729d0a90dd3335165cf4ef44acf46e5e`.

Emulator baseline:

- AVD `Cybergram_API36`;
- Android 16 / API 36 / Google APIs / `x86_64`;
- build `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64` succeeded;
- APK size `68,939,062` bytes;
- APK SHA-256 `66055766015812f92a7de4e396379671fca741b30d3b44b15995066699edef61`;
- `org.telegram.messenger.beta` installed successfully;
- normal onboarding launch remained alive;
- DEBUG `CybergramShowcaseActivity` rendered successfully;
- no immediate FATAL/ANR observed in either validation window.

This proves current-tree buildability, installability and basic API 36 runtime. It does **not** prove `FilterTabsView`, because the emulator session was unauthenticated and the production dialogs/filter surface was not reached.

## Remaining mission

Exercise the final landed Cybergram `FilterTabsView` on an authenticated Telegram UI and close its interaction/visual validation matrix. Do not modify production code during B0.

B0 is no longer a generic build-environment task. The build baseline exists. The remaining gap is surface-specific authenticated validation.

## Startup

1. Fetch `dev` and `master`.
2. Read `AGENTS.md`, `docs/CURRENT_STATE.md`, `docs/EXECUTION_BACKLOG.md`, this file and the filter-tabs sections of the checkpoint/UI spec.
3. Check local `git status` and record it.
4. Record exact tested `dev` SHA and confirm whether product files differ from the preserved product cut.
5. Do not reset/discard unknown local work.

## Static checks

Run or confirm:

- `git diff --check`;
- `python tools/validate_cybergram_theme.py`;
- `FilterTabsView.java` still gates Cybergram presentation through `CybergramTheme.isCybergramPresentation(resourcesProvider)`;
- Cybergram still owns only panel/selected plate/angular clip/presentation;
- non-Cybergram rounded/blurred rendering remains present;
- scrolling/reorder/delegate state machines remain upstream-owned.

If product code changed after the preserved cut, rebuild before authenticated testing. If product code is still byte-identical and the already-recorded E artifact is the exact tested product tree, the existing E baseline may be cited rather than rebuilt solely for ceremony.

## Authenticated validation target

An authenticated Android emulator is acceptable for generic `FilterTabsView` behaviour/visual validation. A physical device adds P confidence but is not required merely to prove this non-OEM-specific component.

Use `org.telegram.messenger.beta`; never touch official `org.telegram.messenger`.

Exercise the actual production filter/folder tabs:

- selected and unselected tabs;
- tapping between tabs;
- horizontal overflow/scroll if enough folders exist;
- page swipe/manual interpolation;
- unread counters where available;
- long-press/menu behaviour;
- edit/reorder/delete mode where available;
- Cybergram visual: dark chamfered outer panel and selected plate, no old large blurred/rounded pill;
- switch to a non-Cybergram theme when practical and verify upstream rounded/blurred presentation is restored;
- check logcat for FATAL/ANR around the matrix;
- capture screenshots of at least Cybergram selected/unselected states.

## Physical-device confidence

When the Samsung SM-A256E is available, a representative filter-tabs smoke is useful before release confidence is claimed. Do not mislabel emulator evidence as Samsung/OEM evidence.

## Stop rule

If a defect appears, record exact reproduction and stop. Do not fix production code inside B0. A defect becomes a separately authorized bounded fix.

## Success condition

B0 becomes `PASS` when:

- static checks are clean for the exact tested source;
- current product tree has valid build/runtime evidence (the 2026-09-11 E baseline may satisfy this when still exact);
- the authenticated production `FilterTabsView` interaction matrix is exercised without functional regression;
- Cybergram and non-Cybergram presentation boundaries behave as intended;
- no immediate FATAL/ANR is observed.

P/physical-device confidence may remain separately recorded as pending if generic authenticated behaviour has passed on the API 36 emulator.

## Required handoff report

Return:

- tested SHA and product-tree relation to `52b8e219...`;
- local status;
- static-check results;
- E evidence reused or fresh build/APK evidence;
- authenticated target type/device/API/package;
- interaction cases exercised;
- screenshot/artifact paths;
- FATAL/ANR result;
- verdict `PASS`, `FAIL`, or `PARTIAL/UNVALIDATED`;
- physical-device confidence status;
- any defect reproduction, without opportunistic fixes.
