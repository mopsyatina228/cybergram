# B0-FIX — selected filter-tab label (2026-09-16)

Status: **IMPLEMENTED / E BUILD+INSTALL+RUNTIME+VISUAL PASS / A PARTIAL (fixed selection behaviour
confirmed on the authenticated surface; remaining matrix items not run) / P NOT RUN.**

Fix for the confirmed defect recorded in `docs/B0_FILTER_TABS_DEFECT_2026-09-15.md`
(`CONFIRMED DEFECT / FIX NOT AUTHORIZED`). Authorization basis: the repository owner instructed this
session to continue the work and choose the tasks (2026-09-16); the defect record named approach 1 as the
smallest correct change.

## 1. Change

`TMessagesProj/src/main/java/org/telegram/ui/Components/FilterTabsView.java` only, **+9/−1**.

```java
final boolean cybergramPlateBehindLabels = child == listView && useCybergramPresentation();
if (cybergramPlateBehindLabels) {
    drawSelector(canvas);            // plate painted BEFORE the labels
}
boolean result = super.drawChild(canvas, child, drawingTime);
if (child == listView && !cybergramPlateBehindLabels) {
    drawSelector(canvas);            // upstream order, untouched
}
```

- The Cybergram branch now paints its opaque plate behind the tab labels.
- The non-Cybergram branch keeps the upstream order and the upstream alpha 31 unchanged; when the gate is
  false, `child == listView && !false` is exactly the previous `child == listView`.
- No alpha, colour, geometry, listener, animation or measurement change; no other file touched.

## 2. Provenance

| Item | Value |
|---|---|
| Feature branch | `fix/cybergram-filter-tabs-plate` |
| Production commit | `3911690fe` (`ui: draw the Cybergram filter-tab plate behind the labels`) |
| Integration | fast-forward into `dev` (no squash, no merge commit) |
| `git diff --check` | clean (exit 0) |

## 3. E tier — build, install, runtime

| Step | Command | Result |
|---|---|---|
| Build | `java -classpath gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain :TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64` | `BUILD SUCCESSFUL in 7m 17s`, exit 0 (`/.local-artifacts/build_b0fix_x86_64.log`) |
| APK | `TMessagesProj_App/build/outputs/apk/afat/debug/app.apk` | `73,844,985` bytes, SHA-256 `E817B00FB4AE053F9BF034BFB9B2C5EE9EBD6C9FF796B9DAD7F832DA94D00538` |
| Install | `adb install -r` | `Success`, exit 0 |
| Target | AVD `Cybergram_API36`, `emulator-5554`, Android 16 / API 36, `x86_64`, authenticated session | — |
| Logcat scan | FATAL EXCEPTION + ANR after the validated relaunch | `fatal_matches=0`, `anr_matches=0`, `crash_matches=0` (`/.local-artifacts/r3/logcat_b0fix_full.txt`) |

### Visual result (dialogs filter row)

Reproduction from the defect record, steps 2–4:

| Artifact (git-excluded) | Observation |
|---|---|
| `/.local-artifacts/r3/08_dialogs_filterrow.png` | selected **`All Chats 2`** chip shows its label and counter on the cyan chamfered plate; `263679 3`, `12412412 104`, `0` keep their labels |
| `/.local-artifacts/r3/11_third_chip_selected.png` | after tapping the third chip, **`12412412 104`** is selected *and* legible, and `All Chats 2` is back to its unselected labelled state; the list below switched to that folder |
| `/.local-artifacts/r3/03_dialogs.png` | first capture of the installed build (an ANR dialog is over the screen, see §4) — the selected `All Chats` label is already visible behind it |
| `/.local-artifacts/r3/07_dialogs_clean.png` | second `FilterTabsView` surface: search tabs `Chats / Channels / Apps / Posts / Media` with the selected `Chats` chip labelled |

Before the fix the same selection produced an empty plate (`docs/B0_FILTER_TABS_DEFECT_2026-09-15.md`, with
the preserved before-captures `.local-artifacts/a-tier/b0_sel_a.png`, `b0_sel_b.png`, `crop_sel.png`).

### 3.1 Tier of this evidence

