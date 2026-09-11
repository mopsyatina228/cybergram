# B5 — service/date plate ownership and implementation audit

Status: SOURCE OWNER IDENTIFIED, RUNTIME/CASE AUDIT READY

Type: evidence + implementation-design audit

Production fixes: FORBIDDEN IN THIS PASS

Design authority: `docs/CYBERGRAM_UI_SPEC.md`

Planning authority: `docs/EXECUTION_BACKLOG.md`

Static ownership evidence: `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md`

## Mission

Finish the evidence required for a safe Cybergram plain service/date plate implementation without turning `ChatActionCell` into a broad restyle.

Static reconnaissance has already identified the ordinary background owner. B5 must now classify which rendered action/date cases use that ordinary path and which use rich/special geometry, then define the smallest B6 implementation seam.

Do not implement B6 during B5.

## Known source facts

Primary owner:

`TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatActionCell.java`

Confirmed static path:

- `setCustomDate(...)` formats date text and feeds `updateTextInternal(...)`;
- `drawBackground(Canvas, boolean)` obtains `Theme.key_paint_chatActionBackground`, `Theme.key_paint_chatActionBackgroundDarken` and `Theme.key_paint_chatActionText`;
- when `invalidatePath` is true, the ordinary `backgroundPath` is rebuilt from `textLayout` line widths/heights;
- upstream ordinary geometry uses rounded outer/inner arc transitions following the text-line silhouette;
- the same `backgroundPath` is used for background/darken/dim drawing;
- rich gift/action/button/ribbon states also contain separate `backgroundPath2`, round-rect and other geometry paths.

Therefore the safe target is the ordinary `backgroundPath` pipeline, not a whole-cell clip/background override.

## Startup

1. Fetch current `dev` and `master`.
2. Read `AGENTS.md`, `docs/CURRENT_STATE.md`, `docs/EXECUTION_BACKLOG.md`, `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md` and the service/date target in `docs/CYBERGRAM_UI_SPEC.md`.
3. Record exact tested SHA and local status.
4. Do not reset/discard unknown local work.

## Allowed repository changes

No production code changes.

Allowed outputs:

- a dedicated dated service/date audit document;
- `docs/WORK_STATE.md` evidence append;
- debug-only showcase additions if a simple service/date case can be represented without changing production code.

## Required case inventory

Identify and classify at least:

- plain date separator via `setCustomDate(...)`;
- ordinary one-line service action;
- ordinary multi-line service action;
- a pinned-message or comparable ordinary action if it uses the same path;
- representative action with reply/navigation interaction if applicable;
- premium/star gift action;
- wallpaper action;
- offer/community/special-card state visible in current upstream;
- action with dedicated button/card background;
- service action with reactions if reproducible.

For every case record whether its visible main plate is:

- ordinary `backgroundPath`;
- separate rich/special path;
- no simple plate;
- uncertain, requiring more source tracing.

## Geometry decision to resolve

The upstream ordinary path hugs individual text-line widths with rounded convex/concave transitions. Cybergram needs a dark chamfered technical plate, but two implementation strategies are plausible:

### Strategy A — line-following chamfered outline

Preserve the changing per-line silhouette and replace rounded transitions with angular convex/concave transitions.

Pros:

- closest geometry footprint to upstream;
- preserves compact wrapping silhouette.

Risks:

- more custom path math;
- concave transitions are easy to get wrong;
- higher clipping/path regression risk.

### Strategy B — one compact chamfered enclosing plate

Use the already-computed text bounds/max line width and total text height to draw one shared `CybergramBubbleDrawable.buildPath(...)` enclosing plate.

Pros:

- simple shared geometry;
- lower implementation/maintenance risk;
- directly matches the UI spec wording "compact dark plates";
- no custom second angular path algorithm.

Risks:

- changes the silhouette for multi-line service messages;
- may create visibly excessive empty width on highly uneven line lengths.

B5 must use screenshots/representative strings to recommend A or B. Do not choose based on code elegance alone.

Default engineering preference is Strategy B only if visual evidence shows acceptable density. If not, design Strategy A explicitly before implementation rather than improvising it inside `ChatActionCell`.

## B6 candidate boundary

B5 should end with an explicit B6 proposal containing:

- exact production files, ideally only `ChatActionCell.java` plus an existing Cybergram helper if genuinely necessary;
- exact gate location using `CybergramTheme.isCybergramPresentation(themeDelegate)` or the provider object actually available at the draw site;
- exact condition that distinguishes ordinary service/date path from rich/special states;
- chosen geometry strategy and bounds;
- unchanged paint/gradient/dim pipeline;
- non-Cybergram path preservation;
- validation matrix.

If the existing `themeDelegate` type makes the central gate unavailable or semantically wrong, record that as a design issue. Do not add a theme-name heuristic.

## Explicit non-goals

Do NOT:

- clip the entire `ChatActionCell` canvas;
- restyle gift cards/buttons/ribbons;
- alter action click/touch logic;
- modify message/action data types;
- combine service/date work with reactions or normal `ChatMessageCell` bubbles;
- change global service paints merely to obtain shape geometry;
- implement B6 during this audit.

## Runtime evidence

Where an authenticated/debug environment is available, capture representative screenshots at the exact tested revision. Record cases that cannot be reproduced rather than guessing.

## Success condition

B5 succeeds when:

- ordinary versus rich service cases are classified;
- multi-line visual evidence selects or rejects Strategy B;
- the exact future B6 production scope/gate/geometry is written down;
- no production code has changed.

## Required handoff report

Return:

- exact tested SHA;
- evidence document path;
- case inventory with ordinary/rich classification;
- screenshots/artifact paths where available;
- recommended geometry strategy A or B and why;
- exact proposed B6 file scope;
- unresolved cases;
- explicit statement that B5 made no production rendering changes.