# Owner decision log — 2026-09-15 (round 2)

Status: `AUTHORITATIVE OWNER RULINGS — RECORDED`. This file records binding owner decisions taken
after `DESIGN_TARGET_RECONCILIATION_2026-09-15.md`. It closes the decisions it names and authorizes
the bounded implementation items it marks **IMPLEMENT**; everything else in the repository keeps its
current status, and the normal build/validation discipline still applies.

Base at recording time: `dev` = `origin/dev` = `a469d5ed9` (12 commits pushed; see §6).

## 0. Owner instruction, verbatim (2026-09-15, RU)

> В первую очередь пуш локальных коммитов, переоценка прогресса проекта. Дальнейшие решения
> пересмотреть в этом контексте. D1 - оставить чамфер. D2 - нейтральные технические надписи на
> латинице, техническая абракадабра и тарабарщина. D3 - это "обои по умолчанию", которые могут быть
> заменены. Но минимальное обрамление необходимо. Вкусовое: палитру - подгоняем под референс. Шрифт
> нужно адаптировать под референс, приоритет за кириллицей. Разрешено пользоваться авторизованным
> аккаунтом в эмуляторе. Дополнения: нужно увеличить расстояние между баблами, как на референсе, а
> также добавить флажки. Окантовка композера гораздо тусклее и слабо выражена. На текущем билде в
> верхней панели видны два маленьких кружка неизвестного назначения по краям экрана. Либо убрать,
> либо разобраться что это за рудимент.

## 1. Numbering map — the owner's D2/D3 are NOT the repo's D2/D3

| Owner label | Repository id | Subject |
|---|---|---|
| `D1` | `D1` (`DESIGN_TARGET_RECONCILIATION_2026-09-15.md` §3) | message silhouette |
| `D2` | `D3` (same §3) | service-label copy |
| `D3` | new `D4` (this file) | default wallpaper layer |

Everything in this file therefore uses the **repository id** plus the owner label in parentheses.

## 2. Rulings

**D1 — message silhouette (owner `D1`). RULED: keep the chamfer.** Already recorded in
`a469d5ed9`. The landed 45-degree `CybergramBubbleDrawable.buildPath(...)` silhouette stands; Stage C
stays closed, `docs/CYBERGRAM_UI_SPEC.md` is not amended, and the recorded B2/B5/B6 geometry evidence
remains valid. **No code change.**

**D2 — palette hues (owner: "палитру подгоняем под референс"). RULED: adopt the reference hues. IMPLEMENT.**
This supersedes the reconciliation's earlier "decide from an E-tier A/B comparison" recommendation —
the owner ruled directly. Target values, mapped onto `CybergramTheme`:

| Role | Landed | Ruled |
|---|---|---|
| amber / incoming | `#E8D93A` | **`#FFB300`** |
| amber highlight | `#F2E75B` | `#FFC94D` (derived tint of the ruled amber) |
| danger / red service layer | `#FF2E46` | **`#FF003C`** |
| muted text | `#7C8A91` | **`#6B7A8A`** |
| UI text | `#E6F2F2` | `#E6F7FF` (reference warm white) |
| cyan / outgoing | `#00E5FF` | unchanged (reference agrees) |
| background / panel / raised | `#080A0F` / `#0B0D12` / `#111820` | unchanged — already inside the reference range `#05070A`–`#0A0E14` |

Scope: `TMessagesProj/src/main/java/org/telegram/ui/ActionBar/CybergramTheme.java` and
`TMessagesProj/src/main/assets/cybergram.attheme`, including the amber/cyan bubble-surface
composites that must be recomputed from the new amber. Role semantics (cyan = self, amber = peer,
red = service/watching layer) are unchanged.

