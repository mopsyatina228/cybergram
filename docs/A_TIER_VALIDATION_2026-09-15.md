# A-tier validation record (2026-09-15)

Status: `IN PROGRESS — FIRST AUTHENTICATED RUN`. This document records evidence; it authorizes nothing.

This is the first recorded run of Cybergram on an **authenticated** Telegram surface. Every earlier
validation in `docs/WORK_STATE.md` was E-tier (pre-auth emulator), and every status document up to
2026-09-15 stated that A-tier was unavailable because no authenticated session existed. That assumption is
now false and the affected entries are being corrected.

Procedure used: `docs/runbooks/CYBERGRAM_A_TIER_VALIDATION.md`.

## 1. Tested artifact (provenance)

| Item | Value |
|---|---|
| Target | AVD `Cybergram_API36`, `emulator-5554`, Android 16 / API 36, `x86_64`, 1080x2400 |
| Package | `org.telegram.messenger.beta` (official `org.telegram.messenger` untouched) |
| Version | `12.10.1` / versionCode `70389`, minSdk 21 / targetSdk 36 |
| Installed APK SHA-256 | `0c781a6b71f13034b36586b4c596a48a3b8719e108d51195dc89c305a78e12f0` |
| Installed APK size | `113,631,225` bytes |
| Artifact identity | the recorded **universal 4-ABI** build of `53dd1368e` (B6 branch HEAD), pulled from the device and hashed in this run |
| Install timestamp on device | 2026-09-14 11:04:04 |
| Session | authenticated (real dialog list, real folders, unread counters) |

Product-tree identity: `git diff --stat 23882dbf0..HEAD -- TMessagesProj TMessagesProj_App` is **empty**, so
the installed binary corresponds to the current product sources at `HEAD` (`39c5df9fc`). Everything committed
after `23882dbf0` is `README.md`, `design/` and `docs/` only. A-tier evidence collected here is therefore
valid for the current product tree.

## 2. Static pre-checks

- `git diff --check` — clean, exit 0;
- `python Tools/validate_cybergram_theme.py` — `unknown=0 duplicates=0 malformed=0`, `OK`, exit 0;
- no rebuild was performed or needed: the installed APK already matches the tested product sources.

## 3. B1 — main bottom navigation, A-tier results

Source matrix: `docs/passes/B1_MAIN_TABS_FLAT.md` (A section). Artifacts (git-excluded, never committed):
`.local-artifacts/a-tier/`.

| Item | Verdict | Evidence |
|---|---|---|
| Chats → Contacts → Settings → Profile navigation | **PASS** | separate screenshots per tab |
| Profile → Chats return | **PASS** | screenshot |
| Angular dark outer panel, no large glass capsule | **PASS** | visible on all four tabs |
| Cyan angular selected plate follows the active tab | **PASS** | the plate tracks Chats / Contacts / Settings / Profile |
| Badge/counter rendering | **PASS** | unread badge renders on the Contacts tab |
| Profile avatar | **PASS** | avatar renders on the Profile tab |
| Long press on a tab | **PASS** | upstream context menu appears; the flat selector does not break it |
| No FATAL/ANR | **PASS** | `crash_matches=0` after every step; top activity remained the app |
| Calls tab enabled/disabled and Settings/Calls swap | not exercised | needs a settings change |
| Reselect / scroll-to-top | not exercised | |
| Long-drag selection across tabs | not exercised | only long-press was exercised |
| Tabs show/hide animation | not exercised | |
| Orientation / configuration change | not exercised | |
| Non-Cybergram theme: upstream glass/rounded tabs intact | not exercised | requires a theme switch (a persisted change; the B6 run used `.local-artifacts/b6/` backups to restore it) |
| Attach/bot tabs keep upstream selector geometry | not exercised | |

B1 therefore moves from `A PENDING` to **`A PARTIAL`**: the visual/navigation core is confirmed on a real
authenticated surface, the interaction edge cases above are not.

## 4. Not covered by this record

- **B0** (filter tabs authenticated matrix) — not started, although the production dialogs surface with its
  folder row is reachable and visible in the B1 Chats screenshots;
- **B2** — all 15 account-dependent `UNTESTED` rows remain untested;
- **B5/B6** — the authenticated ordinary and rich service/date rows remain untested;
- **P** — no physical device was involved; nothing here is OEM/Samsung evidence.

Several of those require opening real conversations, which marks messages as read in a live account. That
side effect is a decision for the repository owner, not something this run should assume.

## 5. Privacy handling

Screenshots of an authenticated client contain personal data (display names, avatars, phone number,
usernames, message previews). They are kept only under the git-excluded `.local-artifacts/a-tier/` and are
referenced here by name, never embedded. No account data, contact data or session material is recorded in
this document or anywhere else under version control.

## 6. Consequence for the status documents

The chain "A-tier is blocked because no authenticated session exists", repeated in `docs/CURRENT_STATE.md`,
`docs/EXECUTION_BACKLOG.md`, `docs/STATUS.md` and the pass records, is no longer accurate. A-tier work can
proceed; the remaining constraint is time and the side effects listed in §4, not availability.
