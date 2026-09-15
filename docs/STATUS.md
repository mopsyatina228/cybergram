# Cybergram repository status

Snapshot taken: 2026-09-15 21:35 +03:00

Snapshot base: `dev` at `09a6f2601e09497203fa4136c7853aeb286c4964`
(`docs: B7 pre-flight - resolve blur-capture and insertion-index questions`, committed 2026-09-15
21:30 +03:00), working tree clean.

State movement: this snapshot was first written against `9f8211503`, then refreshed after the design-target
commit `39c301bb4`, the `docs/STATUS.md` commit made by a concurrent agent (`88fdf88bd`), the design-target
reconciliation `7cf5b408d` and the B7 pre-flight `09a6f2601`. `dev` is now **4 commits ahead of
`origin/dev`**, which still points at `9f8211503`; nothing is pushed.

**This document is descriptive only.** It is a point-in-time snapshot of what exists, what is
unfinished and how validation is performed. It is **not** an authority: `docs/CURRENT_STATE.md` and
`docs/EXECUTION_BACKLOG.md` remain the status/dependency index, `docs/CYBERGRAM_UI_SPEC.md` remains the
design authority, and `docs/passes/` remains the bounded execution contracts. If this file disagrees
with any of them, they win. Nothing here authorizes a pass, a build, an agent run or a release.

## 1. Repository identity

| Item | Value |
|---|---|
| Upstream | `DrKLO/Telegram` |
| Baseline branch | `master` = Telegram Android 12.10.1 (7038), `62b56a07ca7e30e39f7fd00a6728d6bbd716ca1c` |
| Integration branch | `dev` |
| Preserved product-code cut | `52b8e219729d0a90dd3335165cf4ef44acf46e5e` |
| Current `dev` HEAD | `09a6f2601e09497203fa4136c7853aeb286c4964` |
| Working tree | clean; no untracked files; no stash; no `.git` lock files |
| Local vs remote `dev` | local is **4 commits ahead**: `origin/dev` = `9f8211503…` (verified through `gh api`); every 2026-09-15 commit is unpushed |
| Open pull requests | none; repository issues are disabled |
| Published prereleases | 4 (`dev-20260914-23882dbf0` newest, then `dev-b6-20260914-53dd1368e`, `dev-b6-20260913-f86ef812b`, `dev-b6-20260913-1f31cfa91`) |

Retained side refs: `feature/cybergram-main-tabs-flat` (`6802e0010`),
`feature/cybergram-service-date-angular` (`0c4172346`), `audit/cybergram-message-states` (`2db3b48e7`),
`audit/cybergram-service-date` (`a5ab7c0de`), `feature/cybergram-filter-tabs-flat`,
`automation/filter-tabs-runner`.

## 2. What already exists

Durably landed on `dev` (all of it inside or before the preserved product cut, plus two later passes):

- built-in Cybergram theme, `.attheme` palette and fresh-install day/night default that does not
  overwrite an existing user choice;
- shared Cybergram primitives: `CybergramTheme` (central presentation gate),
  `CybergramBubbleDrawable.buildPath(...)`, `CybergramHudDrawable`, `CybergramTypography`;
- angular text/media message-body geometry (`MessageDrawable` `TYPE_TEXT` / `TYPE_MEDIA`);
- chat header HUD decoration (`CybergramHeaderDecorationView`), flat chat header, composer frame and
  angular send control;
- dialogs row/selected-panel treatment, action-bar decoration, opt-in angular FAB, search presentation;
- flat/angular dialog filter/folder tabs (`FilterTabsView`);
- **B1** — flat/angular main bottom navigation (dark chamfered outer panel, angular selected plate and
  long-press selector) with explicit per-instance opt-in in `GlassTabView` and `MainTabsLayout`;
- **B6** — ordinary angular service/date plate: one compact chamfered plate for the ordinary
  `ChatActionCell.backgroundPath` only.

Product-code footprint since the preserved cut is deliberately small: **4 files, +177/−9** over
`TMessagesProj/src/main` — `ChatActionCell.java` (B6) and `GlassTabView.java` /
`MainTabsActivity.java` / `MainTabsLayout.java` (B1). Everything else is already inside the cut, or is
documentation and debug-source-set tooling.

Debug-only tooling (never in release sources or manifests):

- `TMessagesProj_App/src/debug/.../CybergramShowcaseActivity.java` — showcase host with optional modes;
- `.../CybergramB2MessageStatesFixture.java`, `.../CybergramB5ServiceDateFixture.java`.

### Owner design target (landed 2026-09-15, not pushed)

