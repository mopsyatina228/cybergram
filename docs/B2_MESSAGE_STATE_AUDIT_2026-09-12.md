# B2 — message-state coverage and ownership audit

Status: `INTEGRATED / AUDIT COMPLETE`

Integrated revision: `2e9a6a1d066975727cbb4b3888c490a8acbf798a` on `dev` (fast-forward from
`audit/cybergram-message-states`; commits kept separate: DEBUG-only fixture `b2bfdd418`, audit/docs
`6c682e153`, semantic correction `2e9a6a1d0`). The production diff from base over `TMessagesProj/src/main`
is empty.

Type: evidence/audit (`docs/passes/B2_MESSAGE_STATE_AUDIT.md`)

Tested revision: `e000ef8286a406fc27fc55889c286ebbffef2890` (`dev`, clean worktree)

Audit branch: `audit/cybergram-message-states`

Evidence class: local source reconnaissance (authoritative for this tree) + E emulator fixture rendered
from real production renderers. No authenticated (A) surface was available, so account-dependent states
are marked `UNTESTED` rather than guessed.

## 0. Semantic correction applied after consultant review

Revised result: **14 PASS / 0 CONFIRMED DEFECT / 6 DESIGN-OPEN / 15 UNTESTED** across 35 cases.

The original run classified six reply/reaction rows as `DEFECT (polish)`. Consultant review reclassified
those six rows as `DESIGN-OPEN`. **No source, runtime or pixel evidence changed** — every measurement in
sections 1-4 is the evidence gathered by the original run and was not re-measured. Only the verdict/design
classification differs.

Reasoning against the actual design authority, `docs/CYBERGRAM_UI_SPEC.md`:

- the spec requires **angular outer message silhouettes** — line 69, “Cybergram replaces rounded Telegram
  message silhouettes with an angular drawable”;
- the spec requires replies and reactions to **survive** that geometry — line 25, “Message length,
  localization, large fonts, media, reactions and reply blocks must survive the new geometry”;
- the anti-target is stated at line 21 as **“large rounded/glass Material capsules”**. It does **not**
  require compact internal semantic controls — the `ReplyMessageLine` plate/bar or the
  `ReactionsLayoutInBubble` pill — to become angular themselves. Reading the anti-target as “any small
  rounded semantic button” over-extends the spec.

The measured fact therefore stands (the plate/pill are circular while the outer body is chamfered), but
“confirmed production defect” is not supported by the design authority. The supported classification is an
open design question.

Consequences, effective immediately:

- **B4 = `DESIGN-OPEN / NOT AUTHORIZED`.** No production implementation spec is written for reply/reaction
  styling, and `ReplyMessageLine.java` / `ReactionsLayoutInBubble.java` must not be modified on this basis.
- **B3 = `CLOSED / NOT REQUIRED`** (unchanged): `MessageDrawable.TYPE_PREVIEW`’s only production callers are
  theme-preview surfaces, so no implementation task is created for it (section 5.1).

## 1. Environment and artifacts

- AVD `Cybergram_API36`, Android 16 / API 36, `x86_64`, 1080x2400, density 2.625.
- Build: `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64` -> `BUILD SUCCESSFUL`.
- APK: `TMessagesProj_App/build/outputs/apk/afat/debug/app.apk`, 74,277,072 bytes,
  SHA-256 `a9d6d5ed8e4bc05c98bf561a6107696d0d692bc2faeac75ac7a4ae7623e22e08`.
- Install/launch: `org.telegram.messenger.beta` updated in place; normal `LaunchActivity` and DEBUG
  `CybergramShowcaseActivity` both resumed and stable; **no FATAL/ANR** in any window.
- Screenshots (local only, git-excluded): `.local-artifacts/b2/03_normal_launch.png`,
  `.local-artifacts/b2/04_showcase_b2_probe.png`.
- Preference safety: `shared_prefs/mainconfig.xml` is **byte-identical** before and after launching the
  showcase (the saved theme stays `Cybergram`). The debug tooling performs no preference writes.

