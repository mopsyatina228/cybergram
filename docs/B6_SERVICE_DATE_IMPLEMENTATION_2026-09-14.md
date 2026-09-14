# B6 — ordinary Cybergram service/date plate: bounded implementation record (2026-09-14)

Status: `INTEGRATED ON dev / BOUNDED PRODUCTION SEAM (ChatActionCell.java ONLY) / INDEPENDENT REVIEW SAFE_MINIMAL_SEAM / E BUILD+INSTALL+RUNTIME+VISUAL PASS / E-CONTROL PASS / E-RICH UNAVAILABLE PRE-AUTH / A PENDING / P NOT REQUIRED / RELEASE INSTALL COMPATIBILITY OPEN FOR REDMI MIUI`

Type: bounded implementation, evidence and reconciliation

Branch: `feature/cybergram-service-date-angular` (HEAD `53dd1368e478ce6a1f9845d1a1797ecfa4e3e0fc`),
**integrated on `dev` by ff-only fast-forward** (no squash, no merge commit) — see §9

Base `origin/dev` at implementation start: `cefa15eb7ba42d32b60d31ecf16d626f356344d5`

Spec source: `docs/B5_SERVICE_DATE_AUDIT_2026-09-13.md` §5 (Strategy B), authorized by the B5 integration
commit `cefa15eb7`. Design authority: `docs/CYBERGRAM_UI_SPEC.md` (unmodified). Planning authority:
`docs/EXECUTION_BACKLOG.md`. Validation runbook: `docs/runbooks/CYBERGRAM_EMULATOR_VALIDATION.md`.

Scope of this document: it records what was actually built, reviewed and measured. It does not promote E
evidence into A or P and does not claim physical-device runtime from build output. The integration itself is a
separate **ff-only** fast-forward of `dev`, recorded in §9.

---

## 1. Revision map

| role | revision | contents |
|---|---|---|
| base `origin/dev` at implementation start | `cefa15eb7ba42d32b60d31ecf16d626f356344d5` | B5 integration + B6 authorization (docs-only) |
| production commit | `f86ef812bb585db7c753335c7f573406e5129323` | `ChatActionCell.java` only: **+22 / −1** |
| superseded pre-review iteration | `1f31cfa91a8b734e414ca960f2124659ba45bf2f` | `ChatActionCell.java` only: +136 / −100 (side branch `dev-b6-20260913-1f31cfa91`, **not** an ancestor of the current branch HEAD) |
| debug control fixture | `53dd1368e478ce6a1f9845d1a1797ecfa4e3e0fc` | `TMessagesProj_App/src/debug/.../CybergramShowcaseActivity.java`: +26 / −1 |
| branch HEAD at implementation | `53dd1368e478ce6a1f9845d1a1797ecfa4e3e0fc` | debug commit on top of the production commit |
| evidence/docs commit = integration HEAD on `dev` | `0c4172346764c5622a5cdbfa06a4ce624d3cd56a` | docs-only reconciliation; `dev` fast-forwarded onto it (ff-only, no merge commit) |

`git diff cefa15eb7..HEAD` over the repository touches exactly two files and no release asset, manifest or
resource:

- `TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatActionCell.java` (production, 22 insertions / 1 deletion);
- `TMessagesProj_App/src/debug/java/org/telegram/ui/CybergramShowcaseActivity.java` (DEBUG source set only).

`1f31cfa91` is retained because the earlier prerelease artifact `Cybergram-dev-b6-1f31cfa91-x86_64.apk` was
built from it. It is **superseded** by `f86ef812b`: independent review reduced the change from a
two-pass-build replacement (+136 / −100) to a minimal additive seam (+22 / −1) with the same gate, the same
geometry contract and a strictly smaller blast radius. Nothing in the current evidence chain depends on
`1f31cfa91`.

## 2. Production change actually landed (`f86ef812b`)

### 2.1 Shape of the seam

