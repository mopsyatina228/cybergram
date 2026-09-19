# Owner decisions — 2026-09-19 (round 5: B4, B7, publication, B1 verdicts)

Status: `AUTHORITATIVE OWNER INSTRUCTIONS — RECORDED / IMPLEMENTED WHERE POSSIBLE`.

Base: `dev` at `87b3b655b` at the start of this record; the working tree carried the B7 changes below.
Repository ids continue from `D11` (round 4 ended at `D10.5`).

## 0. Owner instruction, verbatim (2026-09-19, RU)

> B4 (reply/reaction) — DESIGN-OPEN / NOT AUTHORIZED; B7 (chat-canvas HUD) — спека готова, NOT
> AUTHORIZED – бери в работу.
> Два PARTIAL в B1 (show/hide, long-drag) — ограничения наблюдаемости/поведения, не дефекты. - тоже
> займись.
> Опубликуй 20 коммитов в origin/dev
> Проверь R-серийный диф (3 файла) и правки композера/микрофона.
> 3. Возвращать служебные настройки телефона ... не нужно.
> Актуальную сборку залей в гит

## 1. Rulings

### D11.1 — B4 is authorized: angular reply plate and reaction pill

Under Cybergram the **in-bubble reply plate** and the **in-bubble reaction pill** use the same shared
45-degree chamfer polygon as the message bodies (`CybergramBubbleDrawable.buildPath(...)`; no second
geometry implementation). Because `ReplyMessageLine` and `ReactionsLayoutInBubble` are shared well
beyond the message flow, each carries an **explicit per-instance opt-in** in addition to the central
presentation gate; the classes are never branch-on-theme. `ChatMessageCell` opts in only the message
`replyLine` (both layout paths) and only its own `reactionsLayoutInBubble`.

Implemented: `ReplyMessageLine.setCybergramAngular(...)` + chamfer branch in `drawBackground(...)`;
`ReactionsLayoutInBubble.setCybergramAngular(...)` propagated to each created `ReactionButton`;
`ReactionButton.drawRoundRect(...)` chamfer branch (pill, its scrim overlay and its service-shader
background). Untouched: quote/link/contact/fact-check/summary lines, rich-text editors, story captions,
article views, `ChatActionCell` service reactions, and every non-Cybergram path. Record:
`docs/B4_ANGULAR_REPLY_REACTION_2026-09-17.md`.

### D11.2 — B7 is authorized: the chat-canvas HUD layer

Option A of `docs/passes/B7_CHAT_CANVAS_HUD.md` is adopted: a dedicated, non-interactive,
runtime-gated `CybergramChatCanvasHudView` inserted as a full-size sibling directly below
`chatListView` (above the wallpaper, behind the message list), drawing four sparse chamfered corner
brackets inside a 7 dp edge band with the shared cut/stroke — no surface, no microtext, no claims.

The HUD adds one child above the wallpaper, so the two later fixed-index inserts in `ChatActivity` were
compensated (`3 + cybergramHudIndexOffset()` for `emptyViewContainer`, `17 + cybergramHudIndexOffset()`
for `topUndoView`); the offset is 1 exactly when the HUD is attached, so both point at the same sibling
as upstream in either case. Record: `docs/passes/B7_CHAT_CANVAS_HUD.md` §Execution record.

### D11.3 — publication

The 20 local `dev` commits were pushed to `origin/dev` (no force; `edb9354ed → 32cd6d38c`, then
`1f2c78498`, `65cd6a038`, `c289a0ee1`, `4b55d3b89`, `87b3b655b`, `7807a75c1`). The current build was
published as the GitHub prerelease `dev-20260917-32cd6d38c` with the universal 4-ABI asset
`Cybergram-dev-32cd6d38c-universal.apk` (113,634,737 bytes, SHA-256
`BF9B5ABC0866E482E0777C35461020DA5701C6FB321628033271A3A0C7EA5745`). Record: `docs/WORK_STATE.md`.

### D11.4 — the two B1 PARTIALs are resolved to PASS on ownership

Both are upstream behaviour that the B1 Cybergram seam does not touch: the tabs show/hide animation
runs entirely in `AnimatedLinearLayout.setViewVisible(child, visible, animated)`
(`AnimatedLinearLayout.java:62`) through `MainTabsActivity.checkUi_callTabVisible`; the long-drag
selection across tabs is upstream `MainTabsLayout` (`lastLongSelectedView`, the `selectedTabPositionX/Y`
springs, `setTabSelected(found, true)`, `MainTabsLayout.java:444-463`). No defect in either; the only
residual is that a transition cannot be evidenced in a still. Record:
`docs/A_TIER_VALIDATION_2026-09-17_ROUND4.md` §Round 4b.

## 2. Validation boundary

B4 and B7 are `IMPLEMENTED / BUILD SUCCESSFUL` but their required device evidence is **not produced**:
the host terminates the AVD the moment the Telegram app starts inside it, and the Redmi is unplugged
(`docs/EMULATOR_DIAGNOSIS_2026-09-19.md`). The one-shot checklist that closes both matrices when a
device is available is `docs/B4_B7_DEVICE_EVIDENCE_PROCEDURE.md`. No non-Cybergram path was changed and
no defect was fixed opportunistically.