## 2. Mandatory local source reconnaissance

Commands (local clone, checked-out tree):

```text
git grep -n "TYPE_PREVIEW" -- '*.java'
git grep -n "new MessageDrawable" -- '*.java'
git grep -n "ReplyMessageLine" -- '*.java'
git grep -n "ReactionsLayoutInBubble" -- '*.java'
git grep -n "new ReactionsLayoutInBubble(" -- '*.java'
git grep -n "Cybergram" -- <message components>
git diff master -- .../ActionBar/MessageDrawable.java
```

### 2.1 `MessageDrawable` and the body seam

- Class: `TMessagesProj/src/main/java/org/telegram/ui/ActionBar/MessageDrawable.java`
  (`TYPE_TEXT`, `TYPE_MEDIA`, `TYPE_PREVIEW` — declaration at line 88).
- Cybergram seam is additive only: `dev` vs upstream `master` is **+111 lines, 0 deletions** in this file.
  Angular path gate: `generatePath` line 676 (`useAngularGeometry() && (TYPE_TEXT || TYPE_MEDIA)`);
  polygon `generateCybergramPath` (lines ~832-858, per-corner near cuts at 846-856); outline
  `drawCybergramBorder` (891-901) invoked from the ordinary draw path (619-621).
- Every `ChatMessageCell` bubble background comes from shared themed drawables:
  `getThemedDrawable(Theme.key_drawable_msgIn/Out[/Media][Selected])` at `ChatMessageCell` 20489-20573,
  21913-21921, 23162-23170 — those map to `Theme.java` 8202-8209, which construct **`TYPE_TEXT` /
  `TYPE_MEDIA` only**. `ChatMessageCell` never reaches `TYPE_PREVIEW`.
- Selection/pressed state reuses the same drawable path: `ChatMessageCell:20342`
  (`currentBackgroundDrawable.drawCached(canvas, backgroundCacheParams, selectionOverlayPaint)`), gated by
  `isDrawSelectionBackground()` (18640-18642: pressed or `isHighlighted`, i.e. multi-select).
- Grouping: the cell feeds the near flags through `setTop(..., topNear, bottomNear)`. `MessageDrawable`
  hardcodes `drawFullBottom = true; drawFullTop = true;` (lines 591-599) — **identical in upstream
  `master`**, i.e. not a Cybergram change — so the upstream "cut at slice height" branches are inert and
  the cell clips the slice itself (`ChatMessageCell:23641-23645`,
  `canvas.clipPath(backgroundCacheParams.getPath())`). Grouped join geometry is therefore driven by the
  near-corner cuts plus cell-side slicing.
- Media clipping follows the same path: `canvas.clipPath(backgroundDrawable.makePath())` at
  `ChatMessageCell:16914` and `:26040`.

### 2.2 `ReplyMessageLine`

- Class: `TMessagesProj/src/main/java/org/telegram/ui/Components/ReplyMessageLine.java`.
- **No Cybergram code exists in this class** (`git grep Cybergram` -> no matches).
- Constructed with a plain `View` (`ReplyMessageLine(View)` line 75); colour/model resolution
  `check(...)` line 237 is null-safe for `messageObject == null` (247-252) and resolves
  `key_chat_inReplyLine` / `key_chat_inReplyNameText` through the supplied `ResourcesProvider`.
- It owns the reply/quote/link/contact/fact-check/summary **plate and bar geometry**:
  `drawBackground` 614-664 — `canvas.drawRoundRect(rect, radii[0], radii[0], backgroundPaint)` when the
  radii are uniform, otherwise `backgroundPath.addRoundRect(rect, radii, ...)` — plus `drawLine` 536-580
  and `drawLoadingBackground` 732.