The run used the **authenticated** session on the AVD (the same account and the same production dialogs
surface on which the defect was first confirmed on 2026-09-15), not a debug fixture. The captures
therefore carry **A-tier** evidence for the fixed selection behaviour, in addition to the E-tier
build/install/runtime numbers.

| B0 matrix item (defect-record reproduction) | Result on the authenticated surface |
|---|---|
| selected chip renders its title | **confirmed** (`08`, `11`) |
| unselected chips keep their titles | **confirmed** (`08`, `11`) |
| switching selection moves the label with the plate | **confirmed** (`11`: `12412412 104` selected and labelled, `All Chats 2` restored) |
| unread counters still render | **confirmed** (`08`, `11`) |
| second `FilterTabsView` surface (search tabs) | **confirmed** (`07`) |
| horizontal overflow / scroll to the last folder | **not run** |
| page swipe / manual interpolation | **not run** |
| long-press / menu behaviour | **not run** |
| edit / reorder / delete mode | **not run** |
| non-Cybergram theme control | **not run** (needs a persisted theme mutation, deliberately not performed) |

The run was stopped after these items because the emulator became unreliable: repeated input-dispatch
ANRs on cold start and on chat opening (§4).

## 4. Disclosures

- **One ANR on the first cold start after install** (16:27:22), before any Cybergram filter-row
  interaction: `ANR in org.telegram.messenger.beta ... Reason: Input dispatching timed out ... Waited
  5003ms for FocusEvent(hasFocus=false)` (`/.local-artifacts/r3/anr_logcat.txt`). The dialog was dismissed
  with *Wait* → app closed → warm relaunch, after which the app was responsive and the run above was
  captured with `crash_matches=0`. The same cold-start ANR pattern is recorded for earlier rounds
  (`/.local-artifacts/r2/00-anr-dialog.png`). **Trace pulled and read**
  (`/data/anr/anr_2026-09-16-16-27-22-879` → `/.local-artifacts/r3/anr_trace_1627.txt`): the main thread
  was inside `android.text.StaticLayout.generate` → `android.graphics.text.LineBreaker.computeLineBreaks`,
  i.e. upstream text layout on the main thread. Neither changed line (`FilterTabsView.drawChild` order,
  `CybergramTheme` guard) appears in the stack; the ANR is main-thread latency in the debug build, not a
  rendering failure of this fix.
- **Repeated cold-start ANRs later in the same session** (16:48), same reason string with a 5001 ms
  focus-event wait. The telegram trace (`/data/anr/trace_04` →
  `/.local-artifacts/r3/anr_trace_1648_4.txt`) shows the main thread in
  `android.os.BatteryManager.queryProperty` → `IBatteryPropertiesRegistrar$Stub$Proxy.getProperty`, i.e. a
  Binder call to the system battery service. These ANRs are why the remaining A-matrix items in §3.1 were
  not run.
- **Non-Cybergram control not re-run** this round. It needs a persisted theme mutation (the procedure in
  the defect record), and the repository deliberately stopped the settings-mutation line. The control is
  instead static: the non-Cybergram branch is byte-equivalent in behaviour because the guard reduces to
  the previous expression. The earlier Blue-theme control in the defect record remains the runtime
  reference.
- `uiautomator` does not expose the filter chips as nodes (custom-drawn labels), so the verification is
  screenshot-based, as in the defect record.

## 5. Not claimed

- No A-tier coverage of the **remaining** B0 matrix items (see §3.1); the fixed selection behaviour is the
  part that carries A evidence.
- No P/OEM evidence; emulator only.
- B0's remaining matrix items (horizontal overflow/scroll to the last folder, page swipe, long-press/menu,
  edit/reorder/delete mode, non-Cybergram control) are still not exercised.
- The fix is not proven to be the only possible presentation; it removes the overdraw that hid the label.
- The AVD's repeated ANRs are an environment observation, not a product verdict: three main-thread stacks
  were captured (StaticLayout text layout, `ChatActivity.createView`, and a `BatteryManager` binder call to
  the system battery service), none of them in the changed code, but no claim is made that the debug build
  has no main-thread latency problems of its own.