The upstream ordinary path generation is left **byte-identical**. The Cybergram branch is taken *after* the
upstream ordinary `backgroundPath` build has completed (`backgroundPath.close()`), and only then replaces
that path — the seam is inserted after the upstream ordinary path generation and before the existing
suggested-post-approval `else if`, which is preserved unchanged:

```java
if (useCybergramOrdinaryServicePlate()) {
    int maxLineWidth = 0;
    for (int a = 0; a < lineWidths.size(); a++) {
        maxLineWidth = Math.max(maxLineWidth, lineWidths.get(a));
    }
    final int centerX = getMeasuredWidth() / 2;
    final float top = dp(4);
    final float bottom = top + textHeight + dp(6);
    final float left = centerX - maxLineWidth / 2f - dp(8);
    final float right = centerX + maxLineWidth / 2f + dp(8);
    CybergramBubbleDrawable.buildPath(backgroundPath, left, top, right, bottom, dp(CybergramTheme.BUBBLE_CORNER_CUT_DP));
    backgroundLeft = (int) Math.floor(left);
    backgroundRight = (int) Math.ceil(right);
} else if (isMessageActionSuggestedPostApproval() && !isNewStyleButtonLayout()) {
    /* upstream, unchanged */
}
```

Two imports are added (`ActionBar.CybergramBubbleDrawable`, `ActionBar.CybergramTheme`); no new class, no new
geometry helper, no new colour, no resource or manifest change.

### 2.2 Central gate

```java
private boolean useCybergramOrdinaryServicePlate() {
    return CybergramTheme.useAngularMessageGeometry(themeDelegate)
        && !isButtonLayout(currentMessageObject)
        && !isMessageActionSuggestedPostApproval();
}
```

This is the existing central Cybergram gate, evaluated on the `themeDelegate` field that is actually
available at the draw site. There is no theme-name string check, no colour heuristic and no second gate
implementation. `isButtonLayout(null)` is false and `isMessageActionSuggestedPostApproval()` is null-safe, so
the `currentMessageObject == null` date/floating case is covered by the same condition.

### 2.3 Geometry (Strategy B) and bounds

One compact enclosing chamfer, built with the project-owned shared primitive required by the operating
contract: `maxLineWidth` from the already-built `lineWidths`, `top = dp(4)`,
`bottom = top + textHeight + dp(6)`, `left/right = centerX ∓ maxLineWidth/2f ∓ dp(8)`,
`cut = dp(CybergramTheme.BUBBLE_CORNER_CUT_DP)`, with `backgroundLeft/backgroundRight` updated to the new
envelope (`floor`/`ceil`) so the footprint contract (`getBoundsLeft()/getBoundsRight()`, touch hit-tests,
`MessageEntityView` positioning) is preserved.

Deviation from the B5 §5.4 sketch, recorded for honesty: §5.4 derived `maxLineWidth` from
`textLayout.getWidth()`; the implemented seam derives it from the already-built `lineWidths`. Same plate, no
new measurement pass, no new layout dependency.

### 2.4 Excluded / unchanged

Excluded by the gate: rich `isButtonLayout` states (gift/star/offer/community/wallpaper/birthday/story),
new-style cards, bot buttons/ribbons and the suggested-post-approval override — all keep upstream geometry.
Preserved unchanged: `backgroundPaint`/`darkenBackgroundPaint`/`dimPaint` sequence, `applyServiceShaderMatrix`,
`hasGradientService`, text measurement/`createLayout`, `backgroundPath2`/card/ribbon paths, reactions, click
and touch logic, message/action data types, and the entire non-Cybergram upstream branch.

## 3. Independent review verdict

Verdict: **`SAFE_MINIMAL_SEAM`**.

- Keeping upstream ordinary path generation intact and superseding it only inside the same `invalidatePath`
  block is safe: the upstream two-pass side effects are safely superseded by the Cybergram replacement, and
  they cannot leak into any other draw, measure or touch path.
