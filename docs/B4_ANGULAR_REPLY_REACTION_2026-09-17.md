# B4 — angular reply plate and reaction pill

Status: `IMPLEMENTED ON dev / E BUILD PASS / DEVICE VERIFICATION PENDING (no device attached)`.

Owner authorization: "B4 (reply/reaction) — DESIGN-OPEN / NOT AUTHORIZED – бери в работу" (2026-09-19
instruction). This closes the `DESIGN-OPEN` part of B4 with an explicit ruling and a bounded
implementation.

## Ruling D11.1

Under Cybergram the **in-bubble reply plate** and the **in-bubble reaction pill** use the same shared
45-degree chamfer polygon as the message bodies (`CybergramBubbleDrawable.buildPath(...)` — no second
geometry implementation). Because `ReplyMessageLine` and `ReactionsLayoutInBubble` are shared well
beyond the message flow, each one carries an **explicit per-instance opt-in** in addition to the
central presentation gate; the classes themselves are never branch-on-theme.

## Changes

- `ReplyMessageLine.java`: new `setCybergramAngular(boolean)`; `drawBackground(...)` builds the chamfer
  path and draws it instead of the round rect when the instance is opted in. `ChatMessageCell` opts in
  **only the message `replyLine`**, in both layout paths (`CybergramTheme.useAngularMessageGeometry(...)`).
- `ReactionsLayoutInBubble.java` + `ReactionButton`: new `setCybergramAngular(boolean)` on the layout,
  propagated to every `ReactionButton` it creates; `ReactionButton.drawRoundRect(...)` — used for the
  pill, its scrim overlay and its service-shader background — draws the chamfer path when the button is
  opted in. `ChatMessageCell` opts in **only its own `reactionsLayoutInBubble`**.
- Untouched: quote/link/contact/fact-check/summary lines, rich-text editors
  (`RichTextCell`, `RichEditorListView`), story captions (`StoryCaptionView`), article views
  (`WebPagePreviewView`, `ArticleViewer`), `ChatActionCell` service-message reactions, and every
  non-Cybergram path (all opt-ins are false unless the central gate is true).

## Evidence

- arm64 build `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a --offline` →
  `BUILD SUCCESSFUL in 3m 58s`; APK 68,456,889 bytes.
- **Device verification pending.** No physical device is attached (the Redmi was unplugged) and the
  AVD is not runnable on this host (it exits within ~1 minute on every renderer backend tried), so the
  visual result of the chamfered reply plate / reaction pill has not been captured yet. The change is
  additive and gated: with the opt-in absent the code path is byte-identical to upstream.

## Residual

- Device (E/P) screenshots of a real reply and a real reaction row are required before B4 can be
  called validated; until then it is `E BUILD PASS`.
- The reaction-pill chamfer applies to the opted-in instance's pill, scrim overlay and service-shader
  background; the tag path (`isTag`) keeps its own upstream geometry by construction.