- Hosts: `ChatMessageCell` owns the instances and their placement (`quoteLine, linkLine, replyLine,
  contactLine, factCheckLine, summaryLine` at 1653; created at 15496, 16214, 16292, 16394, 16940, 17018,
  19562, 22648, 23002, 25454; drawn at 22801-22823). Other hosts: `StoryCaptionView:633`,
  `RichEditorListView:3783`, `RichTextCell:1447/1458`, `TextMessageEnterTransition:565`.

### 2.3 `ReactionsLayoutInBubble`

- Class: `TMessagesProj/src/main/java/org/telegram/ui/Components/Reactions/ReactionsLayoutInBubble.java`.
- **No Cybergram code exists in this class.**
- Exactly **two production instantiations**: `ChatMessageCell:1034` and `ChatActionCell:361`
  (`new ReactionsLayoutInBubble(this)`). `ActionBarMenuItem:2146` and `SearchTagsList:812` construct
  standalone `ReactionButton`s with `null` cell.
- Ownership inside the class: layout `measure` 339-431, composition `draw` 436-485, overlay/preview
  493/538, touch `checkTouchEvent`, animation `animateChange`/`recordDrawingState`/`resetAnimation`, and
  the **pill geometry** `ReactionButton.draw` 1019-… with `float rad = height / 2f;` (1111) and
  `drawRoundRect(canvas, rectTmp, rad, paint)` (1138) routed through
  `ReactionButton.drawRoundRect` 969-979 — `canvas.drawRoundRect(...)` normally, `fillTagPath(...)` for
  tags (the existing non-rounded precedent for saved-reaction tags).
- `ChatMessageCell` only positions the layout and reserves height (e.g. 15278-15329, 23508-23513,
  24088, 24160). Reaction geometry is therefore **not** a `ChatMessageCell` defect surface.

### 2.4 `TYPE_PREVIEW` call sites

Internal handling in `MessageDrawable`: 131, 141, 161, 171, 214, 232, 252, 307, 437, 573, 673, 687, 701,
740, 749, 764, 797. Behaviour that makes it special:

- `getColor`/`getCurrentColor` (161-175) bypass any `ResourcesProvider` and read the **global** `Theme`
  colours — a preview cannot be themed by an injected provider;
- motion-background slot index 1 instead of 0 (214) and no parent invalidation (232) — the preview is
  rasterized into a standalone bitmap;
- always takes the full-bubble path branch (687/701/740/749/764);
- its own radius `dp(6)` (573-575).

Actual instantiation sites (production): **two**, both theme-preview surfaces:

| site | what it is |
|---|---|
| `Theme.java:7362` inside `createThemePreviewImage(...)` (`Theme.java:7270`) | generates a theme/accent preview image; only caller `MessagesController.java:9119` |
| `Components/ThemePreviewDrawable.java:78` | `.attheme` document preview bitmap; loaded via `ImageLoader.java:882` |

Debug caller: `TMessagesProj_App/src/debug/.../CybergramShowcaseActivity.java` (this probe).
No normal messaging surface instantiates `TYPE_PREVIEW`.

### 2.5 Service/date adjacency

- Owner: `TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatActionCell.java`.
- **No Cybergram code exists in this class.**
- Ordinary service/date silhouette: `final int corner = dp(11)` (3333), line-following construction with
  `backgroundPath` + `arcTo(...)` (3367-3463), a fallback `backgroundPath.addRoundRect(rect, dp(15), ...)`
  (3471-3473), drawn at 3500-3510 through the existing background/darken/dim paint pipeline; simpler
  rounded variants at 3524, 3537, 3566-3569, 3576.
- `setCustomDate(int date, boolean scheduled, boolean inLayout)` is at 550. `ChatActionCell` also owns its
  own `reactionsLayoutInBubble` (361) and `getReactionButton` (4117).
- This matches the static map already recorded for B5 in `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md`.

## 3. Debug fixture used for E evidence

New file (debug source set only, no release effect):
`TMessagesProj_App/src/debug/java/org/telegram/ui/CybergramB2MessageStatesFixture.java`, wired into
`CybergramShowcaseActivity` as a second probe bitmap (`(dp(8), dp(536))`, 396x268 dp).