- `ThemePreviewActivity` foreign app-theme preview: the `SCREEN_TYPE_PREVIEW` (app-theme preview) message set
  contains **no `ChatActionCell` / `contentType == 1` row today**, so a preview of a different theme cannot
  render the angular service/date plate. **No additional `ThemePreviewActivity` production opt-out is
  required now**, and the B5 §5.1 "conditional extra file" stays unused.
  - Source-checked in this reconciliation: the only two `contentType` assignments in
    `MessagesAdapter` (`ThemePreviewActivity.java:4940` → `5`, `:4962` → `1`) both sit inside the
    `screenType == SCREEN_TYPE_CHANGE_BACKGROUND` branch under `dialogId != 0 && serverWallpaper == null`,
    and the adapter maps view type `1` to `ChatActionCell`. The wallpaper/chat-theme preview is current-app
    presentation, so angular geometry there is correct, not a leak.
- **Latent caveat (must be recorded, not acted on today):** the central gate falls back to
  `Theme.getCurrentTheme()` when the provider is null or non-Cybergram, and that fallback is **not
  preview-scoped**. If a future upstream change introduces a foreign-theme preview row that does instantiate
  a `ChatActionCell` (`contentType == 1`), that preview would render angular and would then need an explicit
  local opt-out. This is a future-scope note with no current code impact.

## 4. Evidence

### 4.1 x86_64 E — production commit `f86ef812b`

- `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64` → **BUILD SUCCESSFUL**;
- APK `73,306,179` bytes;
- SHA-256 `e415b00305d83122575bd81a18061d0e0b935487cbf0b9a7871388c0407eb621`;
- install to `emulator-5554` succeeded;
- normal launch first, then the real `ChatActionCell` B5/B6 fixture: stable, no FATAL/ANR/process death in the
  checked logcat window;
- screenshot `.local-artifacts/b6/b6_f86ef812.png` (git-excluded via `.git/info/exclude`);
- plate widths measured through the fixture: **98 / 160 / 288 / 230 / 310 / 642 px**, i.e. B5's six cases in
  order (date separator `setCustomDate`, one-line short service, one-line long service, two-line uneven
  service, three-line service, long wrapped service) → **visual Strategy B pass** (compact on date/one-line,
  only moderate symmetric side whitespace on uneven multi-line; B5 §8.3 corrected widths `98/158/288/230/310/640`
  are reproduced within the ±`dp(8)` envelope).

Size and SHA-256 above were re-verified on this host during the reconciliation pass by reading the preserved
artifact from disk: `.local-artifacts/releases/Cybergram-dev-b6-f86ef812b-x86_64.apk` (no rebuild, no re-sign).

### 4.2 arm64-v8a — production commit `f86ef812b` (build evidence only)

- `-PCYBERGRAM_ABI=arm64-v8a` → **BUILD SUCCESSFUL**;
- `68,452,403` bytes; SHA-256 `9d5f936b820766aec4f6c825e8625c147820b2fbc81b9690fe419ad7364e71c8`;
- native code: `arm64-v8a` (`lib/arm64-v8a/*`) and nothing else;
- artifact preserved: `.local-artifacts/releases/Cybergram-dev-b6-f86ef812b-arm64-v8a.apk` (size/SHA re-verified this pass).

**This is build evidence only. It is not a physical install and not a runtime result.**

### 4.3 x86_64 E-control — debug commit `53dd1368e`

- `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64` → **BUILD SUCCESSFUL in 29s**;
- APK `68,948,592` bytes; SHA-256 `ef97f76daf99bbd327287fb25c3a08edefa09b168f0ba9b439ab8b52fac12452`;
- install succeeded;
- emulator saved theme was temporarily switched from **Cybergram** to **Blue** for the control run and then
  restored to **Cybergram** afterwards (restore verified; the saved-theme configs captured around the run are
  retained in `.local-artifacts/b6/` as `mainconfig_before_control.xml`, `mainconfig_blue_control.xml` and
  `mainconfig_restore_cybergram.xml`). Honest deviation note: the B5 pass contract asked debug tooling not to
  modify saved user theme preferences; this control step did touch the saved theme on the emulator, inside an
  explicitly reverted window. It changed no product code and shipped nothing;
