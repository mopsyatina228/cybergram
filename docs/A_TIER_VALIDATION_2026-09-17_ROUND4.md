# Cybergram A-tier validation — 2026-09-17 (round 4)

Status: `PARTIAL A — B0 selected-chip label PASS / B1 navigation PASS / B2 and B5-B6 NOT EXERCISED`.

Authorization: owner ruling D10.4 (`docs/OWNER_DECISIONS_2026-09-17_ROUND4.md` §9) grants the A-tier run
and accepts the read-marking side effect of opening conversations.

## Preflight

- tested revision: `dev` HEAD `cd4a4c3a8`, working tree **clean** before and after the run;
- product sources versus the preserved cut `52b8e219729d0a90dd3335165cf4ef44acf46e5e`:
  `git diff --stat 52b8e219... HEAD -- TMessagesProj/src/main` → **15 files, +776/−218**;
- `git diff --check` → clean;
- `Tools/validate_cybergram_theme.py` → `unknown=0 duplicates=0 malformed=0`, exit 0.

## Build and install

- `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64 --offline` → `BUILD SUCCESSFUL in 3m 55s`;
- APK `TMessagesProj_App/build/outputs/apk/afat/debug/app.apk`, 72,919,901 bytes, SHA-256
  `5095D727C22720466962BCF8254CAF3A445823EC9A371ECBCF5E8C07A1D3CCD0`;
- `adb install -r` → `Success`; package `org.telegram.messenger.beta` (the official
  `org.telegram.messenger` was never touched).

## Authenticated target

`emulator-5554`, AVD `Cybergram_API36`, Android API 36, x86_64, 1080×2400 @ 420 dpi, authenticated
session. This is **A tier** (authenticated product surface on an emulator), **not P**.

## Results

### B0 — filter tabs: PARTIAL (the re-run item PASSES)

- The **selected chip now renders with its label and badge**: tapping `263679` shows `263679` + badge
  `1` on the angular Cybergram selected plate, and the list switches to that folder; tapping
  `12412412 108` and `All Chats` switches back with the labels intact. This confirms the B0-FIX
  (production `3911690fe`) on the authenticated surface.
  Artifacts: `at-b0-chip1.png`, `at-b0-chip2.png`, `at-b0-allchats.png`.
- **Not exercised:** horizontal overflow/scroll to the last folder, page swipe/interpolation,
  long-press/menu, edit/reorder/delete mode, and the **non-Cybergram control** — the last needs a
  persisted theme mutation, which this run deliberately did not perform.

### B1 — main bottom navigation: PARTIAL (navigation PASSES)

- Chats → Contacts → Settings → Profile → Chats were all reached; the Cybergram angular dark panel and
  the cyan selected plate track the active tab; the Contacts unread badge renders; Profile shows the
  Cybergram profile surface. Artifacts: `at-b1-chats.png`, `at-b1-contacts.png`,
  `at-b1-settings.png`, `at-b1-profile.png`, `at-b1-back-chats.png`, `at-b1-back-chats2.png`.
- Note: the Contacts screen raises the system contacts-permission prompt, which consumed one tap in
  the first pass; `at-b1-settings.png` was therefore re-captured from the Chats screen and is the
  valid Settings evidence.
- **Not exercised:** Calls enable/disable and the Settings/Calls swap, reselect/scroll-to-top,
  long-press/long-drag selection, tabs show/hide animation, orientation change, attach/bot tab
  geometry, and the non-Cybergram control.

### B2 — the 15 account-dependent rows: NOT EXERCISED

No row was driven; they require specific message/media/reaction data.

### B5/B6 — service/date plate: NOT EXERCISED

No date separator, ordinary service row or rich/special row was opened in this run.

### Cross-cutting

- `FATAL EXCEPTION`: **0**; `ANR in org.telegram.messenger.beta`: **0** (logcat cleared before the
  matrix and scanned after);
- Day ↔ Cybergram theme switch: **not performed** (persisted theme mutation, outside the granted
  consent);
- no defect was observed, and nothing was fixed opportunistically during the run.

## Verdicts

| pass | verdict |
|---|---|
| B0 | `PARTIAL` — re-run item PASS; overflow/swipe/menu/edit and non-Cybergram control open |
| B1 | `PARTIAL` — navigation PASS; interaction/animation/orientation and non-Cybergram control open |
| B2 | `UNVALIDATED` — 15 rows |
| B5/B6 | `UNVALIDATED` |
| P (physical/OEM) | not run — the Samsung/OEM tier remains untouched |

All artifacts live in `.local-artifacts/run-r4-20260917/` (git-excluded). No screenshot, session
material, phone number or message content is committed.

This run authorizes no production change, no push and no release.
