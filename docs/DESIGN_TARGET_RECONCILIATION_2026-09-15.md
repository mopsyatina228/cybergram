# Design target reconciliation and work plan (2026-09-15)

Status: `ANALYSIS / OWNER DECISIONS RULED` — **D1, D2 and D3 were all ruled by the owner on
2026-09-15**; the binding record (including the owner's own D3 = default wallpaper, recorded here as
D4) is `docs/OWNER_DECISIONS_2026-09-15.md`. The analysis below is kept as the historic
reference-versus-spec comparison. This document still authorizes nothing by itself.

Input: `design/DESIGN_TARGET.md` and `design/references/design-target-hex-chat.jpg` (owner reference,
committed 2026-09-15 as `39c301bb4`), compared against the design authority
`docs/CYBERGRAM_UI_SPEC.md`, the landed product state, and the pass structure in
`docs/EXECUTION_BACKLOG.md` / `docs/passes/`.

Snapshot base for every source claim below: `dev` = `39c301bb448e65cc17fe2132e1b12612860fa732`.
The design-target commit changes `README.md` and `design/` only; `TMessagesProj/src/main` is byte-identical
to `9f8211503`, so the ownership facts recorded in `docs/passes/B7_CHAT_CANVAS_HUD.md` still hold.

Purpose: the reference is a new design input that the backlog predates. It must be reconciled before it
is used to justify implementation, because it partly confirms and partly contradicts the current
authority. No production work is started by this document.

## 1. What the reference confirms

The reference validates the landed direction far more than it disturbs it:

- **Colour roles.** Cyan = self/outgoing, amber = peer/incoming, red = service/"watching" layer. This is
  exactly `docs/CYBERGRAM_UI_SPEC.md` lines 57 and 61 and exactly the landed
  `CybergramTheme.CYAN` / `AMBER` / `DANGER` role model. The reference is a confirmation, not a change.
- **Background.** Reference `#05070A`–`#0A0E14` against landed `CybergramTheme.BACKGROUND = #080A0F`,
  panel `#0B0D12`, raised `#111820`. Compatible within a few units; no action needed.
- **Thin outlines instead of fills.** Reference uses ~1.5 px amber/cyan bubble outlines over dark
  surfaces; spec lines 73-74 ask for "1 px-ish" outlines; that is what landed. Confirmed.
- **No large glass capsules.** Reference has none; spec line 21 declares them an anti-target; landed
  chrome already removed them. Confirmed.
- **Compact date/service plate.** Reference shows a small bordered "Сегодня" pill; spec line 63 and the
  integrated B6 ordinary service/date plate match. Confirmed.
- **Composer shape.** Emoji / divider / muted placeholder / attach / mic, no send button at rest;
  spec line 65 and the landed angular composer agree. Confirmed.
