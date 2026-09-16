# Cybergram A-tier validation runbook

Purpose: make the authenticated (A-tier) validation debt executable as one checklist instead of five
scattered documents. This is a validation runbook, not an implementation task.

`A` means the real product surface is exercised with an authenticated Telegram session, on an emulator or
a physical device. `docs/EXECUTION_BACKLOG.md` defines the tiers; `docs/runbooks/CYBERGRAM_EMULATOR_VALIDATION.md`
covers the E tier. **Never promote E evidence into A or P.**

## Hard rules

- **Do not modify production code during this run.** A defect stops the run and becomes a separately
  authorized bounded fix (`docs/passes/B0_FILTER_TABS_VALIDATION.md` "Stop rule").
- Use `org.telegram.messenger.beta`. **Never touch or uninstall the official `org.telegram.messenger`.**
- Never commit session material, phone numbers, message content, API IDs/hashes or screenshots. Keep every
  artifact in git-excluded `.local-artifacts/`.
- Do not create an authenticated session as part of an E run, and do not treat pre-auth rendering as A.
- `E-rich` is a separate recorded state (`UNAVAILABLE PRE-AUTH`); it is not faked by a parallel renderer.

## 0. Pre-flight (always)

1. Fresh-fetch `dev` and `master`; update `dev` fast-forward only. Never reset or discard unknown local work.
2. Read `AGENTS.md`, `docs/CURRENT_STATE.md`, `docs/EXECUTION_BACKLOG.md`, the relevant pass spec, and this file.
3. Record the exact tested SHA and `git status`.
4. Confirm whether product sources differ from the preserved cut
   `52b8e219729d0a90dd3335165cf4ef44acf46e5e`:
   `git diff --stat 52b8e219... <SHA> -- TMessagesProj/src/main`.
   If product code changed after the recorded build, rebuild; do not cite a stale APK.
5. Static checks for the exact tested source:
   - `git diff --check`;
   - `python Tools/validate_cybergram_theme.py` (expect `unknown=0 duplicates=0 malformed=0`, exit 0);
   - the central gate `CybergramTheme.isCybergramPresentation(...)` is still the only presentation gate,
     and no theme-name or colour heuristic was introduced;
   - non-Cybergram presentation paths still render the upstream (rounded/glass) shapes.
6. Build and install:
   - emulator: `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64`;
   - physical arm64 device: same task with `-PCYBERGRAM_ABI=arm64-v8a`;
   - record APK path, byte size, SHA-256, `adb install -r` result.
7. Clear logcat before the matrix and scan afterwards for `FATAL EXCEPTION`,
   `ANR in org.telegram.messenger.beta`, and process death. Record the match count.

## 1. B0 — filter tabs (`FilterTabsView`)

Source: `docs/passes/B0_FILTER_TABS_VALIDATION.md`. Validation only; no fixes.

> **Status banner (2026-09-16):** B0 was **already run** on the authenticated AVD on 2026-09-15 and
> **stopped on a confirmed defect** — the selected chip renders as an empty chamfered plate because the
> Cybergram plate is opaque and drawn after the labels (`FilterTabsView.java:1531`). The fix is
> `CONFIRMED DEFECT / FIX NOT AUTHORIZED` (`docs/B0_FILTER_TABS_DEFECT_2026-09-15.md`). Do **not**
> re-run this section as a blind "pending validation": it re-runs only **after** an authorized fix, and
> the checklist below then covers the post-fix matrix.

- [ ] selected and unselected tabs;
- [ ] tapping between tabs;
- [ ] horizontal overflow/scroll when enough folders exist;
- [ ] page swipe / manual interpolation;
- [ ] unread counters where available;
- [ ] long-press / menu behaviour;
- [ ] edit / reorder / delete mode where available;
- [ ] Cybergram visual: dark chamfered outer panel and selected plate, no old large blurred/rounded pill;
- [ ] switch to a non-Cybergram theme and confirm the upstream rounded/blurred presentation returns;
- [ ] screenshots of at least the Cybergram selected and unselected states.

P: a representative smoke on the Samsung SM-A256E is desirable before release confidence; emulator evidence
must not be labelled Samsung/OEM evidence.

## 2. B1 — main bottom navigation (`MainTabsActivity`)

Source: `docs/passes/B1_MAIN_TABS_FLAT.md` (A section).

- [ ] Chats → Contacts → Settings → Profile;
- [ ] Calls tab enabled/disabled and the Settings/Calls position swap;
- [ ] current-tab reselect and scroll-to-top;
- [ ] long press and long-drag selection across tabs;
- [ ] badge/counter cases available;
- [ ] profile avatar;
- [ ] tabs show/hide animation;
- [ ] navigation-bar / inset placement;
- [ ] configuration or orientation change where practical;
- [ ] Cybergram: angular dark panel and selected plate, no large glass capsule;
- [ ] non-Cybergram theme: upstream glass/rounded tabs intact;
- [ ] attach/bot tabs keep upstream selector geometry (the opt-in must not leak);
- [ ] the known unverified visual choice: the Cybergram panel is flush to `MainTabsLayout` bounds while the
      upstream capsule was inset — confirm the flat footprint on a real surface;
