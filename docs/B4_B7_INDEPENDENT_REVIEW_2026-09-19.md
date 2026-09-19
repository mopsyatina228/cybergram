# Independent review of B4 and B7, and the fixes it produced — 2026-09-19

An adversarial, read-only subagent review of the two seams was run before any device work could happen
(the host cannot run the app — `docs/EMULATOR_DIAGNOSIS_2026-09-19.md`). It found one real defect and two
consistency issues; all three are fixed below.

## Verdicts

### SAFE — no change needed

- **`ReplyMessageLine`**: the opt-in flag defaults to false; the only two opt-ins in the repository are
  the message `replyLine` sites in `ChatMessageCell`; the chamfer uses
  `CybergramBubbleDrawable.buildPath(...)` with `dp(CybergramTheme.BUBBLE_CORNER_CUT_DP)` (no second
  polygon); with the flag false the draw path is byte-identical to upstream. Every other
  `drawBackground(...)` caller stays round: `factCheckLine`, `contactLine`, `quoteLine` (all sites),
  `summaryLine`, `RichTextCell`, `RichEditorListView`, `RichMessageLayout`, `WebPagePreviewView`.
  `TextMessageEnterTransition` uses the same opted-in instance, so the enter transition matches.
- **`ReactionsLayoutInBubble` / `ReactionButton`**: the flag defaults to false and is propagated to both
  button-creation sites; `ChatActionCell`, `ActionBarMenuItem` and `SearchTagsList` never opt in; the
  `isTag` path is handled first, so tags keep upstream geometry.
- **HUD non-interactivity and gating**: not clickable/focusable, no accessibility node, `onTouchEvent`
  returns false, `onDraw` returns early unless Cybergram presentation is active, no text or claims.
- **Index compensation**: `cybergramHudIndexOffset()` is null/detach safe, and the arithmetic is
  provably correct — inserting exactly one child below index 3 / 17 shifts the intended sibling by
  exactly one, so `3 + offset` and `17 + offset` point at the same sibling with or without the HUD.
  No other fixed index needs compensation: `thanosEffect`, `topicsTabs`, `instantCameraView`,
  `pollHintView`, `pollAddOptionFieldLayout`, the side-controls and mention containers all anchor on
  `indexOf(...)`; `videoPlayerContainer` at index 1 is self-compensating; there is no
  `contentView.getChildAt(<number>)` anywhere.
- **Blur/scrim capture**: the `drawChild` allow-list excludes the HUD from blur capture, and
  `drawListImpl` iterates only `chatListView` children, so the HUD is never smeared into a blurred
  background.

### CONCERN — one real defect

**B7 layout offset.** The HUD was not a "full-size ignore-insets" child and had no layout special case,
so `ChatActivityFragmentView.onLayout` took the `TOP|LEFT` branch, which adds
`actionBar.getMeasuredHeight()`, while the generic measure gave it full-screen height. The layer was
therefore shifted down by the action-bar height and its bottom extended off-window: the bottom two
brackets were clipped and the top two sat at the header boundary instead of the canvas edge — the
four-corner framing was not realized.

**Fix**: `child == cybergramChatCanvasHudView` was added to `isFullSizeIgnoreInsersChild(...)`, whose
measure branch gives the child the full screen and whose layout branch sets `childLeft = childTop = 0`.

### CONCERN — two consistency issues (B4b)

1. the overlay-scrim backing (`drawOverlayScrim` path) drew a raw `canvas.drawRoundRect`, so its round
   corners could poke out of the chamfered pill;
2. the selected-state particle clip used a round-rect path.

**Fix**: both now use the same chamfer-aware geometry as the pill — the backing through the
`drawRoundRect(...)` helper, the clip through `CybergramBubbleDrawable.buildPath(...)` with
`cybergramAngularCut`.

### Nit reviewed, intentionally not actioned

The reaction-scrim suppression list does not include the HUD. It does not need to: the HUD sits **below**
`chatListView`, and the scrim is drawn by the cells **inside** the list, so the scrim already covers and
dims the HUD — unlike `floatingDateView` / `floatingTopicSeparator`, which sit above the list and
therefore must be suppressed.

### Consistency improvement applied

The HUD now mirrors `CybergramHeaderDecorationView`: `GONE` when Cybergram presentation is not active,
restored on the global `didSetNewTheme`, with the `onDraw` gate kept as a safety net. A Day ↔ Cybergram
switch still needs no activity recreation, and the layer is skipped by the traversal when inactive.

## Still requires a device

1. Cybergram chat: confirm all four brackets now render in the edge band after the layout fix (highest
   priority).
2. Reaction scrim and star-particle animation on a Cybergram message: chamfered pill corners intact.
3. Live Day ↔ Cybergram toggle with a chat open: HUD appears/disappears with no stale frame.
4. Round-video playback under Cybergram: HUD vs `videoPlayerContainer` stacking and scrim behaviour.

## Evidence

Compile checks after each fix: `:TMessagesProj:compileDebugJavaWithJavac` → `BUILD SUCCESSFUL in 2m 58s`
(index compensation) and `BUILD SUCCESSFUL in 1m 58s` (review fixes). No non-Cybergram path changed.