- **Decorative layer only at the edges, never over text.** The reference's labels sit in unused edge
  space (`pointer-events: none` in the owner's own words); `DESIGN_TARGET.md` ends with "декор всегда
  фон, текст всегда поверх". This is the B7 mission statement, and spec lines 55 and 113 already
  require it. Confirmed.

## 2. Deltas and conflicts

| # | Item | Reference | Spec / landed | Verdict |
|---|---|---|---|---|
| 1 | Bubble silhouette | "radius ~8px со встроенным «хвостиком»" (rounded + tail) | Spec lines 71-74: clipped/angled corners, tail removed or reduced; landed `CybergramBubbleDrawable.buildPath(...)` is a 45-degree per-corner chamfer with no tail; B2 verified `TYPE_TEXT`/`TYPE_MEDIA` angular bodies as PASS | **Direct conflict.** Rounded+tail would re-open Stage C and invalidate verified B2/B6 geometry |
| 2 | Amber hue | `#FFB300` (orange-gold) | Landed `AMBER = #E8D93A`, `AMBER_HIGHLIGHT = #F2E75B` (acid yellow) | Palette delta, not a structural conflict; centrally changeable |
| 3 | Red hue | `#FF003C` (magenta-red) | Landed `DANGER = #FF2E46` | Minor palette delta |
| 4 | Service-label copy | `NCPD_NET`, `CH 1.0.3.7`, lock + `SECURE CHAT` / `END-TO-END`, `CITY ALWAYS WATCHES`, `BETTER PEOPLE / BRIGHTER / TOMORROW`, `CONNECTION STABLE` + signal bars | Spec line 117 bans fake security claims by name; `AGENTS.md` bans third-party branded assets; `DESIGN_TARGET.md` itself already excludes `NCPD`/`Night City` | **Partial conflict.** The *layer* is sanctioned (spec line 113); this specific *copy* is not |
| 5 | Background pattern | Faint red geometric/hex pattern behind the bubbles | Spec does not define one; B7 rules require wallpapers to be neither obscured, cropped, tinted away nor re-scaled | New decision; must not become a wallpaper override |
| 6 | Service-label typeface | Monospace uppercase, letter-spaced, 8-10sp | Spec line 108 allows "a redistributable open font ... later for headings or compact HUD labels"; nothing is bundled today (landed chrome is the system family `sans-serif-condensed`) | New asset decision; needs an OFL/open font and an APK-size note |
| 7 | Header identity type | Condensed geometric sans | Landed `sans-serif-condensed` through `CybergramTypography` | Compatible; no action |

Nothing in the table is a defect in landed work, and nothing here is validated by machine evidence.

## 3. Decisions required from the owner

**D1 — message silhouette.** Keep the landed 45-degree chamfer (spec), or adopt the reference's rounded
8 px + tail?
Recommendation: **keep the chamfer.** The spec is explicit, B2 verified the angular path, B5/B6 built on
it, and the reference is stated to be a mood reference rather than a layout spec. If the owner wants the
rounded direction, it must be a **new bounded pass with its own spec** (re-opening Stage C), not an
opportunistic edit, and it would supersede recorded B2 evidence.
Cost of changing: high — `MessageDrawable`, grouped joins, selection overlays, B6 plate consistency,
plus new E/A validation.

**Ruling (owner, 2026-09-15): keep the chamfer.** The landed 45-degree
`CybergramBubbleDrawable.buildPath(...)` silhouette stands. D1 therefore closes with **no production
change and no amendment to `docs/CYBERGRAM_UI_SPEC.md`**; Stage C is not re-opened and the recorded
B2/B5/B6 geometry evidence remains valid. The reference's rounded 8 px + tail direction is retired
unless the owner later opens a new bounded Stage C spec.

**D2 — palette hues.** Keep landed amber `#E8D93A` / red `#FF2E46`, or move toward the reference's
`#FFB300` / `#FF003C`?
Recommendation: put this to an E-tier visual comparison rather than deciding from hex values; the change
itself is cheap and central (`CybergramTheme` + `cybergram.attheme` + validator rerun), but it touches
every surface at once and the reference has been through JPEG compression and a screen render, so its
sampled hues are not authoritative.

**Ruled (owner, 2026-09-15): adopt the reference hues** (`#FFB300`, `#FF003C`, muted `#6B7A8A`) —
the owner ruled directly rather than waiting for the A/B recommendation. See
`docs/OWNER_DECISIONS_2026-09-15.md` §2 D2.

**D3 — service-label copy policy.** Which decorative labels are allowed?
Hard constraint, non-negotiable: no `SECURE CHAT`, no `END-TO-END`, no lock/security iconography, no
invented IDs or status that misrepresent Telegram (`CYBERGRAM_UI_SPEC.md` line 117, repeated as a hard
stop in the B7 contract). No third-party brands or slogans.
Recommendation: allow **original, non-deceptive** technical labels only, and treat `CONNECTION STABLE`
plus signal bars as the same class of offence as `SECURE CHAT` (it asserts a live network property the
client does not verify). Default for B7: **structural marks now, textual labels deferred until D3 is
ruled**.

**Ruled (owner, 2026-09-15): neutral Latin technical copy is allowed, including deliberately
meaningless technical gibberish** (`CH 1.0.3.7`-class strings); every security and live-network claim
named above stays banned. Note the numbering clash: the owner called this decision `D2`; in this
repository it is `D3`. See `docs/OWNER_DECISIONS_2026-09-15.md` §2 D3.

## 4. Work plan derived from the existing map

The map already exists, so this is an execution order inside it, not a second roadmap. Superseded
later on 2026-09-15/16: the A-tier assumption below was **wrong** — the `Cybergram_API36` AVD is
authenticated and the first A run partially validated B1 and confirmed the B0 defect
(`docs/A_TIER_VALIDATION_2026-09-15.md`), so the live constraint is the read-marking side effect and
owner time, not availability. The other statements here held at reconciliation time: B4 stays
`DESIGN-OPEN / NOT AUTHORIZED`; B8 stays deferred; R1 stays a separate release track; D2/D3 were ruled
and the authority was amended on 2026-09-16.

| Order | Item | Status | Depends on |
|---|---|---|---|
| 1 | **B7 — chat-canvas HUD layer** (map's next actionable implementation pass) | Contract ready; **needs the owner's explicit priority decision** | D3 for any textual label; nothing else. D1 is out of B7's scope by contract |
| 2 | **D2 visual comparison** for the two candidate palettes | Not started | An E-tier run and a screenshot A/B; can run in parallel with B7 |
| 3 | **Reconcile `DESIGN_TARGET.md` into the authority docs** (`CYBERGRAM_UI_SPEC.md` palette/typography/HUD sections, `CURRENT_STATE.md`) | Not started | D2 and D3 |
| 4 | **D1 decision** — **ruled 2026-09-15: keep the landed chamfer**; no new Stage C spec is written and the item is closed | **Closed** | D1 ruling (done) |
| 5 | A-tier validation debt (B0/B1/B2/B5/B6) | Available (the AVD is authenticated; B1 partially validated, B0 stopped on the defect) | Owner time and the read-marking side effect |
| 6 | R1 install compatibility (Redmi Note 10S / MIUI 14.0.4) | Blocked | Exact `INSTALL_FAILED_*` text from the device |

**Recommendation: start at item 1 (B7).** It is the only unblocked implementation work in the map, the
reference confirms its mission, its contract is prepared and its ownership was re-verified on
2026-09-15, and its scope explicitly excludes the two contested areas (message renderers and forbidden
copy). Items 2 and 3 are documentation/validation work that can follow.

### B7 pre-flight result (re-verified 2026-09-15 at `39c301bb4`)

The contract's mandatory re-reconnaissance was re-run; every anchor holds:

- `ChatActivity.ChatActivityFragmentView` at `ChatActivity.java:17085` and a **separate**
  `ChannelAdminLogActivity.ChatActivityFragmentView` at `ChannelAdminLogActivity.java:4493` — the seam
  does not leak into the admin log;
- container draw pipeline still overridden: `addView(View, int, ViewGroup.LayoutParams)` 17342,
  `onDraw(Canvas)` 17539, `drawChild(Canvas, View, long)` 17550;
- wallpaper layer: `SizeNotifierFrameLayout.java:92` `public View backgroundView`,
  `setBackgroundImage` 363, `getBackgroundImage` 431, `skipBackgroundDrawing` 586, `BackgroundView`
  guarded at 173; `TAG_DRAWING_AS_BACKGROUND` branches at `ChatActivity.java:17540`, 17543, 17566, 17568,
  17576. The contract's claim that an unknown child returns `false` and is therefore not captured into a
  blur pass was **confirmed by reading the code** in the follow-up pre-flight recorded in
  `docs/passes/B7_CHAT_CANVAS_HUD.md` § "Pre-flight findings", which also resolves the insertion-index
  question; runtime proof is still required at execution;
- child order unchanged: `invalidateBlurredSourcesView` 4561, `chatListView` 6979,
  `chatActivityFadeView` 6985, `selectionReactionsOverlay` 6989, `animatingImageView` 6994,
  `progressView` 6998, `actionBar` 7797, `overlayView` 7807,
  `CybergramHeaderDecorationView` 8986 (the precedent to copy);
- **correction to the contract:** `20` files match `extends SizeNotifierFrameLayout` in the current
  tree (19 hosts besides `ChatActivity`), not the "19 classes" the contract states; and index `1` is not
  free at runtime — `videoPlayerContainer` is inserted there (`ChatActivity.java:12157`) and
  `thanosEffect` at `1 + indexOfChild(chatListView)` (`ChatActivity.java:44375`). The insertion index
  must therefore be proven, not assumed;
- `CybergramHeaderDecorationView` is also used by `DialogsActivity.java:4750` with a decor-state
  provider, confirming the reusable pattern.

## 5. Explicit non-goals

- This document does not authorize B7, does not authorize a palette change, and does not amend
  `docs/CYBERGRAM_UI_SPEC.md`. The design authority is amended only by an owner design ruling.
- No production file may be changed on the basis of "the reference shows it". The reference is a mood
  document; the spec is the contract.
- Do not adopt any third-party brand, slogan, logo or security claim from the reference.
