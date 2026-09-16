# R1 — independent device verification of B0-FIX (`3911690fe`) and R-GATE (`4629e98a3`)

Status: **VERIFIED ON DEVICE / controlled A-B on one emulator / both edits confirmed / no crash;
ANR environment caveat recorded in §8 / P NOT RUN.**

Task: R1 — independently verify the two planner edits in `cybergram` on a device: build, run, confirm
the selected tab's label and the presentation gate against the active theme.

This verification was performed by a **separate session** that did not write either edit. It does not
reuse the planner's screenshots or APKs: both images used below were built from the committed sources in
this session, and the pre-fix image was built by checking the two files out at their **pre-fix parents**.
Every visual claim below was re-observed from the raw captures listed in §4.

## 1. Verified commits and source facts

| Item | Value |
|---|---|
| HEAD verified | `fe5412594e3a18a8bd658e19ea0992535f9f15f1` (`dev`) |
| B0-FIX | `3911690fe` — `FilterTabsView.java`, +9/−1 |
| R-GATE | `4629e98a3` — `CybergramTheme.java`, +7/−2 |
| Ancestry | both commits are ancestors of HEAD; no later commit touches either file (`git log -- <path>`) |
| Revert basis (control) | `3911690fe^` for `FilterTabsView.java`, `4629e98a3^` for `CybergramTheme.java`; the diff from each parent to HEAD is exactly the commit diff (9/1 and 7/2), so the control is the true pre-fix source |

Source facts re-checked independently (not taken from the commit messages):

- `Theme.getCurrentTheme()` returns `currentDayTheme != null ? currentDayTheme : defaultTheme`
  (`Theme.java:6444-6446`); `Theme.getActiveTheme()` returns `currentTheme` (`Theme.java:6460-6462`).
  The claim in `4629e98a3` is correct.
- `CybergramBackdropDrawable.isEligible()` calls `CybergramTheme.isCybergramPresentation()` and then
  reads `Theme.getActiveTheme()` (`CybergramBackdropDrawable.java:88,92`). After `4629e98a3` both sites
  read the same accessor.
- `FilterTabsView.drawSelector()` paints `cybergramSelectorDrawable`, a `CybergramHudDrawable` filled
  with `CybergramTheme.PANEL_RAISED`, at `alpha = (int)(255 * listView.getAlpha())` when the Cybergram
  presentation is on, versus `alpha = 31` for the upstream selector (`FilterTabsView.java:1537-1539`).
  The plate really is opaque, so the draw order changed by `3911690fe` is the deciding factor.

## 2. What "independent" means for this run

1. **Own builds from the committed tree**, not the planner's artifacts. Two full builds (the pre-fix
   control and the post-fix treatment) both ran `:TMessagesProj:compileDebugJavaWithJavac` (not
   UP-TO-DATE), i.e. the changed sources were actually recompiled in both directions.
2. **Same device, same persisted state** for each A-B pair. The only deliberate device mutation was the
   theme-slot scenario needed to reach the R-GATE mismatch (§6); it was backed up and restored (§8).
3. **The installed image was identified by hash.** The `base.apk` pulled from the device at the end of
   the run hashes to `7620E12F…`, exactly the treatment build of §3.

## 3. Build evidence

Command used for every build (same as the planner's, plus an explicit repository-local Gradle home and
JDK, because `%USERPROFILE%\.gradle` is outside the writable workspace here):

```
java -classpath gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain \
     :TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64 --console=plain
```

| Build | Source state | Log (git-excluded) | Result | javac | APK |
|---|---|---|---|---|---|
| Control (pre-fix) | both files at their pre-fix parents | `/.local-artifacts/verify/build_control_reverted_gradle.log` | `BUILD SUCCESSFUL in 7m 59s`, exit 0, 8 tasks executed | **recompiled** | 68,949,292 B, `DB714803…` |
| Treatment (HEAD) | `dev` HEAD `fe5412594` | `/.local-artifacts/verify/build_treatment_final.log` | `BUILD SUCCESSFUL in 7m 20s`, exit 0, 8 tasks executed | **recompiled** | 68,948,939 B, `7620E12F…` |
| Packaging-only re-run of HEAD | `dev` HEAD `fe5412594` | `/.local-artifacts/verify/build_treatment_head.log` | `BUILD SUCCESSFUL in 1m 6s`, exit 0, 4 tasks executed | UP-TO-DATE | 68,949,494 B, `E742A9A8…` |

The control wrapper log (`build_control_reverted.log`) records the reverted state that the control build
actually compiled:

