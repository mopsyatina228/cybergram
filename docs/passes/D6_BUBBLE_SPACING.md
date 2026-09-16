# D6 — distinct-bubble spacing (owner ruling 2026-09-15)

Status: `IMPLEMENTED / E BUILD+RUNTIME+MEASUREMENT PASS / A PENDING`

Type: bounded implementation pass derived directly from an owner ruling
(`docs/OWNER_DECISIONS_2026-09-15.md` §2 D6, owner instruction "нужно увеличить расстояние между
баблами, как на референсе").

Production scope: `MessageDrawable.java` (one method) + `CybergramTheme.java` (one constant).
No `ChatMessageCell`, measurement, layout, scroll or metadata change.

## Mission

Give distinct (unjoined) Cybergram text bubbles more clear vertical space, toward the owner
reference's ~12 px inter-bubble gap (`design/DESIGN_TARGET.md` line 29), without changing message
measurement, scroll correctness, grouped-bubble joins or the non-Cybergram path.

## Ruling constraints

From `docs/OWNER_DECISIONS_2026-09-15.md` §2 D6: increase the vertical distance between message
bubbles toward the reference **without changing message measurement, scroll correctness,
grouped-bubble joins, or the non-Cybergram path**.

Additional standing constraints from `docs/CYBERGRAM_UI_SPEC.md` lines 17/21/23/25/75/78:
readability first, high-contrast thin outlines kept, selection/pressed overlays follow the same
path, grouped messages must still join cleanly.

## Seam chosen

`TMessagesProj/src/main/java/org/telegram/ui/ActionBar/MessageDrawable.java`,
`generateCybergramPath(Path, Rect, int)` — the Cybergram-only silhouette builder that is reached
exclusively from the Cybergram branch of `generatePath` and from `drawCybergramBorder`. The bubble
body outline is inset vertically on each **unjoined** edge:

```java
float gap = currentType == TYPE_TEXT ? dp(CybergramTheme.BUBBLE_GAP_EXTRA_DP) : 0f;
float top    = bounds.top    + padding + (isTopNear    ? 0f : gap);
float bottom = bounds.bottom - padding - (isBottomNear ? 0f : gap);
```

`CybergramTheme.BUBBLE_GAP_EXTRA_DP = 2f`.

Why this seam and not `ChatMessageCell`:

- **`isTopNear` / `isBottomNear` are exactly "this edge is joined to a neighbouring bubble of the
  same run".** They are set by `MessageDrawable.setTop(...)` from `ChatMessageCell.pinnedTop` /
  `pinnedBottom`, which `ChatActivity.java:37594-37630` computes by comparing the current message
  with its neighbour (same direction, within five minutes, with the forwarded/imported cases
  handled). Joined edges therefore keep the upstream inset and merged runs still touch — grouped
  joins are preserved by construction, not by a heuristic.
- **It is a paint-only inset.** `bounds`, `setBounds`, `getBackgroundDrawableTop/Bottom`, the nine-patch
  rasterisation, `layoutHeight` and every measurement input are untouched, so scroll maths, animation
  rects and the list remain as upstream.
- **The 1 dp outline follows automatically**: `drawCybergramBorder` re-uses the same
  `generateCybergramPath`, so the outline and the fill cannot diverge.
- **The non-Cybergram path cannot be reached**: `generateCybergramPath` is only called behind
  `useAngularGeometry()` (i.e. `CybergramTheme.isCybergramPresentation(...)`).

`TYPE_MEDIA` is deliberately excluded. The photo/image `y` is set independently of the drawable
bounds (`ChatMessageCell` `photoImage.setImageCoords(0, y + namesOffset + additionalTop, …)`,
`groupMedia.y = y + namesOffset + additionalTop`), so a lowered media outline would leave the image
sticking out above its own outline.

## Known ceiling (recorded, not a defect)

The inset is bounded by the bubble's own content:

- plain text starts at `textY = dp(8) + namesOffset`, emoji-only text at `dp(6)`, against a painted
  top of `dp(3) + E` — so `E` must stay below `dp(3)` for emoji-only bubbles;
- the time/check cluster bottoms at `layoutHeight - dp(6.5)` against a painted bottom of
  `layoutHeight - dp(3) - E` — the same `dp(3.5)` ceiling.

`E = dp(2)` therefore sits inside the headroom with a ~1 dp margin and needs **no** metadata move.
Raising `E` above `dp(3)` would require moving the time, clock, error, status/check and
views/replies baselines together (see the reconnaissance list in this pass's history).

## Expected geometry

Painted body is `[bounds.top + dp(2) + E, bounds.bottom - dp(2) - E]`; two adjacent unjoined cells
therefore leave `dp(6) + 2E = dp(10)` of clear space, plus the anti-aliased edge rendering measured
on device (see evidence). Joined pairs are unchanged at `dp(6)` on the joined edge.

## Explicit non-goals

- no measurement, `layoutHeight`, `getBackgroundDrawableTop/Bottom` or scroll change;
- no `ChatMessageCell` edit and no new hook there;
- no change to media/album geometry, to `ChatActionCell` service/date plates, to replies, reactions
  or the nine-patch body;
- no change to the non-Cybergram (Day/stock) presentation;
- no new chamfer implementation — `CybergramBubbleDrawable.buildPath(...)` is still the only one.

## Required evidence matrix

| tier | required evidence |
|---|---|
| E | `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64` BUILD SUCCESSFUL; install/launch on `Cybergram_API36`; no FATAL/ANR/process death; before/after screenshots at the same scroll position of a chat with distinct text bubbles and at least one merged same-sender run; measured outline-to-outline gap before/after; merged run still touching; time/check inside the outline; reaction pill and service/date plate unmoved; non-Cybergram control screenshot unchanged |
| A | authenticated chat with mixed incoming/outgoing, a merged run, an album and reactions; visual density verdict against `CYBERGRAM_UI_SPEC.md` line 17 |
| P | not required unless a device/OEM-specific defect appears |

Never promote E evidence into A or P.

## Stop conditions

Stop and report rather than proceeding if the change would require touching `ChatMessageCell`
bounds/measurement, if joined bubbles stop joining, if the time/check cluster leaves the bubble, or
if any non-Cybergram path changes.
