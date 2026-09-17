# R-series implementation — 2026-09-17

Status: `IMPLEMENTED ON dev / E+P EVIDENCE / NO PUSH`.

Base: `dev` at `e52aa0e40`. These are the three recorded code-review findings that
`docs/EXECUTION_BACKLOG.md` listed as **recorded findings, unauthorized**; the owner authorized work
on them ("определи дальнейшие цели и работай", on top of the earlier carte blanche).

Every change is Cybergram-gated or neutral: no non-Cybergram rendering path is modified.

## R-PERF — the bubble border path was rebuilt every frame

Finding: the Cybergram outline is stroked on the final user canvas from
`MessageDrawable.drawCybergramBorder(...)` (called from the two `draw` paths), and that method
re-walked the shared polygon with `generateCybergramPath(...)` on every frame.

Change (`MessageDrawable.java`): the outline path is now cached in `cybergramBorderPath` and rebuilt
only when one of its inputs changes — the drawable bounds or the `isTopNear` / `isBottomNear` / `isOut`
/ `currentType` flags. The paint is already created once (`getBorderPaint()`), and the per-frame
colour/alpha assignments are unchanged.

Why it is safe: `generateCybergramPath(...)` writes only into the path it is given, and the border path
is used nowhere else, so a cached path cannot leak into the fill path or another bubble.

## R-STUB — the D4 grid could decorate the per-chat default-theme stub

Finding: `ChatActivity.ChatActivityFragmentView.getBackgroundDrawableFromTheme(...)` returns
`new ColorDrawable(Color.BLACK)` for the `chatTheme.showAsDefaultStub` branch, and that plain
`ColorDrawable` passed `CybergramBackdropDrawable.isEligible(...)`, so the D4 grid/frame could be
painted on a theme placeholder.

Change (`ChatActivity.java`, `getNewDrawable()`): the backdrop is skipped when
`themeDelegate.chatTheme != null && themeDelegate.chatTheme.showAsDefaultStub`, i.e. on exactly the
object that builds the stub. Everything else (user wallpaper, motion wallpaper, other ColorDrawables)
keeps the previous behaviour.

Not reproduced on device: no chat with a default-theme stub was at hand during the run. The guard is a
direct boolean on the same `EmojiThemes` instance that selects the stub branch, so it cannot diverge.

## R-OVERLAY — the always-on inert header overlay

Finding: every `ChatActivity`, including non-Cybergram, carried a full-size inert
`CybergramHeaderDecorationView` whose `onDraw` gated itself away on every frame.

Change (`CybergramHeaderDecorationView.java`): the view now sets its own visibility from the Cybergram
gate — `VISIBLE` under Cybergram, `GONE` otherwise — and re-evaluates it in `onAttachedToWindow()` and
on the global `NotificationCenter.didSetNewTheme` (registered in `onAttachedToWindow`, removed in
`onDetachedFromWindow`). The `onDraw` gate stays as a safety net.

Why this keeps runtime theming: a detached view re-evaluates on attach, and an attached view
re-evaluates on the theme-change notification, so a Day ↔ Cybergram switch still needs no activity
recreation. A `GONE` view is skipped by the measure/draw traversal, removing the per-frame cost when
Cybergram is not active. `setVisibility` is idempotent, so no notification storm is possible.

## Evidence

- arm64 build `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a --offline` →
  `BUILD SUCCESSFUL in 4m 11s`; APK 73,396,975 bytes, SHA-256
  `4691E66B5A8DFC35706912CADA4D76CDD9D97AC6FA974418D674A3C9125AA179`;
- Redmi Note 10S (`4H8L598LAME6CEX4`): `adb install -r` → `Success`; launch `FATAL EXCEPTION` = 0;
- under Cybergram the angular bubbles keep their outlines and the red header rail is present, i.e.
  R-PERF and R-OVERLAY did not disturb the presentation (`rs-chat2.png`);
- the R-OVERLAY round trip was exercised on the device via the in-app theme switch
  (`Настройки чатов` → `Переключить на дневную/ночную тему`): Cybergram → Day (upstream rounded
  bubbles, no red rail) → Cybergram, with no crash and no activity recreation (`rs-day.png`,
  `rs-cybergram-back.png`);
- artifacts: `.local-artifacts/run-rseries-20260917/` (git-excluded).

## Residual

R-STUB is verified statically (the device reproduction needs a chat that carries a default-theme
stub); R-OVERLAY's in-chat theme switch relies on the `didSetNewTheme` notification and was exercised
through the settings screen rather than while a `ChatActivity` stayed in the foreground.