`design/DESIGN_TARGET.md` (written in Russian, unlike the older docs) records the owner's reference
screenshot `design/references/design-target-hex-chat.jpg` and derives from it colour roles
(cyan = outgoing/self, amber = interlocutor, red = "watching" service layer), typography, layout and a
monospace service-label layer. It is explicitly a mood and decision reference, not a pixel spec, and it
is linked from `README.md`. No product code changed with it.

It is reconciled against the authority and the landed code in
`docs/DESIGN_TARGET_RECONCILIATION_2026-09-15.md`, which confirms the landed colour roles, palette, thin
outlines, service/date plate and edge-only decoration, and raises three pending owner decisions
(**D1** message silhouette, **D2** two palette hues, **D3** service-label copy). The design authority
itself has **not** been amended — that requires an owner ruling, so `docs/CYBERGRAM_UI_SPEC.md` remains
unchanged and controlling.

Work recorded on 2026-09-15 (all docs-only, all unpushed):

- `docs/DESIGN_TARGET_RECONCILIATION_2026-09-15.md` — reference versus spec versus landed code, the three
  decisions, and the work plan mapped onto the existing pass queue (commit `7cf5b408d`);
- `docs/passes/B7_CHAT_CANVAS_HUD.md` § "Pre-flight findings" — resolves the contract's two open questions
  statically: blur capture never includes a `contentView` sibling of `chatListView`, and
  `indexOfChild(chatListView)` is the wallpaper-independent insertion anchor, with the hard-coded sibling
  indices (`1`, `3`, `17`) recorded for runtime proof (commit `09a6f2601`);
- `docs/runbooks/CYBERGRAM_A_TIER_VALIDATION.md` — the pending B0/B1/B2/B5/B6 authenticated matrices
  consolidated into one executable checklist.

Evidence lives in `docs/WORK_STATE.md` (chronological), `docs/B2_MESSAGE_STATE_AUDIT_2026-09-12.md`,
`docs/B5_SERVICE_DATE_AUDIT_2026-09-13.md`, `docs/B6_SERVICE_DATE_IMPLEMENTATION_2026-09-14.md`, and
git-excluded `/.local-artifacts/` (screenshots, APKs, workspace-local Gradle home).

## 3. What is not ready

| Pass | Status | Outstanding |
|---|---|---|
| B0 | `PARTIAL` | authenticated `FilterTabsView` interaction/visual matrix. Validation only; no production fixes authorized |
| B1 | `E PASS / A PARTIAL / P PENDING` | navigation and visual core confirmed on an authenticated surface on 2026-09-15 (`docs/A_TIER_VALIDATION_2026-09-15.md`); Calls swap, long-drag, show/hide animation, orientation and the non-Cybergram theme check remain |
| B2 | `AUDIT COMPLETE` | 15 account-dependent `UNTESTED` rows (reply layout, reaction interaction, metadata, service/date adjacency) |
| B3 | `CLOSED / NOT REQUIRED` | nothing — do not reopen |
| B4 | `DESIGN-OPEN / NOT AUTHORIZED` | needs a design ruling first; no spec written; reply plate and reaction pill stay untouched |
| B5 | `AUDIT COMPLETE` | authenticated and rich cases |
| B6 | `E PASS / E-CONTROL PASS / A PENDING` | authenticated ordinary + rich rows; service-message reaction re-check; `E-rich` unreachable pre-auth |
| B7 | `SPEC PREPARED / NOT AUTHORIZED` | the whole implementation, once a human authorizes it |
| B8 | `DEFERRED / STAGE F` | secondary surfaces and onboarding |
| R1 | `SEPARATE TRACK` | package/applicationId decision; signing/Firebase/keystore; unresolved Redmi Note 10S / MIUI 14.0.4 install report |

The dominant outstanding item is **A-tier verification debt**: B0, B1 and B6 are integrated at E tier
only, and B2 carries 15 `UNTESTED` rows. The first authenticated run, on 2026-09-15, partially validated
**B1** — the tab navigation and the Cybergram nav presentation are confirmed on a real account — while
**B0, B2, B5 and B6 remain entirely untested on an authenticated surface**; the account-dependent
tranches that require opening conversations are flagged for an owner decision because they mark messages
as read. The debt is executable as a single checklist in
`docs/runbooks/CYBERGRAM_A_TIER_VALIDATION.md`, and the recorded run is
`docs/A_TIER_VALIDATION_2026-09-15.md`. Two known caveats are carried forward deliberately: `E-rich` was never faked, and the
`ThemePreviewActivity` foreign-theme preview-scope question is answered for today's code but keeps a
recorded latent caveat for any future foreign-theme preview row that instantiates a `ChatActionCell`.

