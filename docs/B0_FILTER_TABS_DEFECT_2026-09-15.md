# B0 filter-tabs defect (2026-09-15)

Status: **CONFIRMED DEFECT on an authenticated surface. NOT FIXED.**
A fix requires separate authorization; this document records the finding and stops the B0 run, per the
stop rule in `docs/passes/B0_FILTER_TABS_VALIDATION.md`.

## Summary

The **selected** dialog filter/folder tab renders as an empty chamfered plate: its title is not drawn.
Unselected tabs keep their titles. The user cannot read the name of the folder they are currently in.

## Environment and provenance

| Item | Value |
|---|---|
| Target | AVD `Cybergram_API36`, `emulator-5554`, Android 16 / API 36, `x86_64` |
| Package | `org.telegram.messenger.beta`, `12.10.1` / versionCode `70389` |
| Session | authenticated (live account) |
| Installed APK SHA-256 | `0c781a6b71f13034b36586b4c596a48a3b8719e108d51195dc89c305a78e12f0` (universal 4-ABI build of `53dd1368e`), verified by device pull + hash |
| Product tree | identical to `HEAD` `84ecca370`: `git diff --stat 23882dbf0..HEAD -- TMessagesProj TMessagesProj_App` is empty |
| Static pre-checks | `git diff --check` clean; `Tools/validate_cybergram_theme.py` → `unknown=0 duplicates=0 malformed=0`, `OK` |

## Reproduction

1. Open the app on the authenticated session and go to the Chats screen (dialogs).
2. Observe the filter row: `All Chats` plus folder chips. Every **unselected** chip shows its title.
3. Tap a chip. The tapped chip becomes selected (cyan-outlined chamfered plate) and **its title vanishes**.
4. Tap a different chip. The previously selected chip regains its title; the newly selected one loses it.

The rule held on every attempt: **selected ⇒ no label, unselected ⇒ labelled**. Two independent captures
with the selection in different positions show it, and the same behaviour occurs for `All Chats` itself.

Artifacts (git-excluded, never committed, contain personal data):
`.local-artifacts/a-tier/b0_all_baseline.png`, `b0_sel_a.png`, `b0_sel_b.png`, and the 2x crops
`crop_baseline.png` / `crop_sel.png` that show the empty plate next to labelled neighbours.

`crash_matches=0` throughout: this is a rendering defect, not a crash.

## Root cause (verified by reading the code, not inferred)

`FilterTabsView.drawChild` draws the list — which includes every tab label — and then paints the selector
plate **on top of it**:

```java
protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
    boolean result = super.drawChild(canvas, child, drawingTime);
    if (child == listView) {
        drawSelector(canvas);            // drawn AFTER the labels
    }
```

`drawSelector` then sets the plate alpha per presentation:

```java
Drawable selectedPlate = useCybergramPresentation() ? cybergramSelectorDrawable : selectorDrawable;
selectedPlate.setBounds(...);
selectedPlate.setAlpha(useCybergramPresentation() ? (int) (255 * listView.getAlpha()) : 31);
selectedPlate.draw(canvas);
```

`TMessagesProj/src/main/java/org/telegram/ui/Components/FilterTabsView.java:1531`

The upstream branch uses alpha **31** (≈12%), which is why painting the plate over the labels is harmless
upstream. The Cybergram branch uses alpha **255** — fully opaque — and the plate it draws is an opaque
fill (`CybergramTheme.PANEL_RAISED`, `#111820`) with a cyan stroke (constructed at lines 927-930). An
opaque plate painted after the text hides the text.

So this is not a colour-contrast subtlety: the selected tab's label is **painted over**.

## Impact

