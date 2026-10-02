# Dialogs palette and database structure — 2026-10-03

Request: align dialogs color blocks with incoming message bubbles and study the locally installed
Cyberpunk 2077 database GUI as the structural reference.

## Local reference evidence

WolvenKit CLI 9.0.1 extracted five `base/gameplay/gui/fullscreen/codex` widget/style resources from
the installed game's `basegame_1_engine.archive`. The database screen is named `codex_main.inkwidget`.
Resources and serialized JSON remain outside Git under `E:/Tools/cp2077-ui-study/database*`.

`codex_main` separates entry view, content, background, frame, image, text and low-opacity decorative
lines. `codex_style` gives `EntryItem` a default darkest-background opacity of 0.61 and frame opacity
of approximately 0.07074; Hover raises frame opacity to 1. This is a structural reference, not a
palette or artwork import: Cybergram uses its existing incoming olive/amber material.

## Implementation

- Central dialogs text tokens now use warm incoming title/metadata colors and readable muted previews.
- DialogCell draws an original chamfered olive plate in the existing text column with a quiet amber
  frame; unread and selected/archive states raise frame contrast, mentions keep red semantics.
- Avatar gutter, row height, text bounds, scrolling, touch targets and Telegram data/state logic remain
  unchanged. Plate bounds and attention rail mirror for RTL.
- The bundled theme aligns dialogs fallback text, archive surfaces and selection overlays with the
  same palette. Navigation/actions and outgoing delivery metadata retain their semantic blue.

## Validation

Before edits, x86_64 `:TMessagesProj_App:assembleAfatDebug --offline` succeeded (56 seconds).
Theme validator: 247 recognized entries, no unknown, duplicate or malformed keys.
- `:TMessagesProj:compileDebugJavaWithJavac -PCYBERGRAM_ABI=x86_64 --offline`: BUILD SUCCESSFUL
  in 3m 10s.
- `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64 --offline`: BUILD SUCCESSFUL
  in 2m 38s. Wrapper invoked through JDK 17 `GradleWrapperMain` on Windows.
- APK SHA-256: `FF1044854CDF523FBF8BDF293915BEF7606DC58C4722A5EE1C245D20EFC652A8`.
- API 36 emulator install: Success. Existing authenticated dialogs rendered with warm titles,
  previews and olive chamfered plates; archive has stronger frame contrast. Muted counters remain
  subdued. No FATAL/ANR found in the recent logcat window after launch.
- Local screenshots: `.local-artifacts/dialogs-before-list.png`, `dialogs-after.png` and
  `chat-after-dialogs-pass.png`; screenshots contain personal data and stay outside Git.
- `git diff --check`: clean. RTL/selected/mention geometry checked in code; no exhaustive runtime
  state matrix or physical-device validation is claimed.
