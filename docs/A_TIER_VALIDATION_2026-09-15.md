# A-tier validation record (2026-09-15)

Status: `IN PROGRESS — FIRST AUTHENTICATED RUN`. This document records evidence; it authorizes nothing.

Section 7 supersedes section 3 for the B1 rows it re-tests.

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

- **B0** (filter tabs authenticated matrix) — **partially exercised**: the production dialogs surface was reached and §8 confirms the selected-chip defect, but the rest of the B0 matrix was not run because the run stopped at that defect (fix not authorized); note this supersedes the earlier "not started" wording;
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

## 7. B1 second pass (2026-09-15, later run)

Further B1 items exercised on the same authenticated device. Artifacts in `.local-artifacts/a-tier/`.

| Item | Verdict | Evidence |
|---|---|---|
| Reselect the active tab / scroll-to-top | **PASS** | tapping the already-selected Chats tab returned the list to the top (stories row visible); `crash_matches=0` |
| Long press on a tab | **PASS** | upstream tab popups open and render correctly: the folder list on Chats, the account switcher on Profile. The Cybergram flat plate does not break them |
| Long-drag selection **across** tabs | **NOT REACHABLE on the phone layout** | both tested tabs consume long-press with their own upstream popup, so the drag selector never appears; its geometry is exercised only by the DEBUG fixture at E tier |
| Orientation / configuration change | **N/A** | the top activity requests `SCREEN_ORIENTATION_PORTRAIT` (upstream), so a rotation request produces no layout change. Rotation settings were restored afterwards |
| No FATAL/ANR | **PASS** | `crash_matches=0` after every step |

Useful side result: the Chats-tab popup enumerates the folders — `All Chats`, `263679`, `12412412`, `0`.
Those titles are therefore drawn normally by upstream code; the empty chip in the filter row is the
**selected** one, which independently confirms the B0 defect recorded in
`docs/B0_FILTER_TABS_DEFECT_2026-09-15.md` for a third time.

Side-effect disclosure: `accelerometer_rotation` was set to 0 for the orientation attempt and restored to
1; `user_rotation` was returned to 0. No app setting, permission, message or account data was changed, and
no permission was granted (`READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `CAMERA` all remained `granted=false`).

### B1 items still not exercised

- Calls tab enabled/disabled and the Settings/Calls position swap — requires a persisted app setting change;
- tabs show/hide animation;
- attach/bot tab geometry — requires opening a conversation with a bot.

The non-Cybergram theme comparison, originally in this list, was performed afterwards — see §8.

B1 remains **`A PARTIAL`**: the navigation and presentation core is confirmed, the three items above are not.

## 8. Non-Cybergram control (theme switch), 2026-09-15

The `Blue` (non-Cybergram) theme was applied on the same device, account, session and build, and the same
screens re-captured. Procedure and integrity notes are recorded in
`docs/B0_FILTER_TABS_DEFECT_2026-09-15.md` ("Theme switch procedure and integrity"); the theme was restored
to `Cybergram` afterwards and the device config re-read to confirm it.

| Item | Under Cybergram | Under Blue (non-Cybergram) | Verdict |
|---|---|---|---|
| Dialog filter row, **selected** chip | chamfered plate, label missing | upstream rounded translucent pill, label fully legible | the Cybergram seam causes the B0 defect; upstream is intact |
| Unselected filter chips | labelled | labelled | same |
| Main bottom navigation | angular dark panel + cyan selected plate | upstream rounded/glass look, labels intact | **PASS** — B1's non-Cybergram requirement is now exercised |
| Header rails, cyan search outline | present | upstream chrome | same |
| No FATAL/ANR | `crash_matches=0` | `crash_matches=0` | — |

This closes two previously open matrix items: B0's "switch to a non-Cybergram theme and verify upstream
presentation is restored" and B1's "non-Cybergram theme: upstream glass/rounded tabs intact".

B1's remaining open items are now: the Calls enable/disable and Settings/Calls swap, the tabs show/hide
animation, and the attach/bot tab geometry (which needs a bot conversation opened).

## 9. Deliberate stop of the settings-mutation line (2026-09-15)

The B1 items still listed above — the Calls tab enable/disable and Settings/Calls swap, the tabs show/hide
animation and the attach/bot tab geometry — plus every B2/B5/B6 row, were deliberately **not** exercised.
They require either a persisted change to the owner's app settings or opening real conversations, which marks
messages as read in a live account.

While looking for the Calls setting through the app's own UI, a system contacts-permission dialog appeared on
the live account. It was dismissed with the deny action and **no permission was granted**
(`READ_MEDIA_IMAGES`, `WRITE_CONTACTS`, `CAMERA` all remained `granted=false`; `READ_CONTACTS` is not
granted). Probing was stopped there: the remaining checkboxes are worth far less than the risk of interfering
with a live messaging session.

Those items stay open, and closing them needs either an explicit owner decision or a separate test account.
The one settings mutation that *was* performed — the non-Cybergram theme control in §8 — was evidence-critical,
byte-exact, backed up and fully restored.