It renders **real production renderers** with synthesized data:

- `MessageDrawable` via the same `setDrawBounds + setTop(..., topNear, bottomNear) + draw` contract the
  cell uses, for TYPE_TEXT/TYPE_MEDIA/TYPE_PREVIEW in plain, selected and grouped positions;
- `ReplyMessageLine` (`new ReplyMessageLine(view)`, `check(null, null, null, provider, TYPE_REPLY)`,
  `drawBackground(...)`, `drawLine(...)`);
- `ReactionsLayoutInBubble` with hand-built `TLRPC.TL_reactionCount` buttons, production `measure(...)`
  and production `draw(...)`.

It does **not** compose a `ChatMessageCell`, so cell-level composition is out of its reach by design.

Measured pixel evidence (screenshot `04_showcase_b2_probe.png`, decoded programmatically):

| measurement | result | meaning |
|---|---|---|
| incoming `TYPE_TEXT` left-edge profile, top corner | inset 54->37 px over 17 rows, deltas `-2,-1` repeating (**slope 1.0**) | 45-degree chamfer, not an arc |
| outgoing `TYPE_TEXT` corner | same linear ramp; fill `#07252c`, outline `#00e5ff` | angular + cyan outline |
| incoming fill / selected fill | `#282715` / `#3e3c19`; amber outline `#e8d93a` | matches UI spec palette |
| outgoing selected fill / outline | `#063a44` / `#33d6ff` | matches UI spec palette |
| outgoing `TYPE_MEDIA` | fill `#07252c`, outline `#00e5ff`, placeholder `#06090d` | angular path active for media |
| grouped middle slice | left ramp ~4 px (2 dp) vs right ramp ~14 px (6 dp) | grouped near-corner cuts applied asymmetrically |
| `TYPE_PREVIEW` corner | inset 54->42 px with deltas `-4,-3,-1,-2`, then accelerating at the bottom | circular arc = upstream rounded path |
| `ReplyMessageLine` plate corner | inset 22->3 px, deltas `-8,-4,-2,-1,-1,-1` | rounded plate (arc) inside an angular bubble |
| reaction pill corner | inset 31->2 px over ~28 rows, deltas `-4,-3,-2,-2,-2,-2,-1...`; pill height 68 px | stadium pill, radius = height/2 |
| reaction pill colours | background `#0b2031`, counter text blue, **no emoji glyph pixels** | geometry renders; emoji image unavailable pre-auth |

## 4. Evidence matrix

Legend: tier `E` = debug fixture on the emulator, `S` = local source evidence (authoritative for
ownership), `A`/`P` = authenticated/physical (not available). "non-CG" = is upstream (non-Cybergram)
presentation affected.

Status values used in the matrix:

- `PASS` — behaviour/geometry matches the design authority;
- `CONFIRMED DEFECT` — the design authority requires something different and the mismatch is measured;
- `DESIGN-OPEN` — measured and owner-proven, but the design authority neither requires nor forbids the
  current shape, so it is an open design question rather than a production defect;
- `UNTESTED` — not exercised in this audit.

### 4.1 Message body (`MessageDrawable`)