- `[control] gate line: … Theme.getCurrentTheme();` — the R-GATE fix is absent in the control;
- `[control] plate line:` (empty) — `cybergramPlateBehindLabels` is absent in the control;
- `[control] git status after restore:` (empty) — HEAD sources were restored immediately after the build.

Both source files compiled cleanly in both states. **APK hashes are not reproducible evidence**: the two
builds of identical HEAD source produced different sizes/hashes (`E742A9A8…` vs `7620E12F…`) because the
incremental states differed. The hash is used here only to prove *which* artifact was installed.

## 4. Device and evidence inventory

| Item | Value |
|---|---|
| Target | AVD `Cybergram_API36`, `emulator-5554`, Android 16 / API 36, `x86_64` |
| Package | `org.telegram.messenger.beta`, authenticated session (untouched) |
| Installed image at verification time | hash `7620E12F…` = the treatment APK of §3 |
| Build helper | `/.local-artifacts/verify/build_variant.ps1`, `control_build.ps1` |
| Run/capture helpers | `/.local-artifacts/verify/run_app.ps1`, `capture.ps1`, `device_scenario.ps1` |
| Logcat scans | `/.local-artifacts/verify/logcat_*.txt`, `anr_trace_1744.txt` |

Captures (git-excluded, `/.local-artifacts/verify/shots/`). Every PNG below was read visually in this
session; the matching `.xml` is the `uiautomator` window dump taken at the same moment.

| Capture | Content |
|---|---|
| `control_day_02.png` | control build, dialogs, Cybergram active |
| `treatment_day_clean.png` | treatment build, dialogs, Cybergram active |
| `treatment_day_chip2.png` | treatment build after tapping the second chip |
| `treatment_night_rc.png` | treatment build, night-slot Cybergram scenario |
| `control_night_rc.png` | control build, same night-slot scenario |
| `treatment_saved_backdrop.png` | treatment build, `Saved Messages`, D4 backdrop |

## 5. B0-FIX (`3911690fe`) — selected tab label: **confirmed**

Scenario: the device's own persisted state (`mainconfig` `theme=Cybergram`, `nighttheme=Cybergram`,
`selectedAutoNightType=0`, system night mode **off**), so the Cybergram presentation is on in both builds.

| Build | Capture | Observation |
|---|---|---|
| Control (pre-fix) | `control_day_02.png` | the selected first chip is an **empty chamfered plate** — no title, no counter; the other three chips (`263679`, `12412412`, `0`) keep their labels and counters. This reproduces the B0 defect from `docs/B0_FILTER_TABS_DEFECT_2026-09-15.md`. |
| Treatment (fixed) | `treatment_day_clean.png` | the same first chip reads **`All Chats 1`** on the cyan chamfered plate; all other chips keep their labels. |
| Treatment (fixed) | `treatment_day_chip2.png` | after tapping the second chip, the plate moves to **`263679 2`** and its label+counter are legible, while `All Chats 1` returns to its unselected labelled state and the list below switches folder. |

The window dumps corroborate *which* tab is selected (the filter chips are custom-drawn, but the selected
one is exposed as an accessibility node): `treatment_day_clean.xml` →
`content-desc="All Chats / 1 unread chat" selected="true"`; `treatment_day_chip2.xml` →
`content-desc="263679 / 2 unread chats" selected="true"`. The dump cannot show the overdraw itself —
label visibility is a pixel property — so the screenshot is the discriminating evidence, exactly as the
planner stated.

**Conclusion: `3911690fe` is confirmed.** The only source difference between the two images is the draw
order of the plate; the pre-fix image hides the selected label, the fixed image renders it, and the label
follows the selection.

## 6. R-GATE (`4629e98a3`) — gate on the active theme: **confirmed**

The device's persisted state (Cybergram in *both* slots, day mode) cannot expose the defect, because
`getCurrentTheme()` and `getActiveTheme()` then return the same object. The scenario that separates them
was therefore constructed and then restored (§8):

- `mainconfig`: `theme` (day slot) = **`Blue`**, `nighttheme` (night slot) = **`Cybergram`**;
- `selectedAutoNightType = 3` (system) and `adb shell cmd uimode night yes`.

Result: the **day** slot is `Blue` while the **active** theme is `Cybergram`, so `getCurrentTheme()`
(pre-fix) is `Blue` and `getActiveTheme()` (post-fix) is `Cybergram`.

