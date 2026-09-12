# B5 — service/date plate ownership and implementation audit

Status: SOURCE OWNER IDENTIFIED / RUNTIME CASE AUDIT READY

Type: evidence + implementation-design audit

Production fixes: FORBIDDEN IN THIS PASS

Design authority: `docs/CYBERGRAM_UI_SPEC.md`

Planning authority: `docs/EXECUTION_BACKLOG.md`

Static ownership evidence: `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md`

Validation runbook: `docs/runbooks/CYBERGRAM_EMULATOR_VALIDATION.md`

## Mission

Finish the evidence required for a safe Cybergram plain service/date plate implementation without turning `ChatActionCell` into a broad restyle.

Static reconnaissance has identified the ordinary background owner. B5 classifies which rendered service/date cases use that ordinary path and which use rich/special geometry, then defines the smallest B6 implementation seam.

Do not implement B6 during B5.

## Known source facts

Primary owner:

`TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatActionCell.java`

Confirmed static path:

- `setCustomDate(...)` formats date text and feeds the ordinary text/background pipeline;
- the ordinary main plate is rebuilt in `backgroundPath` from text line widths/heights;
- upstream ordinary geometry follows the varying line silhouette with rounded convex/concave transitions (`arcTo(...)`, including the established outer/inner corner calculations);
- the same ordinary `backgroundPath` is reused by background/darken/dim drawing;
- rich gift/action/button/ribbon states also contain separate `backgroundPath2`, round-rect and other geometry paths.

Therefore the safe target is the ordinary `backgroundPath` pipeline, never a whole-cell clip/background override.

## Startup

1. Fetch current `dev` and `master`.
2. Read `AGENTS.md`, `docs/CURRENT_STATE.md`, `docs/EXECUTION_BACKLOG.md`, `docs/REMAINING_UI_ARCHITECTURE_2026-09-11.md`, this file and the service/date target in `docs/CYBERGRAM_UI_SPEC.md`.
3. Record exact tested SHA and local status.
4. Do not reset/discard unknown local work.

## Mandatory local source reconnaissance

Before any debug fixture work, use the local clone to map the relevant branches. At minimum run equivalent searches for:

- `git grep -n "setCustomDate" -- 'TMessagesProj/src/main/java/**/*.java'`
- `git grep -n "backgroundPath" -- TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatActionCell.java`
- `git grep -n "backgroundPath2" -- TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatActionCell.java`
- `git grep -n "drawBackground" -- TMessagesProj/src/main/java/org/telegram/ui/Cells/ChatActionCell.java`

Trace the conditions around ordinary path generation/drawing and list special branches that replace/supplement it. Record source locations in the audit so B6 does not have to rediscover them.

## Allowed repository changes

No production code changes.

Allowed outputs:

- a dedicated dated service/date audit document;
- a clearly delimited evidence append;
- debug-only showcase/fixture additions if representative service/date cases can be exercised without changing production behaviour.

Debug tooling must remain outside release source/manifests and must not modify saved user theme preferences.

## Required case inventory

Identify and classify at least:

- plain date separator via `setCustomDate(...)`;
- ordinary one-line service action;
- ordinary multi-line service action;
- pinned-message or comparable ordinary action if it uses the same path;
- representative action with reply/navigation interaction if applicable;
- premium/star gift action;
- wallpaper action;
- offer/community/special-card state visible in current upstream;
- action with dedicated button/card background;
- service action with reactions if reproducible.

For every case record whether its visible main plate is:

- ordinary `backgroundPath`;
- separate rich/special path;
- ordinary path plus special overlays;
- no simple plate;
- uncertain, requiring more source tracing.

## Geometry decision to resolve

The upstream ordinary path hugs individual text-line widths with rounded convex/concave transitions. Cybergram needs a dark chamfered technical plate, but two bounded strategies remain plausible.

### Strategy A — line-following chamfered outline

Preserve the changing per-line silhouette and replace rounded transitions with angular convex/concave transitions.

Benefits: footprint remains close to upstream and multi-line plates stay compact.

Costs: custom concave/convex path math, more regression/maintenance risk, and likely a new geometry helper that must be justified rather than improvised.

### Strategy B — one compact chamfered enclosing plate

Use existing text bounds/max line width/total height to draw one `CybergramBubbleDrawable.buildPath(...)` enclosing plate.

Benefits: reuses shared geometry, much lower implementation risk, visually consistent with the UI spec's “compact dark plates”.

Costs: multi-line silhouette changes and uneven lines may produce excessive empty width.

B5 must decide A versus B from representative screenshots/layouts, not from code elegance.

Default engineering preference is B **only when** visual evidence shows acceptable density. If B is visibly wasteful, design A explicitly before B6 rather than creating path math ad hoc inside `ChatActionCell`.

## Runtime evidence approach

Use the existing API 36 x86_64 emulator as the default E environment.

A DEBUG fixture may exercise plain one-line/date/multi-line geometry using the real `ChatActionCell` only if it can do so without inventing a parallel renderer. If realistic construction of `ChatActionCell` state is too entangled with Telegram message data, do not fake the result with Canvas drawings merely to obtain a screenshot; mark that case for authenticated A validation instead.

Authenticated A evidence is preferred for ordinary and rich real service actions because it proves the actual branch selection.

P physical-device evidence is optional for B5 unless a defect appears device/OEM-specific.

## B6 candidate boundary

B5 ends with an explicit B6 proposal containing:

- exact production files, ideally only `ChatActionCell.java` plus an existing Cybergram helper if genuinely necessary;
- exact central gate location using the `Theme.ResourcesProvider` actually available at the draw site;
- exact condition distinguishing ordinary service/date geometry from rich/special states;
- chosen geometry strategy and exact bounds;
- unchanged background/darken/dim paint pipeline;
- preserved non-Cybergram path;
- E/A/P validation matrix.

If the provider available to `ChatActionCell` makes the central gate ambiguous, record that as a design issue. Do not introduce a theme-name or colour heuristic.

## Explicit non-goals

Do not:

- clip the entire `ChatActionCell` canvas;
- restyle gift cards/buttons/ribbons;
- alter action click/touch logic;
- modify message/action data types;
- combine service/date work with reactions or normal `ChatMessageCell` bubbles;
- change global service paints merely to obtain shape geometry;
- implement B6 during this audit.

## Success condition

B5 succeeds when:

- local source reconnaissance records the ordinary and special branch boundaries;
- required cases are classified or explicitly UNTESTED;
- multi-line visual evidence selects/rejects Strategy B;
- exact future B6 production scope/gate/geometry is written down;
- no production code changed.

## Required handoff report

Return:

- exact tested SHA;
- source-recon commands/findings;
- evidence document path;
- case inventory with ordinary/rich/overlay/untested classification;
- E/A/P evidence and screenshots where available;
- recommended geometry strategy A or B with visual rationale;
- exact proposed B6 file scope and gate;
- unresolved cases;
- explicit statement that B5 made no production rendering changes.