- the control runs the *same deterministic fixture colours* through a provider that deliberately does **not**
  implement the Cybergram geometry provider (`PlainPalette` wrapper in the DEBUG-only
  `CybergramShowcaseActivity`), so the only variable is the gate;
- screenshot `.local-artifacts/b6/b6_control_blue.png` proves the **actual upstream rounded ordinary path**
  underneath the magenta Strategy-B candidate outline → the non-Cybergram presentation path is intact;
- `CRASH_MATCHES=0`.

### 4.4 Universal 4-ABI compatibility build (branch HEAD `53dd1368e`)

- `assembleAfatDebug` universal (4 ABIs) → **BUILD SUCCESSFUL in 1m 23s**;
- `113,631,225` bytes; SHA-256 `0c781a6b71f13034b36586b4c596a48a3b8719e108d51195dc89c305a78e12f0`;
- native code: `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`; `minSdk 21` / `targetSdk 36`; signed v1+v2;
- published separately as a prerelease asset:
  `.local-artifacts/releases/Cybergram-dev-b6-20260914-53dd1368e-universal.apk` (size/SHA re-verified this pass),
  with notes `.local-artifacts/releases/notes_dev-b6-20260914-53dd1368e.md`.

**This is compatibility/build evidence and an explicit installer probe, NOT device runtime validation.**

## 5. Release/install issue — Redmi Note 10S / MIUI 14.0.4 (OPEN, non-rendering)

A user report states the **arm64 APK did not install** on Redmi Note 10S / MIUI 14.0.4. The exact
`INSTALL_FAILED_*` code is **unavailable**, because the device is not currently reachable through `adb`.
Therefore:

- Do **not** claim an ABI root cause. The universal 4-ABI APK (§4.4) is the explicit compatibility probe for
  exactly this open question, and it has not been run on that device;
- the package is `org.telegram.messenger.beta`, so a **signature/package conflict with an already-installed
  Telegram Beta** remains a plausible unresolved install cause — likely more plausible than ABI, per the
  attached release notes;
- this is tracked as a **release/install issue**, not as a B6 rendering defect. No B6 geometry, gate or code
  conclusion depends on it;
- closure requires the exact package-installer / `adb install` error text captured on the device.

## 6. Validation tier verdicts

| tier | item | verdict |
|---|---|---|
| `E-build` x86_64 (production commit) | BUILD SUCCESSFUL, size/SHA recorded | **PASS** |
| `E-install+launch` (production commit) | install on `emulator-5554`, normal launch + real fixture, no FATAL/ANR/process death | **PASS** |
| `E-geometry/density` | chamfer present on ordinary date/one-line/multi-line plates; widths 98/160/288/230/310/642 px = B5's six cases in order | **PASS** |
| `E-control` | non-Cybergram provider reproduces the upstream rounded ordinary path (`b6_control_blue.png`), CRASH_MATCHES=0 | **PASS** |
| `E-rich` | rich gift/offer/wallpaper/birthday/community rows | **UNAVAILABLE PRE-AUTH** (not faked) |
| `A` | authenticated service/date cases and rich cases; service-message reactions still render | **PENDING** |
| `P` | physical-device/OEM confidence | **NOT REQUIRED** unless a device/OEM defect appears |
| `compat` arm64-v8a + universal 4-ABI | build/size/SHA/ABI/signing; installer probe | **EVIDENCE ONLY — not runtime** |

Never promote one tier into another. The Redmi install report is unresolved **installation compatibility**,
not runtime rendering evidence, and it neither closes nor contradicts any tier above.

## 7. Unresolved limitations

1. **A tier open**: authenticated ordinary service/date rows (date separators, pin/unpin, join/leave,
   title/photo/TTL changes, group-call, screenshot actions) and all rich/special rows remain unverified on a
   real account.
2. **`E-rich` unavailable**: gift/star/offer/community/wallpaper/birthday/story and `TYPE_ACTION_PHOTO` rows
   need real message data; they were not faked with a parallel renderer.
