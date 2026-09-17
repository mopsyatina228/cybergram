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
| B0 | `PARTIAL` — re-run item PASS; non-Cybergram control PASS (P: upstream rounded chips); overflow/swipe/menu/edit open |
| B1 | `PARTIAL` — navigation PASS; non-Cybergram control PASS (P: upstream rounded nav); interaction/animation/orientation open |
| B2 | `UNVALIDATED` — 15 rows |
| B5/B6 | `UNVALIDATED` |
| P (physical/OEM) | not run — the Samsung/OEM tier remains untouched |

All artifacts live in `.local-artifacts/run-r4-20260917/` (git-excluded). No screenshot, session
material, phone number or message content is committed.

This run authorizes no production change, no push and no release.

## Follow-up run — B2 rows, B5/B6 plate, P reachability (same date)

Runner note: the AVD was unstable on this host. The host-GPU renderer (`-gpu auto`) crashed
`qemu-system-x86_64` within about a minute of boot; the software renderer (`swiftshader_indirect`) was
stable but slow enough to raise `ANR in com.android.systemui`. The run resumed after (a) stopping a
6.4 GB Gradle daemon, (b) restarting the adb server — the emulator process had actually survived its
launcher exiting, only the adb connection was lost — and (c) running the AVD headless
(`-no-window -gpu swiftshader_indirect`). All evidence below is from the authenticated AVD
`emulator-5554`; artifacts are in `.local-artifacts/run-b256/`.

### B2 — per-row verdicts

| # | case | verdict | evidence |
|---|---|---|---|
| 8 | cell-side group slicing/clipping | PASS | grouped run of three outgoing messages in Saved Messages; joined silhouettes with no slice leak (`b2-saved-group.png`) |
| 10 | incoming media | PASS | photo inside the angular body (`b2-meta.png`, `b2-uvoleno.png`) |
| 12 | caption layout / time-on-media | PASS | captions + time readable in the body (12:30 PM; 8:07 AM) |
| 13 | media clipping + touch targets | PARTIAL | media is clipped to the angular silhouette; touch targets not probed |
| 14 | cell multi-select overlay/ripple | PASS | `1 Selected`, selection overlay follows the bubble, Reply/Forward bar (`b2-cellselect2.png`, `b2-reactionlist.png`) |
| 18 | reply name/text layout | PASS | reply quote bars with name + cyan link (`b2-meta.png`, `b2-cellselect2.png`) |
| 19 | quote/code/link/contact lines | PARTIAL | reply/quote lines, links and an in-bubble **link-preview card** (OpenAI Help Center) render (`b2-reactionlist.png`); code/contact/fact-check absent from the sampled chats |
| 25 | reaction emoji glyph rendering | PASS | 👍😱 and a rich row (⭐57 👍24 ❤️12 👎12 🔥2 🤬2) render correctly |
| 26 | reaction touch/bounce/scrim/particles | NOT EXERCISED | the reaction pill renders and is reachable, but the animation itself is not observable in a still; the reactor-list attempt aborted when the emulator died |
| 27 | time / checks / views metadata | PASS | times, delivery checks and views (25.1K, 7.8K, 3110) readable |
| 28 | forwarded header/state | PASS | `Forwarded from / Блокировки Рунета \| Новости` header legible inside the angular bubble, with a link-preview card beneath (`r2-hishchnik.png`) |
| 29 | links plus metadata | PASS | links render cyan with adjacent metadata (`b2-bfm.png`), and a link-preview card (`b2-reactionlist.png`) |
| 30 | bot buttons bottom / body | PARTIAL | the bot chat (Harness) shows the composer **Menu** button and a channel message shows a bottom **comments** button (71 comments), but no inline bot keyboard was present (`b2-harness.png`, `b2-kirill.png`) |
| 31 | service/date cell adjacent | PASS | `April 13` plate and `Unread Messages` divider adjacent to ordinary messages (`b2-uvoleno.png`) |
| 35 | `TYPE_PREVIEW` on a live client | PASS | the Cybergram tile in Chat Settings → Color theme renders the **Cybergram palette** (near-black canvas, olive incoming, blue-green outgoing, no wallpaper); the rounded preview silhouettes are the documented B3 `TYPE_PREVIEW` exclusion, not a defect (`r2-themepreview-zoom.png`) |

Tally: **11 PASS / 3 PARTIAL / 1 NOT EXERCISED / 0 defects.**

### B5/B6

- ordinary date separator: **PASS** — `April 13` renders as a compact dark **angular** plate with
  chamfered corners and amber text (`b56-datesep.png`, 4x nearest-neighbour crop);
