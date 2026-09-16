# R-GATE — Cybergram presentation gate accessor (2026-09-16)

Status: **IMPLEMENTED / E BUILD+INSTALL+RUNTIME REGRESSION PASS / NIGHT-SLOT SCENARIOS NOT EXERCISED /
A PENDING / P NOT RUN.**

Fix for finding **R-GATE** in `docs/EXECUTION_BACKLOG.md` § "Code-review follow-ups (R-series)",
recorded `NOT APPLIED; unauthorized` on 2026-09-16. Authorization basis: the repository owner instructed
this session to continue the work and choose the tasks (2026-09-16).

## 1. Verified defect

Two Cybergram code paths resolved "the theme" through different accessors:

| Site | Accessor | Upstream value |
|---|---|---|
| `CybergramTheme.isCybergramPresentation()` | `Theme.getCurrentTheme()` | `currentDayTheme != null ? currentDayTheme : defaultTheme` (`Theme.java:6444-6446`) |
| `CybergramBackdropDrawable.isEligible()` | `Theme.getActiveTheme()` | `currentTheme` (`Theme.java:6460-6462`) |

The two `ThemeInfo` objects differ whenever the night slot holds a different theme from the day slot, so
the gate could be false while Cybergram was the visible theme (night slot) or true while a non-Cybergram
night theme was active.

## 2. Change

`TMessagesProj/src/main/java/org/telegram/ui/ActionBar/CybergramTheme.java` only, **+7/−2**:

```java
Theme.ThemeInfo info = Theme.getActiveTheme();   // was Theme.getCurrentTheme()
```

plus javadoc recording why the active theme is the correct source. After this change the gate and
`CybergramBackdropDrawable.isEligible()` read the same accessor; the backdrop's own second lookup stays
consistent with it. No other file was touched; no colour, geometry or behaviour changed.

## 3. Provenance

| Item | Value |
|---|---|
| Feature branch | `fix/cybergram-theme-gate-accessor` |
| Production commit | `4629e98a3` (`ui: gate Cybergram presentation on the active theme`) |
| Integration | fast-forward into `dev` (no squash, no merge commit) |
| `git diff --check` | clean (exit 0) |

## 4. E tier — build, install, runtime

| Step | Command | Result |
|---|---|---|
| Build | `java -classpath gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain :TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64` | `BUILD SUCCESSFUL in 3m 1s`, exit 0 (`/.local-artifacts/build_rgate_x86_64.log`) |
| APK | `TMessagesProj_App/build/outputs/apk/afat/debug/app.apk` | `79,183,709` bytes, SHA-256 `BF62D0150ED1FC05287B6533268616522C06DB902F1F2CE20D27EC3899670F52` |
| Install | `adb install -r` | `Success`, exit 0 |
| Target | AVD `Cybergram_API36`, `emulator-5554`, Android 16 / API 36, `x86_64`, authenticated session | — |
| Logcat | after the run | `fatal_matches=0`, `anr_matches=1` — see §5 (`/.local-artifacts/r3/logcat_rgate_full.txt`) |

### Regression observed (Cybergram remains the active presentation)

| Artifact (git-excluded) | Observation |
|---|---|
| `/.local-artifacts/r3/21_rgate_dialogs.png` | dialogs screen with the full Cybergram chrome: flat header + red rule, Cybergram search field, angular filter row with the labelled selected `All Chats` chip, angular bottom navigation |
| `/.local-artifacts/r3/24_rgate_chat_clean.png` | `Saved Messages` chat: flat Cybergram header, angular cyan/amber bubbles, composer frame, and the **D4 default backdrop** — the faint cyan grid is visible in the wallpaper area, i.e. `isEligible()` and the gate now agree on the active Cybergram theme |

The APK contains only the `x86_64` ABI (41.2 MB of `lib/`), matching the build flag.

## 5. Disclosures

- **One ANR, caused by this run's own tap**: `ANR ... Reason: Input dispatching timed out ... Waited
  5004ms for MotionEvent(... pointers=[0: (544.0, 941.0)])` — the tap that opened `Saved Messages`; the
  debug build took longer than the 5 s input-dispatch budget. Dismissed with *Wait*; the app stayed
  functional and the chat rendered (capture above). No `FATAL EXCEPTION` in the run. No trace-level
  causation analysis was done.
- The two user-visible night-slot scenarios (Cybergram as **night** theme; Cybergram as **day** theme with
  another night theme active) were **not** exercised. Reproducing them requires a persisted theme-slot
  mutation, and the repository deliberately stopped the settings-mutation line. The accessor semantics are
  verified at source (`Theme.java:6444-6466`) and the change is the gate's own accessor only.
- No A/P evidence.

## 6. Not claimed

- Not proven that no other Cybergram surface reads a day-slot theme directly: `isCybergramPresentation()`
  is the single funnel for every call site listed in §1 of the review, and the grep in this round found no
  other Cybergram file reading `Theme.getCurrentTheme()`.
- The ANR is reported, not explained, and is not attributed to this two-line accessor change.