| # | case | tier | expected under `CYBERGRAM_UI_SPEC.md` | actual | status | owner | category | non-CG | future scope | severity |
|---|---|---|---|---|---|---|---|---|---|---|
| 1 | incoming plain text | E+S | angular corners, amber outline, dark warm fill | measured 45-degree chamfer, fill `#282715`, outline `#e8d93a` | **PASS** | `MessageDrawable` | geometry/colour | unaffected (gate) | — | — |
| 2 | outgoing plain text | E+S | angular corners, cyan outline, dark cyan fill | measured chamfer, fill `#07252c`, outline `#00e5ff` | **PASS** | `MessageDrawable` | geometry/colour | unaffected | — | — |
| 3 | incoming selected/pressed | E+S | "selected/pressed states increase surface contrast without changing semantic side colours" | fill `#3e3c19`, amber outline retained, same chamfer | **PASS** | `MessageDrawable` + `ChatMessageCell:20342` | state transition | unaffected | — | — |
| 4 | outgoing selected/pressed | E+S | as above | fill `#063a44`, outline `#33d6ff` | **PASS** | same | state transition | unaffected | — | — |
| 5 | grouped top adjacency | E+S | "grouped messages must still join cleanly" | near-corner ramp 2 dp on the joined side, 6 dp outer | **PASS** | `MessageDrawable` (`isTopNear`) | geometry | unaffected | — | — |
| 6 | grouped middle adjacency | E+S | as above | measured asymmetric cuts (2 dp vs 6 dp); full-bubble path + cell slice clip (upstream behaviour, identical in `master`) | **PASS** | `MessageDrawable` + `ChatMessageCell:23641` | geometry | unaffected | — | — |
| 7 | grouped bottom adjacency | E+S | as above | near-corner ramp 2 dp on the joined side | **PASS** | `MessageDrawable` (`isBottomNear`) | geometry | unaffected | — | — |
| 8 | cell-side group slicing/clipping | S | slices must not leak outside the joined silhouette | mechanism identified (`backgroundCacheParams.getPath()` clip) but not rendered in this fixture | **UNTESTED** | `ChatMessageCell:23641-23645` | clipping | unaffected | — | — |
| 9 | outgoing media | E+S | angular media body, clipping/touch preserved | fill `#07252c`, outline `#00e5ff`, chamfered | **PASS** | `MessageDrawable` + `ChatMessageCell` clip | geometry | unaffected | — | — |
| 10 | incoming media | S | same as outgoing, mirrored | same production branch with `isOut=false`; not rendered in the fixture | **UNTESTED** | `MessageDrawable` | geometry | unaffected | — | — |
| 11 | media with caption (bubble geometry) | E | media body must stay angular with caption text | angular body rendered; caption layout is cell-owned | **PASS** | `MessageDrawable` | geometry | unaffected | — | — |
| 12 | caption layout / time-on-media row | A | caption/metadata must remain readable inside the angular body | requires `ChatMessageCell` | **UNTESTED** | `ChatMessageCell` | layout | unaffected | — | — |
| 13 | media clipping + touch targets | S | "media messages must preserve clipping and touch targets" | clip uses `backgroundDrawable.makePath()`, so it follows the angular path | **UNTESTED** (mechanism identified, A needed) | `ChatMessageCell:16914/26040` | clipping | unaffected | — | — |
| 14 | cell-level multi-select overlay/ripple | S | "selection/pressed overlays must follow the new path" | overlay reuses the drawable path (`drawCached(..., selectionOverlayPaint)`); the `hasSelectionOverlay()` variant is separately gated | **UNTESTED** (A) | `ChatMessageCell:20332-20345`, `18620-18642` | state transition | unaffected | — | — |
| 15 | shadow/gradient behaviour with angular path | E+S | "shadows should be minimal or absent" | no shadow/gradient artefact in the fixture output; source shows the Cybergram outline is drawn only on the final user canvas and never inside the cached nine-patch/shadow rasterization (`MessageDrawable` 885-890, called at 619-621) | **PASS** | `MessageDrawable` | geometry | unaffected | — | — |

### 4.2 Reply surfaces