- ordinary service actions (pin/unpin, join/leave, title/photo/TTL change, group call, screenshot),
  service-message reactions, and the `SharedMediaLayout` floating date: **NOT EXERCISED** — none
  appeared in the sampled chats;
- rich/special rows (gift, star/offer, community, wallpaper, birthday, story, `TYPE_ACTION_PHOTO`):
  **UNAVAILABLE** — no such message was present; not faked, so `E-rich` stays `UNAVAILABLE`;
- latent preview caveat (a foreign-theme preview row instantiating a `ChatActionCell`): not observed.

### P (physical / OEM)

**PASS (Redmi Note 10S).** A physical device became reachable during the run:
`4H8L598LAME6CEX4`, `rosemary_ru` / `M2101K7BNY` (Redmi Note 10S), Android 13 (SDK 33), MIUI `V140`,
arm64-v8a — the exact device family from the R1 install report.

- build `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a --offline` → `BUILD SUCCESSFUL
  in 1m 49s`; APK 68,455,859 bytes, SHA-256
  `D115E38692D51F096E507D754FF3CE591DCDF820971BAAA76B5E72A6EDC60622`;
- **`adb install -r` → `Success`** on MIUI V140 — this closes the R1 arm64 install-compatibility item
  for this device: the arm64 APK does install. The likely original cause was a signature/package
  conflict with an already-installed `org.telegram.messenger.beta`, since that debug package was
  already present and the in-place update succeeded;
- launch → focused `DefaultIcon`, `FATAL EXCEPTION` = 0, `ANR in org.telegram.messenger.beta` = 0;
- the Cybergram chrome renders on the physical device: angular bottom navigation with the cyan selected
  plate, red header rail and composer separator, dark dialogs list, angular bubbles with the corner
  spur, the joined reply run, and the shorter composer;
- the microphone plate is a dark angular frame with a thin outline and the glyph is **pale blue**: its
  dominant pixel colour is exactly `8FBFCC` (`CybergramTheme.ICON_PALE`), and the zoom shows the glyph
  centred in its frame (`p-mic-zoom.png`); the paperclip shares the same colour;
- the device locale is Russian, so the chrome also survives localization;
- artifacts: `.local-artifacts/run-p-20260917/` (`p-launch.png`, `p-chat.png`, `p-mic-zoom.png`).

This is P-tier evidence for the round-4 changes and for the R1 install item. It is a smoke, not the
full P interaction matrix, and the Samsung SM-A256E target was not attached — genuine Samsung/OEM
confidence is still outstanding.

**Non-Cybergram control (P, unplanned but valid).** On the same device the active theme was
**non-Cybergram** (light Day theme) during a later part of the session. That supplied the control the
emulator runs could not produce: the dialogs list renders the **upstream rounded filter chips** and the
**upstream rounded bottom navigation with no red rails**, the chat bubbles are the **upstream rounded
bubbles** (no chamfer, no corner spur), and the composer shows the **upstream bright rounded control
button and rounded field** instead of the Cybergram angular plate
(`p-noncyber-dialogs.png`, `p-noncyber-mic.png`, `p-nc-composer-zoom.png`). This confirms that the
round-4 microphone plate/tint changes and the Cybergram chrome are presentation-gated. **No theme
mutation was performed** — the control was observed because the device's active theme was already
non-Cybergram.

### Round-2 runner note — AVD became unusable

After the round-1 pass the AVD stopped being viable for further interactive work: it exited repeatedly
within about a minute of boot (the host-GPU renderer crashed `qemu-system-x86_64`; the software
renderer booted but the app raised `ANR in org.telegram.messenger.beta` during cold start and the
emulator then exited). Four restart attempts — headless, stale locks cleared, Gradle daemon stopped —
all failed the same way. Rows 28 and 35 were captured before the instability; **B2 row 26** (reaction
touch/bounce/scrim/particles) and the remaining **B5/B6 ordinary service actions** (pin/unpin,
join/leave, title/photo/TTL change, group call, screenshot actions, service-message reactions and the
`SharedMediaLayout` floating date) could therefore not be exercised. They stay *unexercised* rather
than guessed, and the rich/special rows stay `UNAVAILABLE`.

### Verdicts after the follow-up

| pass | verdict |
|---|---|
| B2 | `PARTIAL` — 11 PASS / 3 PARTIAL / 1 not exercised / 0 defects |
| B5/B6 | `PARTIAL` — ordinary date plate PASS; ordinary service actions and reactions not exercised; rich `UNAVAILABLE` |
| P | `PASS` (Redmi Note 10S smoke: install + launch + chrome + microphone colour/centring); Samsung/OEM target not attached |

No defect was observed in the exercised rows, and nothing was fixed opportunistically.