**D3 — service-label copy policy (owner `D2`). RULED: neutral Latin technical text, including gibberish.**
ALLOWED: original, non-deceptive technical inscriptions in Latin script, uppercase, monospace/HUD
style — explicitly including deliberately meaningless technical "abracadabra" (e.g. channel/index/
frequency-shaped strings such as `CH 1.0.3.7`). Labels stay decorative, edge-anchored,
non-interactive, never over text.
STILL BANNED (hard, unchanged): `SECURE CHAT`, `END-TO-END`, lock/security iconography, invented IDs
or status that misrepresent Telegram, any live network/connection claim (this includes
`CONNECTION STABLE` and its signal bars), third-party brands/slogans (`NCPD`, `Night City`), and any
text drawn over message content. `docs/CYBERGRAM_UI_SPEC.md` line 117 stands.
Consequence: the B7 label blocker is cleared for neutral copy; B7 itself is still NOT AUTHORIZED.

**D4 — default wallpaper layer (owner `D3`). RULED: ship a default replaceable wallpaper with minimal framing. IMPLEMENT.**
A Cybergram default wallpaper/pattern layer is allowed and expected, with a *minimal* framing
(thin technical edge framing only). Hard constraints: the user's own wallpaper choice must always
override it; it must not crop, tint away, re-scale or obscure a user wallpaper; it must not intercept
touch; and it must not violate the existing B7 rule that decoration never sits over message text.

**D5 — typography, Cyrillic first.** Adapt the type to the reference with **Cyrillic as a
first-class script, not a fallback**. Interface: geometric/condensed sans with Cyrillic; HUD service
layer: monospace uppercase with Cyrillic. Only redistributable open (OFL/Apache-class) fonts may be
bundled — `docs/CYBERGRAM_UI_SPEC.md` line 108 — with an APK-size note. No proprietary or
game-ripped fonts.

**D6 — bubble spacing.** Increase the vertical distance between message bubbles toward the reference
(`design/DESIGN_TARGET.md` line 29: gap ≈ 12 px, bubbles ≤ 75–80 % width), without changing message
measurement, scroll correctness, grouped-bubble joins, or the non-Cybergram path. **IMPLEMENT.**

**D7 — composer outline.** The Cybergram chamfered frame around the input field reads as far dimmer
and weaker than the reference (~1.5–2 px visible stroke). Strengthen it so the composer frame is
clearly legible, while keeping it a frame (no rounded capsule, no neon fill). **IMPLEMENT.**

**D8 — the two small marks in the header. INVESTIGATED, then REMOVE.**
They are not upstream and not system UI: they are the two cyan chamfered "ticks" drawn by
`TMessagesProj/src/main/java/org/telegram/ui/CybergramHeaderDecorationView.java`
(`onDraw`, tick size 8 dp, `HEADER_TECH_ALPHA = 180`, at `ax+24dp` and `ax+aw-24dp` just above the
header rule — lines 129–138). At 8 dp with a 1 dp stroke they read as unexplained small circles at
the screen edges. Ruling: **remove the two ticks** (and the red edge rails that share that corner
zone if they read the same way); keep the red bottom rule and the short cyan identity segment, which
match the reference header. **IMPLEMENT.**

## 3. Validation policy

Use of the **authenticated account in the `Cybergram_API36` AVD is explicitly authorized** by the
owner, including opening conversations. Note the known side effect: opening a chat marks its messages
read. Evidence from these runs may be recorded in the docs; private message content must not be
transcribed into the repository (existing practice).

## 4. Open ambiguity — "флажки"

`добавить флажки` is not resolved. Best reading: the outgoing send-state check marks
("галочки") should be present and reference-styled (`design/DESIGN_TARGET.md` line 40: one check =
sent). Machine evidence on the current build (`a469d5ed9`) shows a **cyan double check already drawn
on outgoing messages**, so if the owner means check marks, the change is stylistic, not additive.
Alternative reading: a deliberately added flag/marker decoration. **Not implemented until the owner
confirms which is meant.**

## 5. Consequences for the existing queue