- The active folder/filter is unidentifiable by name on the dialogs screen; only position and the unread
  counter remain. Directly contradicts `docs/CYBERGRAM_UI_SPEC.md` line 17 ("Every screen must remain
  readable and usable at normal phone scale") and defeats the purpose of the flat filter-tab treatment —
  the plate requirement is met visually while the label is lost.
- Scope: `FilterTabsView`, i.e. the dialogs screen and folder navigation. The B1 main bottom navigation is
  unaffected (its labels render correctly).
- Not a crash, not a state-machine change; Telegram behaviour is otherwise intact.

## Control run — the root cause is confirmed experimentally, not only by reading code

After the static analysis above, the same screen was captured under a **non-Cybergram** theme on the same
device, same account, same session, same build.

| Theme | Selected first chip | Other chips |
|---|---|---|
| **Cybergram** | chamfered plate, **no label at all** | `263679`, `12412412`, `0` — all labelled |
| **Blue** (non-Cybergram) | upstream rounded translucent pill, **`All Chats` fully legible** (with its counter) | `263679`, `12412412`, `0` — all labelled |

Switching back to Cybergram reproduced the empty plate, so the behaviour tracks the theme and not the
capture: **the Cybergram seam is the cause**, and the upstream branch is unaffected. Artifacts:
`.local-artifacts/a-tier/control_blue_full2.png`, `blue_filters2.png`, `crop_baseline.png`,
`restored_filters_final.png` (git-excluded).

## Theme switch procedure and integrity

The theme was changed by replacing exactly one string in the app's own preferences, with the app stopped:

- read `shared_prefs/mainconfig.xml` through `run-as` and saved it byte-for-byte before touching anything
  (`mainconfig_before_Cybergram.xml`, SHA-256 `12da72ba…`);
- replaced only `<string name="theme">Cybergram</string>` with `Blue` and pushed the result back;
- captured the control frames, then reversed exactly the same operation and re-read the device config to
  confirm `<string name="theme">Cybergram</string>` (screenshots `control_blue_full2.png`,
  `restored_final.png`).

The restored preferences file is **112 bytes smaller** than the pre-run backup. The difference is entirely
values the application itself rewrote while it ran — check timestamps, promo/birthday/stories state, the
TON cache rate, a rotated `autologinToken`, and a dropped empty `proxyDialogAddress`. The only edit made
here was the theme string. The backups contain session material and stay in the git-excluded
`.local-artifacts/a-tier/`; they must never be committed.

## Explicitly not claimed

- Not proven to be the *only* mechanism: an illegible dark-on-dark text colour would look identical in a
  screenshot. The opaque-plate-over-text path is sufficient and is verified in the code, but it has not
  been isolated by an instrumented test.
- The non-Cybergram theme branch was **not** exercised in this run; statically it still uses alpha 31 and
  was not modified by the Cybergram seam.
- No P/OEM evidence; emulator only.

## Proposed bounded fix (NOT APPLIED — needs authorization)

Candidate approaches, to be chosen in a bounded fix pass with its own spec:

1. **Draw the Cybergram plate before the labels** (for the Cybergram branch only), instead of after
   `super.drawChild` — keeps upstream's branch byte-identical and removes the overdraw entirely.
2. Reduce the Cybergram plate alpha and/or use a translucent fill, so the label remains legible through it
   — closest to upstream's own contract but weaker visually than a real plate.
3. Give the selected label an explicit Cybergram-safe colour — only valid if the plate stays behind it.

Approach 1 is the smallest correct change, but it must be proven not to disturb the selector animation,
`listView` translation/scale handling and the clip path. Validation: E (build + install + screenshot under
Cybergram and under a non-Cybergram theme) plus an A re-run of the exact reproduction above.

## Stop-rule compliance

`docs/passes/B0_FILTER_TABS_VALIDATION.md`: "If a defect appears, record exact reproduction and stop. Do
not fix production code inside B0." This run stopped at the defect. **No production file was modified**;
no fix was attempted.

Remaining B0 matrix items not exercised in this run (the run stopped): horizontal overflow/scroll to the
end of the folder row and edit/reorder/delete mode. The non-Cybergram theme comparison **was** performed —
see the control run above.
