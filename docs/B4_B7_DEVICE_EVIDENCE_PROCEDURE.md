# B4 / B7 device evidence procedure (prepared 2026-09-19, run when a device is available)

Purpose: close the E (and, where possible, P) evidence for the two implemented-but-unverified changes
in one device session. Nothing here changes code; it is a checklist plus the exact probes, so the run
does not need a second reconnaissance pass.

Preconditions: a working AVD or the Redmi on USB. For the AVD use the verified recipe in
`docs/EMULATOR_DIAGNOSIS_2026-09-19.md` (free the Gradle daemon, clear locks, launch detached headless
with `-gpu auto -memory 3072 -no-snapshot-load -no-snapshot-save`, and do **not** build while it runs).

Build/install first (pick one ABI):
- AVD: `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=x86_64 --offline`, then `adb install -r`;
- Redmi: the same with `-PCYBERGRAM_ABI=arm64-v8a`.

## B4 — angular reply plate and reaction pill

1. Open a chat that has **a reply** (a bubble with a quoted message) and **a reaction row** with at
   least one pill. Capture the full screen into `.local-artifacts/b4-<date>/b4-chat.png`.
2. Crop the reply plate and the reaction pill at 4x (nearest neighbour) and inspect: under Cybergram
   both must show a **straight 45-degree chamfer** at every corner (no round radius).
3. Pixel probe of the reply plate corner: walk the plate's top-left corner; a chamfer means the
   boundary is a straight diagonal for `BUBBLE_CORNER_CUT_DP` (5 dp) — at density 420 that is ~13 px
   of diagonal, with no arc. Record the measured diagonal length in px and dp.
4. Pixel probe of the pill: the pill's corner must be cut, not a semicircular cap (`rad = height/2`
   upstream). Record the corner cut in px against `5 dp`.
5. **Non-Cybergram control**: switch to the Day theme (Chat Settings → the Day theme) without leaving
   the chat, re-capture, and confirm the reply plate and pill are **rounded again** (upstream), then
   switch back.
6. Repeat step 1 on a **service reaction** (`ChatActionCell`) and a story/rich surface if reachable:
   those instances must stay **rounded** (their `ReactionsLayoutInBubble` is not opted in).
7. Accessibility/touch: the reply text and the reaction pill must still respond to tap and long-press
   exactly as before.

## B7 — chat-canvas HUD

1. Open a chat under Cybergram and capture `.local-artifacts/b7-<date>/b7-chat.png`. Four sparse red
   corner brackets must be visible in the edge band (inset 7 dp), nowhere else.
2. **Edge-band probe**: count HUD-coloured pixels (red, alpha ~44 over the canvas) inside the central
   region (the middle 70% of the canvas). Expected: **0**. All HUD pixels must lie within 7 dp +
   stroke of an edge.
3. **Non-Cybergram control**: switch to Day, capture, and confirm **zero** HUD pixels anywhere;
   switch back and confirm the brackets return **without recreating the activity**.
4. **Wallpaper sweep**: repeat step 1 with a gradient, a motion/pattern wallpaper and a custom image;
   the HUD must not tint, crop or obscure the wallpaper and the brackets must still render.
5. **Blur check**: with blur enabled (theme/wallpaper settings) scroll the list so the blur/wallpaper
   capture runs; the HUD must **not** be smeared into the blurred background (the `drawChild` allow
   list only captures `actionBar`/`chatListView`/`chatInputViewsContainer`/`bottomChannelButtonsLayout`).
6. **Scrim / transition check**: open and close the emoji or attach panel and switch topics while a
   topic switch animation runs; the HUD must not flicker, survive incorrectly or be suppressed.
7. **Empty chat and undo**: open a chat with no messages (the greeting/empty view) and trigger the undo
   bar (delete a message). Both must render at their normal z-position — this is the regression the
   index compensation in `ChatActivity.cybergramHudIndexOffset()` exists to prevent.
8. **Round video**: play a round video; the HUD may draw over it only in the edge band (the video is
   centred) — record the result.
9. **Touch pass-through**: tap, long-press and pull-down directly over a bracket; the message list must
   receive the gesture (the HUD is non-interactive).
10. **Accessibility**: `adb shell uiautomator dump` and confirm the HUD contributes **no** accessibility
    node and is not focusable.

## Recording

Write the outcome into `docs/A_TIER_VALIDATION_2026-09-17_ROUND4.md` (or a new dated record), noting
per item PASS/FAIL/PARTIAL, the exact artifact paths, the measured px values, and the tier (E on the
AVD, P on the Redmi). Never promote E evidence to P.