### Gaps verified during this snapshot (2026-09-15, static grep only)

Recorded so they are not lost, not raised as confirmed defects:

- the B7 contract states "19 classes extend `SizeNotifierFrameLayout`"; the current tree has **20**
  matching files (19 hosts besides `ChatActivity`'s own container). The conclusion — do not place the
  seam in the shared base class — is unaffected;
- B7's single unresolved implementation detail is the child index. Besides the lazily created
  `backgroundView` already noted in the contract, index-level neighbours exist that the contract does
  not mention: `videoPlayerContainer` is added at index `1` (`ChatActivity.java:12157`) and
  `thanosEffect` at `1 + indexOfChild(chatListView)` (`ChatActivity.java:44375`). Any insertion must be
  proven against these;
- the superseded pre-review B6 iteration is retained as a **tag** (`dev-b6-20260913-1f31cfa91`), not as
  the "side branch" the docs mention; provenance is intact;
- an R1 package-identity audit was executed on 2026-09-14 with an explicit "do not edit or commit"
  instruction, so its findings are **not** in this repository — they exist only in that session's
  transcript. Re-deriving them is an open, unowned task;
- `Tools/validate_cybergram_theme.py` documents its own usage as `tools/…` (lowercase). Case-sensitive
  filesystems need `Tools/…`.

### Design target vs design authority (open decision, not a defect)

`design/DESIGN_TARGET.md` describes the reference's service-label layer and lists among its examples
`SECURE CHAT`, `END-TO-END`, a lock icon and `CONNECTION STABLE`. It rules out the borrowed Cyberpunk
2077 brands and slogans (`NCPD`, `Night City`) and any decoration over text, but it does **not**
explicitly exclude the security-claim microtext.

`docs/CYBERGRAM_UI_SPEC.md` line 117 forbids exactly that, and `docs/passes/B7_CHAT_CANVAS_HUD.md`
repeats the ban as a hard stop: no `SECURE CHAT`, no `END-TO-END`, no invented IDs that misrepresent
Telegram. This is a conflict between a newly landed reference document and the design authority that
has to be resolved by a human decision — the authority is currently silent about the design target, and
the design target does not mention the authority. Until it is resolved, no implementation should treat
the service-label examples as approved copy.

## 4. What runs, and how

Validation tiers are defined in `docs/EXECUTION_BACKLOG.md`:

- **E** — emulator: build, install, launch, debug showcase, screenshots, logcat. Default remote tier;
- **A** — authenticated production UI on emulator or device; required for account-dependent surfaces;
- **P** — physical device / OEM confidence.

Never promote E evidence into A or P. No integrated Cybergram surface has A evidence today.

### 4.1 Theme palette validator (no Android toolchain required)

`Tools/validate_cybergram_theme.py` is a deterministic, local cross-check of the `.attheme` palette
against the string key names in `ThemeColors.createColorKeysMap()`. It checks unknown keys, duplicate
keys, malformed lines and ARGB sanity, and prints a full per-line dump.

```text
python Tools/validate_cybergram_theme.py
```

Exit code is non-zero when unknown, duplicate or malformed entries exist; `OK` otherwise.

Result when run for this snapshot (2026-09-15, tree at `dev` = `9f8211503`; the later design-target
commit touches no theme sources, so the result stands for `39c301bb4` as well):

```text
unknown=0 duplicates=0 malformed=0
OK
```

### 4.2 Instrumented tests (device required)

Module `:TMessagesProj_AppTests` is the only test module in the build. It is an Android application
module in Kotlin, `testBuildType "debug"`, flavor `afat`, namespace
`org.telegram.messenger.test`, debug `applicationIdSuffix ".web"`, runner
`androidx.test.runner.AndroidJUnitRunner` (JUnit 4 + androidx test + kotlin-test).

Existing tests: `org.telegram.tgnet.test.BaseSchemeTest`, `NativeSchemeTest`, `TestDatabaseMigration`,
and the generated `org.telegram.tgnet.test.generated.Test_All` (`@RunWith(Enclosed::class)`). The TL
model classes and `Test_All.kt` are produced into `src/androidTest/kotlin` by the `generateScheme`
task, which the `test-generator` plugin hooks in front of every `preBuild` task.

```text
:TMessagesProj_AppTests:connectedAfatDebugAndroidTest
```

Notes and honest limitations:

- requires a booted emulator/device and a full native + app build; it is comparatively heavy;
- **there is no evidence in this repository that these tests have ever been executed for Cybergram**
  (`TMessagesProj_AppTests/build` does not exist);
- they are upstream-oriented (TL scheme serialization and a database migration). No automated test
  asserts Cybergram presentation; the Cybergram visual checks are manual, via the fixtures below.

### 4.3 Debug fixtures (manual visual harnesses, E tier)

Debug source set only; installed package is `org.telegram.messenger.beta`.

```text
adb shell am start -n org.telegram.messenger.beta/org.telegram.ui.CybergramShowcaseActivity
adb shell am start -n org.telegram.messenger.beta/org.telegram.ui.CybergramShowcaseActivity --ez cybergram_b5 true
adb shell am start -n org.telegram.messenger.beta/org.telegram.ui.CybergramShowcaseActivity --ez cybergram_b6_control true
```

- no extra — base showcase;
- `--ez cybergram_b5 true` — B5 service/date geometry fixture;
- `--ez cybergram_b6_control true` — B6 non-Cybergram control (proves the upstream rounded path still
  renders when Cybergram presentation is off).

The B6 control run on the emulator temporarily changed and then restored a saved theme preference; that
deviation is disclosed in `docs/WORK_STATE.md` and `docs/B6_SERVICE_DATE_IMPLEMENTATION_2026-09-14.md`.
Screenshots stay in git-excluded `.local-artifacts/`, never in Git.

### 4.4 E-tier build, install, runtime

Procedure: `docs/runbooks/CYBERGRAM_EMULATOR_VALIDATION.md`. Build target for emulator work:

```text
:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64
```

Host/toolchain facts observed on this machine:

- Gradle 8.11.1 via the wrapper; there is **no `gradlew.bat`** in this checkout, only `gradlew`
  (sh). On Windows the wrapper is invoked as a Java main class instead:

```text
jdk-17.0.20.1+1\bin\java.exe -classpath gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain :TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64
```

- JDK 17 at `%LOCALAPPDATA%\Android\toolchain\jdk-17.0.20.1+1`; Android SDK path comes from the
  gitignored `local.properties` (`sdk.dir`), which also holds `CYBERGRAM_API_ID` / `CYBERGRAM_API_HASH`
  — these must never be printed, copied into reports or committed;
- sandboxed executors may be denied writes to `~/.gradle`; point `GRADLE_USER_HOME` at a
  workspace-local cache (this workspace uses `.local-artifacts/gradle-home`);
- APK output: `TMessagesProj_App/build/outputs/apk/afat/debug/app.apk`; record size and SHA-256;
- install with `adb install -r`, launch normally, then optionally launch a fixture; scan logcat for
  `FATAL EXCEPTION` / `ANR in org.telegram.messenger.beta` / process death and report the match count.

AVD in use on this host: `Cybergram_API36`, Android 16 / API 36 / `x86_64`
(`sdk_gphone64_x86_64`), normally appearing as `emulator-5554`.

### 4.5 Cheap checks used during passes

- `git diff --check` before reporting;
- the re-reconnaissance `git grep` block in `docs/passes/B7_CHAT_CANVAS_HUD.md` before touching the
  chat canvas (host classes, `extends SizeNotifierFrameLayout`, `contentView.addView`,
  background/wallpaper entry points, `TAG_DRAWING_AS_BACKGROUND`);
- product-scope check: `git diff --stat <preserved-cut> dev -- TMessagesProj/src/main`;
- FATAL/ANR scan and screenshot capture as described in 4.4.

### 4.6 Not runnable in this environment

| Capability | State |
|---|---|
| A tier (authenticated UI) | **available since 2026-09-15**: the AVD has an authenticated session, and the first run is recorded in `docs/A_TIER_VALIDATION_2026-09-15.md`. Opening conversations marks messages as read in a live account, so that tranche needs an explicit owner decision |
| P tier (physical device) | not available: the Redmi Note 10S is not reachable through `adb` |
| `git fetch` from an unprivileged sandbox | fails with `SEC_E_NO_CREDENTIALS`; remote state is verified via `gh api` or a host-side git run |
| CI | no workflow definitions exist in the `dev` tree; the only recorded runs are two 2026-09-10 runs on a filter-tabs patch branch |

## 5. How to read the rest of the repository

1. `AGENTS.md` — repository and safety rules;
2. `docs/CURRENT_STATE.md` — recovery entrypoint and validation boundary;
3. `docs/CYBERGRAM_UI_SPEC.md` — design/product authority;
4. `docs/EXECUTION_BACKLOG.md` — status/dependency index and validation tiers;
5. `docs/passes/<PASS>.md` — the bounded contract to execute;
6. `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md` — static ownership/seam evidence;
7. `docs/WORK_STATE.md` — chronological historical evidence for its recorded revisions.

A pass does not authorize the next pass, and this document authorizes nothing at all.
