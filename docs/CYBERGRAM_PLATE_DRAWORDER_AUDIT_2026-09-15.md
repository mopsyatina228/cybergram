# Cybergram plate draw-order audit (2026-09-15)

Status: static audit. **No production file was modified.** This document authorizes nothing.

Motivation: the authenticated B0 run found that the selected dialog filter tab renders as an empty plate
because its label is painted over (`docs/B0_FILTER_TABS_DEFECT_2026-09-15.md`). This audit sweeps every
other Cybergram seam that draws a plate, to find out whether that was an isolated mistake or a systemic
pattern.

## Method

For each seam that builds a `CybergramHudDrawable` / `CybergramBubbleDrawable`, two questions:

1. **Draw order** — is the plate drawn *before* the children that carry text/icons (`super.dispatchDraw`
   / `super.drawChild` / the cell's own content), or *after* them?
2. **Fill** — is the fill enabled, and is it opaque at draw time?

The second question is decisive because of `CybergramHudDrawable`'s own semantics
(`ActionBar/CybergramHudDrawable.java`):

- `fillEnabled` defaults to **`false`** (line 40) and only `setFillColor(...)` turns it on (73-78);
- `setStroke(...)` enables the stroke only;
- `draw(...)` emits the fill **only** when `fillEnabled && fillPaint.getAlpha() > 0` (157-158), and the
  stroke only when it is enabled (160-161).

So a plate can be drawn after the children and still be harmless — but only while its fill stays disabled.

## Results

| Seam | Where the plate is drawn | Order | Fill | Verdict |
|---|---|---|---|---|
| `FilterTabsView` selected tab plate | `drawChild` → `drawSelector` after `super.drawChild` (1407-1411); alpha at 1531 | **after children** | **enabled, opaque** (`PANEL_RAISED`) | **CONFIRMED DEFECT** — recorded in `docs/B0_FILTER_TABS_DEFECT_2026-09-15.md` |
| `MainTabsLayout` long-press selector | `dispatchDraw` at 356, `super.dispatchDraw` at 365 | before children | enabled (`PANEL_RAISED`) but under the labels | OK |
| `Components/glass/GlassTabView` selected plate | 204, `super.dispatchDraw` at 219 | before children | enabled (`PANEL_RAISED`) but under the icon/label | OK — confirmed visually on the authenticated run |
| `MainTabsActivity` outer panel | assigned as the tabs view background (439-445) | background | enabled | OK |
| `Components/FragmentSearchField` | plate assigned to `bg` (317-321), drawn at 206 before `super.dispatchDraw` (216) | before children | enabled | OK |
| `Components/FragmentFloatingButton` | plate used as `iBlur3Background`, drawn at 234 before `super.draw` (236) | background | enabled | OK |
| `Components/ChatActivityEnterView` composer frame | `dispatchDraw`: `super.dispatchDraw` at 2673, then `drawCybergramComposerFrame` at 2674 | **after children** | **disabled** — only `setStroke(...)` is called (2690), never `setFillColor` | **benign today**, latent hazard — see below |
| `Cells/DialogCell` selected row panel | early in `onDraw`, 4048, before the row content | before content | enabled (`chats_pinnedOverlay` + `chat_messagePanelSend` stroke) | OK |
| `Cells/DialogCell` row treatment (separator/accent/tick) | early in `onDraw`, 3831 | before content | alpha-limited paints | OK |
| `Cells/ChatActionCell` (B6 ordinary plate) | path used as the bubble background, before the text | background | enabled | OK |
| `ActionBar/MessageDrawable` (bodies) | path used as the bubble body | background | enabled | OK |
| `CybergramHeaderDecorationView` | dedicated overlay View above the chat container | above everything | enabled, but thin rails/corner marks in unused edge space | by design; verified visually on the authenticated run |

## Conclusion

The defect is **isolated**: every other Cybergram seam that fills a plate draws it **below** the content it
frames, which is exactly why the bottom navigation, dialogs rows, search field, FAB and bubbles all show
their labels correctly. `FilterTabsView` is the only seam that draws an enabled, opaque fill *after* its
children.

That also means the fix does not need a new pattern: **moving the Cybergram selector below the labels
matches what `MainTabsLayout` and `GlassTabView` already do.**

## Latent hazard worth recording (not a defect)

`ChatActivityEnterView.dispatchDraw` (2672-2674) draws the composer frame after the children — the same
placement that broke the filter tab. It is harmless today only because the frame enables no fill and emits
a 1 dp stroke. If anyone later adds `setFillColor(...)` to `cybergramComposerFrame`, the input field's
content would be painted over exactly as the filter-tab label is now. Either move the call before
`super.dispatchDraw`, or record explicitly why it must stay stroke-only.

## Non-claims

- Static analysis only. No runtime verification of the composer frame was possible because it requires
  opening a conversation, which the current run avoided; the composer is therefore **not** validated at
  A tier here.
- The audit covers seams that build Cybergram drawables. It does not claim to have reviewed every
  Cybergram-tinted paint or typography call site.
- Nothing here is a defect finding except the already-recorded `FilterTabsView` case.