| # | case | tier | expected | actual | status | owner | category | non-CG | future scope | severity |
|---|---|---|---|---|---|---|---|---|---|---|
| 16 | incoming reply block plate/bar | E+S | angular **outer** body (line 69) and reply content that survives the geometry (line 25); the anti-target is *large* rounded/glass capsules (line 21), not a compact internal reply plate/bar | measured **rounded** plate (arc) inside an **angular** bubble; amber bar `#e8d93a` | **DESIGN-OPEN** | `ReplyMessageLine.drawBackground` (614-664) | geometry | unaffected | NOT AUTHORIZED — no implementation until a design ruling | design question |
| 17 | outgoing reply block plate/bar | E+S | as row 16 | same rounded plate inside an angular outgoing bubble | **DESIGN-OPEN** | `ReplyMessageLine.drawBackground` | geometry | unaffected | NOT AUTHORIZED — no implementation until a design ruling | design question |
| 18 | reply name/text layout, ripple, selector | A | reply content stays readable and hit-testable | requires `ChatMessageCell` | **UNTESTED** | `ChatMessageCell:19562-19584`, `22648-22865` | layout | unaffected | — | — |
| 19 | quote / code / link / contact / fact-check lines | S | upstream-consistent unless ruled otherwise | owners proven (`ReplyMessageLine.TYPE_*` at 231-235); not rendered | **UNTESTED** | `ReplyMessageLine` + `ChatMessageCell` | geometry | unaffected | — | — |

### 4.3 Reaction surfaces

| # | case | tier | expected | actual | status | owner | category | non-CG | future scope | severity |
|---|---|---|---|---|---|---|---|---|---|---|
| 20 | reactions absent | E+S | no reaction chrome drawn | production `isEmpty` early-return confirmed in measure/draw | **PASS** | `ReactionsLayoutInBubble` | state | unaffected | — | — |
| 21 | one reaction (pill geometry + counter) | E+S | reactions must survive the new geometry (line 25); the spec does not require the compact reaction pill itself to be angular | measured stadium pill (radius = height/2), width grows with the counter | **DESIGN-OPEN** (grouped with 24) | `ReactionButton.draw` / `drawRoundRect` 969-1138 | geometry | unaffected | NOT AUTHORIZED — no implementation until a design ruling | design question |
| 22 | multiple reactions | E+S | pills must lay out without overlapping the bubble; spec does not mandate pill angularity | three pills measured at distinct x positions, counters 12/1234 widen the pill | **DESIGN-OPEN** (grouped with 24) | `ReactionButton` + `measure` (339-431) | layout/geometry | unaffected | NOT AUTHORIZED — no implementation until a design ruling | design question |
| 23 | chosen reaction | E+S | chosen state visible without an opaque cyan block; spec does not mandate pill angularity | chosen pill colour path resolved (`key_...ReactionButton*`); same pill geometry | **DESIGN-OPEN** (grouped with 24) | `ReactionButton.draw` (1031-1061) | state/geometry | unaffected | NOT AUTHORIZED — no implementation until a design ruling | design question |
| 24 | reaction **surface geometry** vs message language | E+S | open design question, not a spec violation | rounded stadium pills inside/next to angular bubbles | **DESIGN-OPEN** | `ReactionsLayoutInBubble.ReactionButton.drawRoundRect` (969-979) | geometry | unaffected | NOT AUTHORIZED — no implementation until a design ruling | design question |
| 25 | reaction emoji glyph rendering | E | emoji must render | no glyph pixels observed: pre-auth `MediaDataController.getReactionsMap()` is empty, so no image is set | **UNTESTED** | `ReactionsLayoutInBubble` (930-938) | colour/media | unaffected | — | — |
| 26 | reaction touch, bounce, scrim, particle animation | A | interactions unchanged | requires a real message/cell | **UNTESTED** | `ReactionsLayoutInBubble` + `ChatMessageCell` | interaction/animation | unaffected | — | — |

### 4.4 Cell composition, metadata and adjacency