| Build | Capture | Observation |
|---|---|---|
| Treatment (fixed) | `treatment_night_rc.png` | **Cybergram chrome is present**: red rule under the flat header, angular cyan search field, chamfered filter row with the labelled selected `All Chats` chip, angular bottom navigation. The gate read the active theme. |
| Control (pre-fix) | `control_night_rc.png` | **no Cybergram chrome**: upstream rounded search field over the blurred background, upstream alpha-31 selector pill, upstream bottom navigation with the round blue FAB — the upstream UI drawn in the active theme's colours. The gate read the day slot and suppressed the presentation. |

Both runs were in the same scenario and had the same selected tab (`All Chats`, verified from
`treatment_night_rc.xml` and `control_night_rc.xml`), so the visual difference is attributable to the
gate alone.

`treatment_saved_backdrop.png` additionally shows the treatment build inside `Saved Messages`: Cybergram
header/composer frame plus the faint cyan grid of the **D4 default backdrop** in the chat area, i.e.
`CybergramBackdropDrawable.isEligible()` — which reads `getActiveTheme()` — agrees with the gate.

**Conclusion: `4629e98a3` is confirmed.** The pre-fix gate suppresses the whole presentation when
Cybergram is active from the night slot; the fixed gate keeps it, matching the backdrop's accessor.

## 7. Acceptance criteria

| Criterion | Check | Result |
|---|---|---|
| A1 `FilterTabsView.java` contains `cybergramPlateBehindLabels` | lines 1412, 1413, 1417 | present |
| A2 `CybergramTheme.java` contains `getActiveTheme()` | lines 95, 101, 110 | present |
| A3 working tree clean | `git status --porcelain` empty after committing this document | see `git log` for the commit that adds this file |

## 8. Disclosures

- **Theme-slot mutation was required and was fully reverted.** The R-GATE separation cannot be reached
  from the persisted state; `mainconfig.xml` and `themeconfig.xml` were backed up first
  (`/.local-artifacts/verify/device/backup/`), the scenario was pushed with `run-as`, and afterwards the
  original files were restored and `cmd uimode night no` re-applied. The re-read after restore shows
  `theme=Cybergram`, `selectedAutoNightType=0`, night mode `no`; the account/session files were never
  touched. The mutation was test instrumentation only — it is not a product change and is not a
  re-opening of the deliberately stopped settings-mutation line.
- **ANRs are an emulator/debug-build environment artifact, not a failure of either edit.** Four input
  dispatch ANRs (`fatal=0`, all `Reason: Input dispatching timed out … Waited 5005ms for
  FocusEvent/MotionEvent`) occurred around the cold starts; one of them is my own `Wait` tap at
  `(540,1359)`. The full trace `anr_trace_1744.txt` shows the main thread in
  `android.graphics.HardwareRenderer.syncAndDrawFrame`, and the word `FilterTabsView`,
  `CybergramTheme` and `isCybergramPresentation` appears **0 times** in that trace. The ANR dialog
  overlaps the middle of `control_day_02.png`; the filter row is above it and unobstructed.
  `hide_error_dialogs` was briefly set to `1` while diagnosing and has been deleted again
  (`settings get global hide_error_dialogs` → `null`).
- **The planner's APK hashes are not reproduced and should not be treated as reproducible.**
  `docs/B0_FIX_IMPLEMENTATION_2026-09-16.md` quotes 73,844,985 B and
  `docs/R_GATE_IMPLEMENTATION_2026-09-16.md` quotes 79,183,709 B for the same build command this session
  produced 68,948,939 B from. The sizes differ; this is not by itself an inconsistency in the planner's
  work (incremental build state and packaging differ), but it does mean the hash column only identifies
  an artifact, it does not certify a source state.
- **`uiautomator` does not render the chip labels**, only the selected chip's accessibility node, so the
  B0 visual conclusion rests on the screenshots, as in the planner's record.

## 9. Not claimed

- No clean-build (`clean` / `--rerun-tasks`) evidence; the two authoritative builds recompiled the
  changed module sources but other modules were UP-TO-DATE, as the logs show.
- The R-GATE "Cybergram in the day slot with a different night theme" half of the defect (chrome drawn
  over a non-Cybergram palette) was **not** exercised; only the night-slot half below was.
- No A/P/OEM or physical-device evidence; emulator only. No non-Cybergram regression control was run
  beyond the pre-fix images, which are themselves the upstream-order behaviour.
- The remaining B0 matrix items (horizontal overflow/last folder, page swipe, long-press/menu,
  edit/reorder/delete, non-Cybergram presentation control) were not exercised.
- The verification does not authorize a release, a push, or further product changes.