3. **Service-message reactions**: ordinary plate plus `ReactionsLayoutInBubble` overlay; the overlay stays
   upstream (B4 `DESIGN-OPEN`) and has not been re-checked under the new plate.
4. **`SharedMediaLayout` floating date** uses `new ChatActionCell(context)` (null provider) and therefore the
   `Theme.getCurrentTheme()` fallback; it is expected to angularize under Cybergram. Confirm in `A`.
5. **Latent preview caveat** (§3): the global fallback is not preview-scoped; a future foreign-theme preview
   row that instantiates a `ChatActionCell` would need an explicit opt-out.
6. **Redmi/MIUI install report** (§5): open, non-rendering, exact installer error not yet captured.
7. **Integrated on `dev` (ff-only)**: `dev` was fast-forwarded from `feature/cybergram-service-date-angular`
   with no squash and no merge commit; integration final SHA
   `0c4172346764c5622a5cdbfa06a4ce624d3cd56a` (production `f86ef812b`, debug control `53dd1368e`, evidence/docs
   `0c4172346`). No push, rebase or release-asset change is part of this record.
8. **B5 evidence note**: B5's own audit doc remains the source for Strategy selection and the ordinary/rich
   ownership map; only its "B6 NOT IMPLEMENTED" and `ThemePreviewActivity` items are superseded by this
   document.

## 8. Integrity statements

- No production or debug code was modified in this docs reconciliation; the only changes are under `docs/`.
- `docs/CYBERGRAM_UI_SPEC.md` was not modified.
- No release asset was altered, replaced or re-uploaded by this pass. Sizes/SHA-256 for the retained
  artifacts were read **from disk** for cross-checking only; no file was rebuilt, re-signed or moved.
- No build, push, merge or rebase was performed by this pass. The subsequent B6 integration reconciliation
  fetched `origin`, verified `dev == origin/dev == 0c4172346764c5622a5cdbfa06a4ce624d3cd56a` before editing and
  fast-forwarded `dev` locally by one docs-only commit; nothing was pushed.
- E evidence is E evidence; the Redmi install report is a release/install issue; neither is A or P.

## 9. Integration record (2026-09-14, ff-only)

- Preflight: `git fetch`, then `dev == origin/dev == 0c4172346764c5622a5cdbfa06a4ce624d3cd56a`; base
  `cefa15eb7ba42d32b60d31ecf16d626f356344d5` is an ancestor of that HEAD and `cefa15eb7..0c4172346` contains
  exactly three commits (`f86ef812b`, `53dd1368e`, `0c4172346`) with **zero merge commits** → the integration was
  **ff-only**, with no squash and no merge commit.
- Production scope is unchanged and still `ChatActionCell.java` only (**+22/−1**); the debug control commit is
  debug source only (`TMessagesProj_App/src/debug/.../CybergramShowcaseActivity.java`, +26/−1).
- The integration itself is docs-only: the status lines in this record, `docs/CURRENT_STATE.md`,
  `docs/EXECUTION_BACKLOG.md`, `docs/passes/README.md`, `docs/WORK_STATE.md`, the B5 records and
  `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md` changed from feature-branch/not-integrated to
  **integrated-on-dev**. No production code, debug code, manifest, resource, release asset or
  `docs/CYBERGRAM_UI_SPEC.md` change is part of it, and nothing was pushed.
- Limitations carried forward unchanged: `E-rich` unavailable pre-auth (not faked); `A` pending; `P` not
  required unless a device/OEM runtime defect appears; `ThemePreviewActivity` current foreign app-theme preview
  has no `ChatActionCell` row, so no extra production opt-out is required now, and the **latent future-preview
  caveat remains**; B4 remains `DESIGN-OPEN / NOT AUTHORIZED` and next product work must not silently start B4.
- The Redmi Note 10S / MIUI 14.0.4 manual install failure remains **unresolved**: the exact installer error has
  **not** been captured. The universal 4-ABI APK (§4.4) exists as the explicit compatibility probe and has not
  been run on that device — **no fix is claimed**.
