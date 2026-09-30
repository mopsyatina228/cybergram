# Cybergram visual convergence — pass 2

Target: physical Redmi Note 10S / Android 13 screenshot supplied 2026-09-30, compared against `design/references/design-target-hex-chat.jpg`.

Status: implementation started on `feature/visual-convergence-pass2`.

## P0 — preserve the good parts

- Keep the current angular bubble silhouette and tail geometry unless runtime evidence shows a collision.
- Keep message semantics and Telegram behaviour untouched; this pass is presentation-only.
- Keep DeepSeek Harness and Hermes background runtimes untouched during build/test work.

## P1 — message rhythm and page margins

- Add Cybergram-only inter-message row clearance so consecutive messages no longer read as a Telegram stack.
- Add symmetric side breathing room: incoming moves away from the left HUD rail; outgoing moves away from the right rail.
- Do not obtain spacing by shrinking the message text box or by moving timestamps into unsafe geometry.
## P2 — palette, material and neon

- Desaturate cyan and amber cores toward pale ice-blue and straw-yellow.
- Make message body text participate in the side palette: warm incoming, cool outgoing.
- Reduce core dominance and move perceived light into a wider, lower-alpha halo.
- Keep bubble fills dark and translucent enough for the canvas structure to read through.

## P3 — typography and header identity

- Use a lighter condensed body face; reserve medium emphasis for names and metadata.
- Reduce header title weight and tint title/subtitle into the cyan identity palette.
- Add a restrained cyan avatar ring without interfering with story rings or hit targets.

## P4 — composer

- Convert the Cybergram composer into one visual shell spanning the microphone/send zone.
- Keep the mic/send interaction as the existing Telegram control, but place it inside the common frame.
- Stop the red separator from reading as a line cutting through the cyan composer shell.
## P5 — date separator and reply plate

- Fix the actual runtime date-separator ownership path instead of only increasing alpha on the unused branch.
- Give dates a thin angular amber frame and remove the pill-like visual weight.
- Reduce reply plate opacity and visual mass; keep the accent bar thin and technical.

## P6 — HUD density

- Replace continuous red side rails with segmented structural fragments.
- Keep the grid/substrate barely visible; red must organize the page rather than dominate it.
- Avoid fake security claims or noisy decorative text that competes with message content.

## Acceptance

- `git diff --check` clean.
- `tools/validate_cybergram_theme.py` clean.
- Full `arm64-v8a` debug APK builds with `--no-daemon`.
- Physical Redmi screenshot compared against the reference before any further geometry tuning.
- No changes to protocol, account/session, networking, storage, or message behaviour.
## Implementation checkpoint — 2026-09-30

Implemented in the working tree:
- P1: +4dp Cybergram-only row space, +6dp side inset, with outgoing time/check compensation.
- P2: paler cyan/amber/red palette, warm/cool body + metadata colours, 0.6dp core and 4dp low-alpha halo.
- P3: `sans-serif-condensed-light` body, regular-weight header title, cyan header palette and avatar ring.
- P4: composer frame spans the mic/send zone; standalone Cybergram mic/send plates are suppressed; red rule is separated from the cyan frame.
- P5: real `MessageObject.isDateObject` separators enter the Cybergram angular-date path; reply plate alpha and accent width are reduced.
- P6: side rails are segmented, grid/rail/red-rule alphas are reduced.

Validation checkpoint:
- `git diff --check`: clean.
- Theme parse: 239 keys, 0 duplicates, 0 malformed entries.
- `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a --no-daemon`: PASS.
- APK ABI: arm64-v8a only; runtime physical-device screenshot still required for final visual verdict.
- Final pass-2 ARM64 debug APK: 74,326,172 bytes; SHA-256 `869E32CFAA26A0A1F8642C1C4267BA5C4F7ED47E9AF9CCA899F047A4B798EAC7`.

## Physical-device runtime validation

- Redmi Note 10S / Android 13 / arm64-v8a tested through ADB after pass-2 build.
- Header cyan identity, avatar ring, message side inset, row clearance and segmented HUD rails render as intended.
- Runtime date objects now use the angular amber plate; date halo was reduced after device inspection.
- Reply plate opacity and accent width were reduced again after the 15:50 reply sample remained too heavy.
- Composer shell was moved to the full outer input container while restoring Telegram's control layout margin.
- Result: smiley, paperclip and a static microphone glyph render inside one continuous Cybergram frame; typed send state also lays out correctly.
- Temporary test text was cleared; no test message was sent.