| # | case | tier | expected | actual | status | owner | category | non-CG | future scope | severity |
|---|---|---|---|---|---|---|---|---|---|---|
| 27 | time / checks / views metadata inside the angular body | A | metadata must remain readable on the new silhouette | requires `ChatMessageCell` layouts | **UNTESTED** | `ChatMessageCell` | layout | unaffected | — | — |
| 28 | forwarded header/state | A | forward chrome must remain legible | requires `ChatMessageCell` name/forward layout | **UNTESTED** | `ChatMessageCell` | layout | unaffected | — | — |
| 29 | links plus message metadata | A | links keep cyan semantics (UI spec line 61) | colour keys exist (`key_chat_inPreviewLine` etc.); layout unverified | **UNTESTED** | `ChatMessageCell` + `ReplyMessageLine.TYPE_LINK` | layout/colour | unaffected | — | — |
| 30 | bot buttons bottom / body interaction | S | `botButtonsBottom` must keep working | input exists in `MessageDrawable` (688, 750) and only alters the near radius; not rendered | **UNTESTED** | `ChatMessageCell` + `MessageDrawable` | layout | unaffected | — | — |
| 31 | service/date cell adjacent to ordinary messages | S | "date separators and service messages use compact dark plates with amber/cyan accents rather than Telegram's translucent default service bubbles" | `ChatActionCell` still builds upstream rounded geometry (arcs, `corner = dp(11)`) and has no Cybergram seam | **UNTESTED** (owner + scope already established; decision belongs to B5) | `ChatActionCell` (3333-3475, 3500-3510, 3524-3579) | geometry | unaffected | `ChatActionCell.java` (B5/B6) | — |
| 32 | non-Cybergram presentation for all of the above | S | upstream rendering must remain intact | every seam is gated by `CybergramTheme.isCybergramPresentation(...)`; `MessageDrawable` angular path is additive-only vs `master` (+111/-0) | **PASS** | `MessageDrawable`, `CybergramTheme` | colour/geometry | this is the non-CG path | — | — |

### 4.5 `TYPE_PREVIEW`

| # | case | tier | expected | actual | status | owner | category | non-CG | future scope | severity |
|---|---|---|---|---|---|---|---|---|---|---|
| 33 | `TYPE_PREVIEW` geometry | E+S | not required to be angular | measured circular arc (rounded), no outline; both production call sites are theme-preview surfaces | **PASS** | `MessageDrawable` (573-575, 687-797) | geometry | unaffected | — | — |
| 34 | `TYPE_PREVIEW` colours | E+S | preview colours come from the preview's own theme source | provider bypass confirmed (161-175); the fixture therefore renders the *process-global* theme, not the probe palette | **PASS** (documented limitation) | `MessageDrawable` | colour | unaffected | — | — |
| 35 | `TYPE_PREVIEW` on a live Cybergram client | A | preview of the Cybergram theme should look like Cybergram | theme editor/preview requires an authenticated session | **UNTESTED** | `Theme.createThemePreviewImage`, `ThemePreviewDrawable` | colour/geometry | — | — | — |

## 5. Verdicts and proposed conditional passes

### 5.1 `TYPE_PREVIEW` — B3 closed, not required

`TYPE_PREVIEW` has exactly two production call sites, both theme-preview surfaces
(`Theme.createThemePreviewImage`, used only by `MessagesController:9119`, and `ThemePreviewDrawable`,
used by `ImageLoader:882` for `.attheme` thumbnails). No normal messaging surface instantiates it.
Its distinct behaviour (global-theme colour lookup, motion-background slot 1, no parent invalidation,
always-full-bubble path, 6 dp radius) exists to rasterize a standalone preview bitmap.

**Conclusion: `TYPE_PREVIEW` staying rounded is correct for its actual surfaces. B3 is `CLOSED / NOT
REQUIRED`; no implementation task is created for it.** The static exclusion in `MessageDrawable`
(676, 892) is the desired state, not a gap. Do not reopen this merely for symmetry with text/media.

### 5.2 Reply and reaction geometry — `DESIGN-OPEN`, not a confirmed defect

Measured in the same fixture: `MessageDrawable` bodies are 45-degree chamfered while `ReplyMessageLine`
plates and `ReactionsLayoutInBubble` pills are circular (rounded).

The measurement is unchanged and remains the evidence. What changed after consultant review is the
classification. `docs/CYBERGRAM_UI_SPEC.md`:

- requires angular **outer** message silhouettes (line 69);
- requires replies, reactions and media to **survive** the new geometry (line 25) — a survival/legibility
  requirement, not an angularity requirement for each inner control;
- names the anti-target as **“large rounded/glass Material capsules”** (line 21).

The spec does not state that the compact internal semantic controls — the reply plate/bar inside the bubble
or the reaction pill — must themselves become angular. Rows 16, 17 and 21-24 are therefore recorded as
**`DESIGN-OPEN`**: measured, owner-proven, and an open design question rather than a confirmed production
defect.

Consequences:

- **No B4 implementation spec is written and no B4 work is authorized.** Reply/reaction styling is
  `B4 = DESIGN-OPEN / NOT AUTHORIZED` until an explicit design ruling states that these compact controls
  should adopt the angular language.
- `ReplyMessageLine.java` and `ReactionsLayoutInBubble.java` must not be modified on this basis.
- The provenance recorded in sections 2.2/2.3 remains valid and is the correct starting evidence if a
  ruling later requires angular reply/reaction controls. The shared-surface risk also remains: both classes
  serve surfaces well beyond the message flow (`StoryCaptionView`, rich-text editors, `ChatActionCell`
  reactions, `ActionBarMenuItem`/`SearchTagsList` tags), so any future work would need an explicit
  per-instance opt-in in addition to the central Cybergram gate, exactly as in B1.

No B3 spec and no B4 spec are generated; no other conditional pass is justified by this audit.

## 6. Totals

| status | count |
|---|---|
| PASS | 14 |
| CONFIRMED DEFECT | 0 |
| DESIGN-OPEN (owners proven; implementation NOT AUTHORIZED) | 6 |
| UNTESTED | 15 |
| total cases | 35 |

`DESIGN-OPEN` owner list (provenance retained for any future design ruling; **no implementation is
authorized**):

| owner | rows | minimal future file scope (only if a design ruling requires it) |
|---|---|---|
| `ReplyMessageLine.drawBackground` | 16, 17 | `TMessagesProj/src/main/java/org/telegram/ui/Components/ReplyMessageLine.java` |
| `ReactionsLayoutInBubble.ReactionButton.drawRoundRect` | 21, 22, 23, 24 | `TMessagesProj/src/main/java/org/telegram/ui/Components/Reactions/ReactionsLayoutInBubble.java` |

No defect was assigned to `ChatMessageCell`: cell composition, metadata, forwarding, bot buttons and
group slicing are all **UNTESTED**, not defective, and the source evidence shows the cell consumes the
angular path rather than re-implementing geometry.

## 7. Explicit statements

- **No production rendering fix was made in B2.** The only repository changes in this pass are the
  debug-only fixture and the audit/correction documentation. `ReplyMessageLine.java`,
  `ReactionsLayoutInBubble.java` and `MessageDrawable.java` are untouched.
- The six reply/reaction rows were reclassified from `DEFECT` to `DESIGN-OPEN` after consultant review.
  This is a verdict/design-classification change only: all source, runtime and pixel evidence above is the
  evidence gathered by the original run and was not re-measured or discarded.
- `B3` is `CLOSED / NOT REQUIRED` and `B4` is `DESIGN-OPEN / NOT AUTHORIZED`; no implementation task exists
  for either.
- No authenticated (A) or physical (P) surface was exercised; every account-dependent state above is
  `UNTESTED`, not "passing by assumption".
- The fixture proves component-level geometry for `MessageDrawable`, `ReplyMessageLine` and
  `ReactionsLayoutInBubble`. It does **not** prove cell composition, and its `TYPE_PREVIEW` row renders
  the process-global theme (the showcase never calls `Theme.applyTheme`), so only that row's *geometry*
  is meaningful.
- Reaction emoji glyphs and chosen-state emoji rendering could not be reproduced pre-auth
  (`MediaDataController` reactions map is empty); only pill geometry and counter-driven widths were
  observable.