- `DESIGN_TARGET_RECONCILIATION_2026-09-15.md` item 3 ("reconcile the design target into the authority
  docs") is now unblocked for palette and label policy; only the font (D5) still needs a concrete
  choice.
- B7's copy blocker is cleared by D3 for neutral Latin text; B7 remains **NOT AUTHORIZED** and still
  needs an explicit owner priority decision.
- B0-FIX remains `CONFIRMED DEFECT / FIX NOT AUTHORIZED` — D2 changes its plate colours, not its
  draw-order defect.
- The A-tier debt (B0/B1/B2/B5/B6) is now runnable with the owner's authorization in §3.

## 6. Implementation record

**Implemented in this round** (build + emulator evidence in `docs/WORK_STATE.md` § "Owner ruling
round 2 — reference palette and chrome (2026-09-15)"):

- **D2** palette — `CybergramTheme` (`AMBER #FFB300`, `DANGER #FF003C`, muted/text/in-bubble
  recomputed) and `cybergram.attheme` (97 lines remapped). Verified by device pixel sampling:
  incoming outline `#FAAF00`/`#FBB000`, header rule composite `#C10332` (`G = 0x03` ⇒ `#FF003C`).
- **D7** composer outline — stroke `dp(1) → dp(1.5)`; measured 2 px → 4 px in a 2× crop.
- **D8** header rudiment — the two 8 dp cyan chamfered ticks were identified and removed; absent in the
  post-change captures.
- One **ANR** during the run is documented and attributed to an upstream `CalendarActivity` /
  `VideoPlayer` main-thread path, not to this pass.

**Ruled but deliberately still open** (each needs its own bounded contract):

- **D6** bubble spacing — the code seam is known (`ChatMessageCell` drawable bounds + `MessageDrawable`
  padding), but the time/check baseline has ~3.5 dp of headroom, so the change must move the metadata
  cluster with it; not attempted as an opportunistic edit.
- **D5** font — measurement result: the already-bundled `fonts/rmono.ttf` (Roboto Mono) and
  `fonts/rcondensedbold.ttf` (Roboto Condensed Bold) both carry full Cyrillic (255 U+04xx codepoints),
  so Cyrillic-first needs no new asset; the remaining question is only whether to bundle an additional
  geometric sans, which costs APK size and needs its own justification.
- **D4** default wallpaper with minimal framing — not started.
- **Owner `флажки`** — unresolved (§4); nothing changed for it.

**Not changed by this round:** `docs/CYBERGRAM_UI_SPEC.md` (D1 = keep, so no amendment is due),
message geometry, measurement/layout, the non-Cybergram path, and B0's confirmed filter-tab defect.

## 7. Progress reassessment snapshot (same date)

- `dev` == `origin/dev` == `a469d5ed9`; working tree clean; 12 commits pushed (verified with a live
  `git fetch` on 2026-09-15).
- Integrated product passes: B1, B2 (audit only), B5 (audit only), B6; B7 spec-only / not authorized;
  B0 is validation-only and stopped on the confirmed filter-tab label defect; B0-FIX unauthorized.
- Product diff versus the preserved cut `52b8e219` over `TMessagesProj/src/main`: 4 files, +177/−9
  (`ChatActionCell` B6; `GlassTabView`/`MainTabsActivity`/`MainTabsLayout` B1).
- Largest outstanding item remains A-tier verification debt, not new presentation work.
- **Stale documents** (flagged, not yet fixed): `docs/STATUS.md` header table still claims HEAD
  `09a6f2601`, "4 ahead, nothing pushed"; `docs/CURRENT_STATE.md:3` and
  `docs/EXECUTION_BACKLOG.md:5` still say "last reconciled 2026-09-14"; B1 section of the backlog and
  `docs/passes/B1_MAIN_TABS_FLAT.md` still say `A PENDING` although the 2026-09-15 A run is PARTIAL;
  `docs/WORK_STATE.md` has no 2026-09-15 entry.
