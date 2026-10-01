# Cybergram visual convergence — pass 4

Target: converge the current Redmi runtime toward the Cyberpunk 2077 chat reference, with emphasis on typography, message rhythm, optical padding and softer material rendering.

Status: implementation on `feature/visual-convergence-pass4-typography-softness`.

## P0 — preserve validated pass-3 work

- Keep current angular bubble silhouette and unified composer geometry.
- Keep the segmented HUD and neutral micro-label policy.
- Keep protocol, storage, networking and Telegram message behaviour untouched.
- Treat this pass as presentation-only.

## P1 — typography seam

- Add a Cybergram-only private-font loader with safe runtime fallback.
- Preferred local test asset: extracted CP2077 Russian Raj resource converted to TTF.
- Never commit or publish proprietary game font bytes.
- Fallback body face must be less vertically stretched than `sans-serif-condensed-light`.
- Body target: approximately 15sp with regular weight.
- Time/metadata target: approximately 10.5–11sp.
- Composer placeholder should use the same body family at approximately 15sp.
- Header keeps separate chrome hierarchy and should not inherit message-body sizing.

## P2 — vertical rhythm and optical padding

- Increase Cybergram-only inter-message row gap from 4dp toward 7–8dp.
- Do not raise `BUBBLE_GAP_EXTRA_DP`; it is already constrained by time geometry.
- Raise the timestamp baseline away from the bottom edge by roughly 1.5–2dp.
- Re-evaluate body bottom padding after the smaller font is active.
## P3 — softer edge light and material

- Reduce the visual dominance of the crisp bubble core.
- Use a restrained three-layer edge-light model: dim broad halo, medium halo, thin core.
- Lower bubble fill opacity enough for the canvas substrate to faintly read through.
- Further reduce regular grid prominence before adding more decorative structure.
- Avoid whole-screen blur; softness should come from layered alpha and texture, not loss of legibility.

## P4 — composer polish

- Preserve the current unified shell and control layout.
- Restore/emphasize the cyan separator after the emoji control.
- Reduce placeholder size and move it onto the body typography family.
- Slightly reduce paperclip/microphone visual mass relative to the shell edge.
- Use the same softened edge-light model as bubbles.

## P5 — CP2077 font acquisition

- Cyberpunk 2077 is installing through Steam to `E:\SteamLibrary`.
- `E:` was selected because it has by far the largest free capacity of current Steam-capable drives.
- After install, inspect the Russian Raj font resources under the game's archives.
- Export only to a local ignored/private path for comparison and local builds.
- Measure cap height, x-height, Cyrillic widths and available weights before wiring the asset into production paths.

## Acceptance

- `git diff --check` clean.
- Cybergram theme validator clean.
- ARM64 debug build succeeds.
- Install to Redmi and compare the same chat against the reference.
- Proprietary font bytes remain local and untracked.
## Implementation checkpoint � 2026-10-01

- Steam: Cyberpunk 2077 download started in `E:\SteamLibrary` (volume index 2); `E:` was chosen because it had ~850 GB free versus ~100 GB on C:/D:.
- Added ignored private-font seam for `raj_rus_regular.ttf` / `raj_rus_medium.ttf`; no proprietary font bytes are tracked.
- Interim fallback changed from `sans-serif-condensed-light` to wider `sans-serif`, body reduced by 1sp, metadata to 10.5sp, composer text to 15sp.
- Cybergram text-message row gap raised from 4dp to 7dp; time baseline raised 1.5dp from the lower bubble edge.
- Bubble material softened: lower fill opacity, 0.55dp core at reduced alpha, 2.2dp mid halo and 5.5dp broad low-alpha bloom.
- Header/composer red rails softened; composer gained the reference-style cyan separator after the emoji control.
- `git diff --check`: PASS. Theme validator: 239 keys, 0 unknown, 0 duplicates, 0 malformed.
- `:TMessagesProj_App:assembleAfatDebug -PCYBERGRAM_ABI=arm64-v8a --no-daemon`: BUILD SUCCESSFUL in 6m 6s.
- ARM64 debug APK: 73,354,939 bytes; SHA-256 `05ecc7f081787674c37a9eda3cfca745c52e794831561583163543900475d1c5`.
- Physical-device install/visual validation is pending because ADB currently reports no connected device.

## CP2077 Russian font extraction, 2026-10-01

The Steam build is fully installed at `E:\SteamLibrary\steamapps\common\Cyberpunk 2077` (build 20383525). The Russian UI family is `base\gameplay\gui\fonts\foreign\russian\raj_rus.inkfontfamily`.

That family resolves to two actual font resources: `rajdhani_medium_neu.fnt` for Medium and `rajdhani-semibold_ru_v4.fnt` for SemiBold. WolvenKit Console 8.20.0 exported them as TTF for local comparison.

TTF metadata: Medium is weight 500, SemiBold is weight 600. Both use 1000 UPM, ascender 930, descender -346, lineGap 0, x-height 510 and cap-height 643. Both contain basic Russian Cyrillic including Ё/ё. Embedded font metadata identifies Rajdhani/Indian Type Foundry and SIL OFL 1.1.

Local-only test mapping is `rajdhani_medium_neu.ttf -> raj_rus_regular.ttf` and `rajdhani-semibold_ru_v4.ttf -> raj_rus_medium.ttf`. The private asset directory stays ignored by Git; this lets licensed local builds exercise the real in-game Cyrillic without making the binary font part of the repository.

The ARM64 pass-4 APK was rebuilt with both local TTF assets present and verified to contain `assets/fonts/cybergram_private/raj_rus_regular.ttf` and `raj_rus_medium.ttf`. Runtime visual verification remains pending until ADB sees the Redmi again.