- [ ] no FATAL/ANR.

## 3. B2 — the 15 account-dependent `UNTESTED` rows

Source: `docs/B2_MESSAGE_STATE_AUDIT_2026-09-12.md`. These are untested, **not** known defects.

| # | case | what to check |
|---|---|---|
| 8 | cell-side group slicing/clipping | slices must not leak outside the joined silhouette (`ChatMessageCell:23641-23645`) |
| 10 | incoming media | mirrors case 9; same production branch with `isOut=false` |
| 12 | caption layout / time-on-media row | caption and metadata readable inside the angular body |
| 13 | media clipping + touch targets | clip follows `makePath()`; touch targets preserved (`ChatMessageCell:16914/26040`) |
| 14 | cell-level multi-select overlay/ripple | overlay follows the new path (`ChatMessageCell:20332-20345`, `18620-18642`) |
| 18 | reply name/text layout, ripple, selector | readable and hit-testable (`ChatMessageCell:19562-19584`, `22648-22865`) |
| 19 | quote/code/link/contact/fact-check lines | upstream-consistent (`ReplyMessageLine.TYPE_*`) |
| 25 | reaction emoji glyph rendering | glyphs actually render with a real reactions map |
| 26 | reaction touch, bounce, scrim, particle animation | interactions unchanged |
| 27 | time / checks / views metadata | metadata readable on the new silhouette |
| 28 | forwarded header/state | forward chrome legible |
| 29 | links plus message metadata | links keep cyan semantics (UI spec line 61) |
| 30 | bot buttons bottom / body interaction | `botButtonsBottom` keeps working |
| 31 | service/date cell adjacent to ordinary messages | compact dark plate beside angular bodies |
| 35 | `TYPE_PREVIEW` on a live Cybergram client | the Cybergram theme preview should look like Cybergram |

## 4. B5/B6 — service/date plate

Source: `docs/B6_SERVICE_DATE_IMPLEMENTATION_2026-09-14.md` §6-7, `docs/B5_SERVICE_DATE_AUDIT_2026-09-13.md`.

- [ ] ordinary rows: date separators, pin/unpin, join/leave, title/photo/TTL changes, group-call,
      screenshot actions — all on the angular plate, readable, correctly measured;
- [ ] rich/special rows (previously `E-rich UNAVAILABLE`): gift, star/offer, community, wallpaper,
      birthday, story, `TYPE_ACTION_PHOTO` — these need real message data; do not fake them;
- [ ] bot buttons / ribbons and the suggested-post-approval override stay on their upstream paths;
- [ ] service-message reactions: the upstream `ReactionsLayoutInBubble` overlay still renders correctly
      over the new ordinary plate (B4 remains `DESIGN-OPEN`);
- [ ] `SharedMediaLayout` floating date uses `new ChatActionCell(context)` with a null provider and the
      `Theme.getCurrentTheme()` fallback — confirm it angularizes as expected;
- [ ] latent preview caveat: a foreign-theme preview row that instantiates a `ChatActionCell` would need an
      explicit opt-out; note whether such a row now exists.

## 5. Cross-cutting

- [ ] Day ↔ Cybergram theme switch changes presentation **without recreating the activity**;
- [ ] no Cybergram surface changes on a non-Cybergram theme, including the header decoration and the
      canvas HUD once it lands;
- [ ] accessibility: decorative Cybergram layers add no focusable node and never intercept touch;
- [ ] scroll, typing, selection, long-press and pull-down behave exactly as upstream;
- [ ] no new FATAL/ANR across the whole run.

## 6. Required handoff

Report, factually:

- tested SHA and its relation to the preserved product cut;
- local `git status` before and after;
- static-check results (including the theme validator output);
- build command, APK path, size, SHA-256, install result, app/package version;
- authenticated target type/device/API;
- every case exercised, with its verdict, and every case **not** exercised;
- screenshot/artifact paths (never in Git);
- FATAL/ANR scan result;
- verdict per pass: `PASS`, `FAIL` or `PARTIAL/UNVALIDATED`;
- physical-device (P) confidence status, kept separate;
- any defect reproduction, without opportunistic fixes.

Update `docs/WORK_STATE.md` with the chronological record and the affected entries in
`docs/EXECUTION_BACKLOG.md` / `docs/CURRENT_STATE.md`. Closing A on one pass does not close it on another.

This runbook authorizes no production change, no push and no release.
